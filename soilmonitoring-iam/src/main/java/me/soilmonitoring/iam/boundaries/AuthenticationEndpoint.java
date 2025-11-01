package me.soilmonitoring.iam.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.security.Argon2Utility;
import me.soilmonitoring.iam.security.AuthorizationCode;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;


@Path("/")
@RequestScoped
public class AuthenticationEndpoint {
    public static final String CHALLENGE_RESPONSE_COOKIE_ID = "signInId";
    @Inject
    private Logger logger;

    @Inject
    PhoenixIAMManager phoenixIAMManager;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/authorize")
    public Response authorize(@Context UriInfo uriInfo) {
        var params = uriInfo.getQueryParameters();
        //1. Check tenant
        var clientId = params.getFirst("client_id");
        if (clientId == null || clientId.isEmpty()) {
            return informUserAboutError("Invalid client_id :" + clientId);
        }
        var tenant = phoenixIAMManager.findTenantByName(clientId);
        if (tenant == null) {
            return informUserAboutError("Invalid client_id :" + clientId);
        }
        //2. Client Authorized Grant Type
        if (tenant.getSupportedGrantTypes() != null && !tenant.getSupportedGrantTypes().contains("authorization_code")) {
            return informUserAboutError("Authorization Grant type, authorization_code, is not allowed for this tenant :" + clientId);
        }
        //3. redirectUri
        String redirectUri = params.getFirst("redirect_uri");
        if (tenant.getRedirectUri() != null && !tenant.getRedirectUri().isEmpty()) {
            if (redirectUri != null && !redirectUri.isEmpty() && !tenant.getRedirectUri().equals(redirectUri)) {
                //sould be in the client.redirectUri
                return informUserAboutError("redirect_uri is pre-registred and should match");
            }
            redirectUri = tenant.getRedirectUri();
        } else {
            if (redirectUri == null || redirectUri.isEmpty()) {
                return informUserAboutError("redirect_uri is not pre-registred and should be provided");
            }
        }

        //4. response_type
        String responseType = params.getFirst("response_type");
        if (!"code".equals(responseType) && !"token".equals(responseType)) {
            String error = "invalid_grant :" + responseType + ", response_type params should be code or token:";
            return informUserAboutError(error);
        }

        //5. check scope
        String requestedScope = params.getFirst("scope");
        if (requestedScope == null || requestedScope.isEmpty()) {
            requestedScope = tenant.getRequiredScopes();
        }
        //6. code_challenge_method must be S256
        String codeChallengeMethod = params.getFirst("code_challenge_method");
        if(codeChallengeMethod==null || !codeChallengeMethod.equals("S256")){
            String error = "invalid_grant :" + codeChallengeMethod + ", code_challenge_method must be 'S256'";
            return informUserAboutError(error);
        }
        StreamingOutput stream = output -> {
            try (InputStream is = Objects.requireNonNull(getClass().getResource("/login.html")).openStream()){
                output.write(is.readAllBytes());
            }
        };
        return Response.ok(stream).location(uriInfo.getBaseUri().resolve("/login/authorization"))
                .cookie(new NewCookie.Builder(CHALLENGE_RESPONSE_COOKIE_ID)
                .httpOnly(true).secure(true).sameSite(NewCookie.SameSite.STRICT).value(tenant.getName()+"#"+requestedScope+"$"+redirectUri).build()).build();
    }

    private String cipher(String codeChallenge) {
        return null;
    }

