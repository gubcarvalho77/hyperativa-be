package com.hyperativa.be.config.auditing;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.function.Predicate;

@Configuration
@RequiredArgsConstructor
public class UserAuditorConfig implements AuditorAware<String> {

    private static final String ANONYMOUS_RESPONSE = "anonymous";

    private static final Predicate<Authentication> isAuthenticated = auth ->
            auth != null &&
                    auth.isAuthenticated() &&
                    !"anonymousUser".equals(auth.getPrincipal());

    @NonNull
    @Override
    public Optional<String> getCurrentAuditor() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isAuthenticated.test(authentication) && authentication.getPrincipal() instanceof UserDetails user) {
            return Optional.ofNullable(user.getUsername());
        }

        return Optional.of(ANONYMOUS_RESPONSE);
    }
}
