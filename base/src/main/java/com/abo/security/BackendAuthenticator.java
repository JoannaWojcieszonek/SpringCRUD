package com.abo.security;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BackendAuthenticator {

    private final @NonNull AuthenticationManager authenticationManager;

    //TODO show how to use this method in test !!

    public boolean login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        boolean isAuthenticated = isAuthenticated(authentication);
        if (isAuthenticated) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        return isAuthenticated;
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    public boolean isAuthenticated(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .filter(a -> !(a instanceof AnonymousAuthenticationToken))
                .filter(a -> a.isAuthenticated())
                .isPresent();
    }

}