    @POST
    @Path("/login/authorization")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response login(@CookieParam(CHALLENGE_RESPONSE_COOKIE_ID) Cookie cookie,
                          @FormParam("username")String username,
                          @FormParam("password")String password,
                          @Context UriInfo uriInfo) throws Exception {
        Identity identity = phoenixIAMManager.findIdentityByUsername(username);
        if(Argon2Utility.check(identity.getPassword(),password.toCharArray())){
            logger.info("Authenticated identity:"+username);
            var params = uriInfo.getQueryParameters();
            Optional<Grant> grant = phoenixIAMManager.findGrant(cookie.getValue().split("#")[0],identity.getId());
            if(grant.isPresent()){
                String redirectURI = buildActualRedirectURI(
                        cookie.getValue().split("\\$")[1],params.getFirst("response_type"),
                        cookie.getValue().split("#")[0],
                        username,
                        checkUserScopes(grant.get().getApprovedScopes(),cookie.getValue().split("#")[1].split("\\$")[0])
                        ,params.getFirst("code_challenge"),params.getFirst("state")
                );
                return Response.seeOther(UriBuilder.fromUri(redirectURI).build()).build();
            }else{
                StreamingOutput stream = output -> {
                    try (InputStream is = Objects.requireNonNull(getClass().getResource("/consent.html")).openStream()){
                        output.write(is.readAllBytes());
                    }
                };
                return Response.ok(stream).build();
            }
        } else {
            logger.info("Failure when authenticating identity:"+username);
            URI location = UriBuilder.fromUri(cookie.getValue().split("\\$")[1])
                    .queryParam("error", "User doesn't approved the request.")
                    .queryParam("error_description", "User doesn't approved the request.")
                    .build();
            return Response.seeOther(location).build();
        }
    }

    @PATCH
    @Path("/login/authorization")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response grantConsent(@CookieParam(CHALLENGE_RESPONSE_COOKIE_ID) Cookie cookie,
                                 @FormParam("approved_scope") String scope,
                                 @FormParam("approval_status") String approvalStatus,
                                 @FormParam("username") String username){
        if ("NO".equals(approvalStatus)) {
            var location = UriBuilder.fromUri(cookie.getValue().split("\\$")[1])
                    .queryParam("error", "User doesn't approved the request.")
                    .queryParam("error_description", "User doesn't approved the request.")
                    .build();
            return Response.seeOther(location).build();
        }
        //==> YES
        List<String> approvedScopes = Arrays.stream(scope.split(" ")).toList();
        if (approvedScopes.isEmpty()) {
            var location = UriBuilder.fromUri(cookie.getValue().split("\\$")[1])
                    .queryParam("error", "User hasn't approved the request.")
                    .queryParam("error_description", "User hasn't approved the request.")
                    .build();
            return Response.seeOther(location).build();
        }
        try {
            return Response.seeOther(UriBuilder.fromUri(buildActualRedirectURI(
                    cookie.getValue().split("\\$")[1],null,
                    cookie.getValue().split("#")[0],username, String.join(" ", approvedScopes), null,null
            )).build()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildActualRedirectURI(String redirectUri,String responseType,String clientId,String userId,String approvedScopes,String codeChallenge,String state) throws Exception {
        var sb = new StringBuilder(redirectUri);
        if ("code".equals(responseType)) {
            var authorizationCode = new AuthorizationCode(clientId,userId,
                    approvedScopes, Instant.now().plus(2, ChronoUnit.MINUTES).getEpochSecond(),redirectUri);
            sb.append("?code=").append(URLEncoder.encode(authorizationCode.getCode(codeChallenge), StandardCharsets.UTF_8));
        } else {
            //Implicit: responseType=token : Not Supported
            return null;
        }
        if (state != null) {
            sb.append("&state=").append(state);
        }
        return sb.toString();
    }

    private String checkUserScopes(String userScopes, String requestedScope) {
        Set<String> allowedScopes = new LinkedHashSet<>();
        Set<String> rScopes = new HashSet<>(Arrays.asList(requestedScope.split(" ")));
        Set<String> uScopes = new HashSet<>(Arrays.asList(userScopes.split(" ")));
        for (String scope : uScopes) {
            if (rScopes.contains(scope)) allowedScopes.add(scope);
        }
        return String.join( " ", allowedScopes);
    }

    private Response informUserAboutError(String error) {
        return Response.status(Response.Status.BAD_REQUEST).entity("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <title>Error</title>
                </head>
                <body>
                <aside class="container">
                    <p>%s</p>
                </aside>
                </body>
                </html>
                """.formatted(error)).build();
    }
}
