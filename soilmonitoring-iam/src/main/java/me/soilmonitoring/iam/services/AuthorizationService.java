package me.soilmonitoring.iam.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.GrantPK;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.entities.Tenant;
import me.soilmonitoring.iam.security.AuthorizationCode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

@ApplicationScoped
public class AuthorizationService {

    @Inject
    private Logger logger;

    @Inject
    private PhoenixIAMManager manager;

    /**
     * Generate OAuth2 authorization code with PKCE
     */
    public String generateAuthorizationCode(String clientId, String username,
                                            String approvedScopes, String codeChallenge,
                                            String redirectUri) throws Exception {
        long expirationTime = Instant.now().plus(2, ChronoUnit.MINUTES).getEpochSecond();
        AuthorizationCode authCode = new AuthorizationCode(
                clientId,
                username,
                approvedScopes,
                expirationTime,
                redirectUri
        );
        return authCode.getCode(codeChallenge);
    }

    /**
     * Check which requested scopes the user has access to
     */
    public String filterScopes(String userScopes, String requestedScopes) {
        Set<String> allowedScopes = new LinkedHashSet<>();
        Set<String> requested = new HashSet<>(Arrays.asList(requestedScopes.split(" ")));
        Set<String> userHas = new HashSet<>(Arrays.asList(userScopes.split(" ")));

        for (String scope : requested) {
            if (userHas.contains(scope)) {
                allowedScopes.add(scope);
            }
        }

        return String.join(" ", allowedScopes);
    }

    /**
     * Save user's consent grant
     */
    public void saveGrant(String clientId, String username, String approvedScopes) {
        try {
            Identity identity = manager.findIdentityByUsername(username);
            Tenant tenant = manager.findTenantByName(clientId);

            Grant grant = new Grant();
            GrantPK grantPK = new GrantPK();
            grantPK.setTenantId(tenant.getId());
            grantPK.setIdentityId(identity.getId());
            grant.setId(grantPK);
            grant.setApprovedScopes(approvedScopes);
            grant.setIssuanceDateTime(LocalDateTime.now());

            manager.saveGrant(grant);
            logger.info("✅ Grant saved for user: " + username + ", client: " + clientId);

        } catch (Exception e) {
            logger.severe("Failed to save grant: " + e.getMessage());
            throw new RuntimeException("Failed to save grant", e);
        }
    }

    /**
     * Validate OAuth2 authorization request parameters
     */
    public void validateAuthorizationRequest(String clientId, String redirectUri,
                                             String responseType, String codeChallenge,
                                             String codeChallengeMethod) {
        if (clientId == null || redirectUri == null || responseType == null) {
            throw new IllegalArgumentException("Missing required OAuth2 parameters");
        }

        if (!"code".equals(responseType)) {
            throw new IllegalArgumentException("Only 'code' response_type is supported");
        }

        if (codeChallenge == null || !"S256".equals(codeChallengeMethod)) {
            throw new IllegalArgumentException("PKCE with S256 is required");
        }
        logger.info("Checking client exists for clientId: " + clientId);
        Tenant client = manager.findTenantByName(clientId);
        logger.info("Comparing redirectUri: " + redirectUri + " vs registered: " + client.getRedirectUri());
        logger.info("Validating responseType: " + responseType);
        logger.info("Validating codeChallengeMethod: " + codeChallengeMethod);

        logger.info("Raw code challenge: " + codeChallenge);


        // Validate client exists
        try {
            manager.findTenantByName(clientId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid client_id: " + clientId);
        }
    }
}