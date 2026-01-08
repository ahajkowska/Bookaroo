package org.example.bookaroo.testutils;

import org.example.bookaroo.service.CustomUserDetailsService; // Import klasy wrappera
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;
import java.util.UUID;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // uprawnienia
        var authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + customUser.role())
        );

        // odwzorowanie CustomUserDetailsService
        var principal = new CustomUserDetailsService.BookarooUserDetails(
                customUser.username(),
                customUser.password(),
                authorities,
                UUID.fromString(customUser.id()),
                false
        );

        // token autoryzacji
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                principal.getPassword(),
                principal.getAuthorities()
        );

        // do kontekstu
        context.setAuthentication(auth);
        return context;
    }
}