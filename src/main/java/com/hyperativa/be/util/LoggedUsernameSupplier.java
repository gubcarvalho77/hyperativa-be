package com.hyperativa.be.util;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Component
public class LoggedUsernameSupplier implements Supplier<String> {
    @Override
    public String get() {
        var auth = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .orElse(null);

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        var principal = auth.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String s) {
            return s;
        }

        return auth.getName();
    }
}
