package me.soilmonitoring.api.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.Set;
import java.util.stream.Collectors;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Inject
    JwtValidator validator;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String auth = ctx.getHeaderString("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Missing token");
        }

        String token = auth.substring("Bearer ".length());

        try {
            String kid = JwtUtils.extractKid(token);
            var key = validator.getKey(kid);

            if (!JwtUtils.verifySignature(token, key))
                throw new NotAuthorizedException("Invalid signature");

            JsonObject claims = JwtUtils.decodePayload(token);

            String username = claims.getString("sub");
            Set<String> roles = claims.getJsonArray("groups")
                    .stream().map(v -> v.toString().replace("\"",""))
                    .collect(Collectors.toSet());

            ctx.setSecurityContext(new JwtSecurityContext(
                    username, roles, ctx.getUriInfo().getAbsolutePath().toString().startsWith("https")
            ));

        } catch (Exception e) {
            throw new NotAuthorizedException("Invalid token");
        }
    }
}
