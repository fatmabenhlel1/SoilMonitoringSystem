package me.soilmonitoring.api.security;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.*;

@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequireScope {
    String value();
}
