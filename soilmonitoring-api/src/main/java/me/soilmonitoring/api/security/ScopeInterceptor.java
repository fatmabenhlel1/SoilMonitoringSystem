package me.soilmonitoring.api.security;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@RequireScope("")
@Interceptor
public class ScopeInterceptor {

    @Context
    private SecurityContext securityContext;

    @AroundInvoke
    public Object checkScope(InvocationContext ic) throws Exception {
        RequireScope annotation = ic.getMethod().getAnnotation(RequireScope.class);

        if (annotation == null) {
            annotation = ic.getTarget().getClass().getAnnotation(RequireScope.class);
        }

        if (annotation != null) {
            String required = annotation.value();
            var claims = ((JwtSecurityContext) securityContext).getUserPrincipal();

            // récupérer scope du JWT
            // (ajoute le champ "scope" dans JwtSecurityContext si nécessaire)

            // Pour simplifier :
            if (!securityContext.isUserInRole(required)) {
                throw new ForbiddenException("Missing scope: " + required);
            }
        }

        return ic.proceed();
    }
}
