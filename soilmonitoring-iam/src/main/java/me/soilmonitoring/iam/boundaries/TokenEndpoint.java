package me.soilmonitoring.iam.boundaries;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.security.AuthorizationCode;
import me.soilmonitoring.iam.security.JwtManager;


import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.Set;

@Path("/oauth/token")
public class TokenEndpoint {
    private final Set<String> supportedGrantTypes = Set.of("authorization_code", "refresh_token");

    @Inject
    private PhoenixIAMManager phoenixIAMManager;

    @EJB
    private JwtManager jwtManager;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response token(@FormParam("grant_type")String grantType,
                          @FormParam("code") String authCode,
                          @FormParam("code_verifier")String codeVerifier) {
        if (grantType == null || grantType.isEmpty())
            return responseError("Invalid_request", "grant_type is required", Response.Status.BAD_REQUEST);

        if (!supportedGrantTypes.contains(grantType)) {
            return responseError("unsupported_grant_type", "grant_type should be one of :" + supportedGrantTypes, Response.Status.BAD_REQUEST);
        }
        if("refresh_token".equals(grantType)){ /*refreshed*/
            var previousAccessToken = jwtManager.verifyToken(authCode);
            var previousRefreshToken = jwtManager.verifyToken(codeVerifier);
            if(!previousAccessToken.isEmpty() && !previousRefreshToken.isEmpty()){
                try {
                    var tenantId = previousAccessToken.get("tenant_id");
                    var subject = previousRefreshToken.get("sub");
                    var scopes = previousAccessToken.get("scope");
                    var roles = Json.createReader(new StringReader(previousAccessToken.get("groups"))).readArray().getValuesAs(JsonString.class).stream().map(JsonString::getString).toList().toArray(new String[0]);
                    var accessToken = jwtManager.generateToken(tenantId,subject,scopes,roles);
                    var refreshToken = jwtManager.generateToken(tenantId,subject,scopes,new String[]{"refresh_role"});
                    var refreshSubject = previousRefreshToken.get("sub");
                    var refreshTenantId = previousRefreshToken.get("tenant_id");
                    var refreshScopes = previousRefreshToken.get("scope");
                    if(refreshScopes.equals(scopes)&&refreshTenantId.equals(tenantId)&&refreshSubject.equals(subject)) {
                        return Response.ok(Json.createObjectBuilder()
                                        .add("token_type", "Bearer")
                                        .add("access_token", accessToken)
                                        .add("expires_in", ConfigProvider.getConfig().getValue("jwt.lifetime.duration", Integer.class))
                                        .add("scope", scopes)
                                        .add("refresh_token", refreshToken)
                                        .build())
                                .header("Cache-Control", "no-store")
                                .header("Pragma", "no-cache")
                                .build();
                    }else {
                        return responseError("Invalid_request", "Can't get token", Response.Status.UNAUTHORIZED);
                    }
                } catch (Exception e){
                    throw new WebApplicationException(e);
                }
            }
            return Response.ok().build();
        }
        try {
            AuthorizationCode decoded  = AuthorizationCode.decode(authCode,codeVerifier); /*first time*/
            assert decoded!=null;
            String tenantName = decoded.tenantName();
            String accessToken = jwtManager.generateToken(tenantName, decoded.identityUsername(), decoded.approvedScopes(), phoenixIAMManager.getRoles(decoded.identityUsername()));
            String refreshToken = jwtManager.generateToken(tenantName, decoded.identityUsername(), decoded.approvedScopes(),new String[]{"refresh_role"});
            return Response.ok(Json.createObjectBuilder()
                            .add("token_type", "Bearer")
                            .add("access_token", accessToken)
                            .add("expires_in", ConfigProvider.getConfig().getValue("jwt.lifetime.duration",Integer.class))
                            .add("scope", decoded.approvedScopes())
                            .add("refresh_token", refreshToken)
                            .build())
                    .header("Cache-Control", "no-store")
                    .header("Pragma", "no-cache")
                    .build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            return responseError("Invalid_request", "Can't get token", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    private Response responseError(String error, String errorDescription, Response.Status status) {
        JsonObject errorResponse = Json.createObjectBuilder()
                .add("error", error)
                .add("error_description", errorDescription)
                .build();
        return Response.status(status)
                .entity(errorResponse).build();
    }
}
