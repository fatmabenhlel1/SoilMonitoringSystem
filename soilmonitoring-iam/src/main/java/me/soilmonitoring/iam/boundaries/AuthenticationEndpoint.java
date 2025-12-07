package me.soilmonitoring.iam.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.security.Argon2Utility;
import me.soilmonitoring.iam.services.AuthorizationService;
import me.soilmonitoring.iam.services.HtmlTemplateService;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/")
@RequestScoped
public class AuthenticationEndpoint {

    public static final String CHALLENGE_RESPONSE_COOKIE_ID = "signInId";

    @Inject private Logger logger;
    @Inject private PhoenixIAMManager phoenixIAMManager;
    @Inject private Argon2Utility argon2Utility;
    @Inject private AuthorizationService authService;
    @Inject private HtmlTemplateService htmlService;

    /**
     * OAuth2 Authorization Endpoint - Shows login page
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/authorize")
    public Response authorize(@Context UriInfo uriInfo) {
        var params = uriInfo.getQueryParameters();

        String clientId = params.getFirst("client_id");
        String redirectUri = params.getFirst("redirect_uri");
        String responseType = params.getFirst("response_type");
        String scope = params.getFirst("scope");
        String state = params.getFirst("state");
        String codeChallenge = params.getFirst("code_challenge");
        String codeChallengeMethod = params.getFirst("code_challenge_method");
        String error = params.getFirst("error");

        try {
            // Validate OAuth2 parameters
            authService.validateAuthorizationRequest(clientId, redirectUri, responseType,
                    codeChallenge, codeChallengeMethod);

            // Create session cookie
            String cookieValue = String.format("%s#%s$%s$%s$%s",
                    clientId,
                    scope != null ? scope : "openid profile email",
                    redirectUri,
                    codeChallenge,
                    state != null ? state : ""
            );

            NewCookie sessionCookie = new NewCookie.Builder(CHALLENGE_RESPONSE_COOKIE_ID)
                    .value(cookieValue)
                    .path("/")
                    .maxAge(600)
                    .httpOnly(true)
                    .secure(false)
                    .build();

            // Show login page
            String loginHtml = htmlService.buildLoginPage(clientId, scope, error);
            return Response.ok(loginHtml).cookie(sessionCookie).build();

        } catch (IllegalArgumentException e) {
            logger.warning("Authorization validation failed: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(htmlService.buildErrorPage("Invalid Request", e.getMessage()))
                    .build();
        }
    }

    /**
     * Login Form Handler
     */
    @POST
    @Path("/login/authorization")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@CookieParam(CHALLENGE_RESPONSE_COOKIE_ID) Cookie cookie,
                          @FormParam("username") String username,
                          @FormParam("password") String password) {

        if (cookie == null || cookie.getValue() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(htmlService.buildErrorPage("Session Expired", "Please try again"))
                    .build();
        }

        // Parse session cookie
        String[] parts = cookie.getValue().split("\\$");
        String clientIdAndScope = parts[0];
        String redirectUri = parts[1];
        String codeChallenge = parts[2];
        String state = parts.length > 3 ? parts[3] : "";

        String[] clientParts = clientIdAndScope.split("#");
        String clientId = clientParts[0];
        String scope = clientParts.length > 1 ? clientParts[1] : "openid profile email";

        try {
            // Authenticate user
            Identity identity = phoenixIAMManager.findIdentityByUsername(username);

            if (!identity.isAccountActivated()) {
                return redirectToLogin(clientId, redirectUri, scope, codeChallenge, state, "account_not_activated");
            }

            if (!argon2Utility.check(identity.getPassword(), password.toCharArray())) {
                logger.info("Failed login attempt for: " + username);
                return redirectToLogin(clientId, redirectUri, scope, codeChallenge, state, "invalid_credentials");
            }

            logger.info("✅ Authenticated: " + username);

            // Check for existing grant
            Optional<Grant> existingGrant = phoenixIAMManager.findGrant(clientId, identity.getId());

            if (existingGrant.isPresent()) {
                // Skip consent - generate code directly
                String approvedScopes = authService.filterScopes(
                        existingGrant.get().getApprovedScopes(), scope);
                return redirectWithCode(username, clientId, approvedScopes, codeChallenge, redirectUri, state);
            } else {
                // Show consent screen
                String consentHtml = htmlService.buildConsentPage(clientId, scope, username);
                return Response.ok(consentHtml).build();
            }

        } catch (IllegalArgumentException e) {
            return redirectToLogin(clientId, redirectUri, scope, codeChallenge, state, "invalid_credentials");
        } catch (Exception e) {
            logger.severe("Login error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(htmlService.buildErrorPage("Server Error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Consent Form Handler
     */
    @POST
    @Path("/consent")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response handleConsent(@CookieParam(CHALLENGE_RESPONSE_COOKIE_ID) Cookie cookie,
                                  @FormParam("approved_scope") String scope,
                                  @FormParam("approval_status") String approvalStatus,
                                  @FormParam("username") String username) {

        if (cookie == null || cookie.getValue() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(htmlService.buildErrorPage("Session Expired", "Please try again"))
                    .build();
        }

        String[] parts = cookie.getValue().split("\\$");
        String clientId = parts[0].split("#")[0];
        String redirectUri = parts[1];
        String codeChallenge = parts[2];
        String state = parts.length > 3 ? parts[3] : "";

        if ("NO".equals(approvalStatus)) {
            return redirectWithError(redirectUri, state, "access_denied", "User denied request");
        }

        try {
            // Save grant
            authService.saveGrant(clientId, username, scope);

            // Generate code and redirect
            return redirectWithCode(username, clientId, scope, codeChallenge, redirectUri, state);

        } catch (Exception e) {
            logger.severe("Consent error: " + e.getMessage());
            return redirectWithError(redirectUri, state, "server_error", "Failed to process consent");
        }
    }

    // Helper methods

    private Response redirectWithCode(String username, String clientId, String scope,
                                      String codeChallenge, String redirectUri, String state) {
        try {
            String authCode = authService.generateAuthorizationCode(
                    clientId, username, scope, codeChallenge, redirectUri);

            URI location = UriBuilder.fromUri(redirectUri)
                    .queryParam("code", authCode)
                    .queryParam("state", state)
                    .build();

            return Response.seeOther(location).build();
        } catch (Exception e) {
            logger.severe("Failed to generate code: " + e.getMessage());
            return redirectWithError(redirectUri, state, "server_error", "Failed to generate code");
        }
    }

    private Response redirectToLogin(String clientId, String redirectUri, String scope,
                                     String codeChallenge, String state, String error) {
        try {
            URI location = new URI("http://iam.soilmonitoring.me:8080/iam/authorize" +
                    "?error=" + error +
                    "&client_id=" + clientId +
                    "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, "UTF-8") +
                    "&response_type=code" +
                    "&scope=" + java.net.URLEncoder.encode(scope, "UTF-8") +
                    "&code_challenge=" + codeChallenge +
                    "&code_challenge_method=S256" +
                    "&state=" + state);
            return Response.seeOther(location).build();
        } catch (Exception e) {
            logger.severe("Failed to build redirect URL: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(htmlService.buildErrorPage("Server Error", "Failed to redirect"))
                    .build();
        }
    }

    private Response redirectWithError(String redirectUri, String state, String error, String description) {
        try {
            URI location = new URI(redirectUri +
                    "?error=" + java.net.URLEncoder.encode(error, "UTF-8") +
                    "&error_description=" + java.net.URLEncoder.encode(description, "UTF-8") +
                    "&state=" + state);
            return Response.seeOther(location).build();
        } catch (Exception e) {
            logger.severe("Failed to build error redirect: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(htmlService.buildErrorPage("Server Error", e.getMessage()))
                    .build();
        }
    }
}