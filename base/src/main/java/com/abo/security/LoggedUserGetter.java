package com.abo.security;

import com.abo.user.domain.User;
import com.abo.user.persistance.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.lang.String.format;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class LoggedUserGetter {

    private final @NonNull UserRepository userRepository;
    private final @NonNull BackendAuthenticator backendAuthenticator;

    //TODO important we need 3 methods and this methods must be used in aware way 2 read information from security context very fast and not have invasion for read/write to database
    //TODO one method read information form database

    public CurrentUser getLoggedUserDetails() {
        Authentication authentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(backendAuthenticator::isAuthenticated)
                .orElseThrow(() -> new AuthenticationServiceException("Cannot work with unauthenticated user"));

        return Optional.ofNullable(authentication.getPrincipal())
                .filter(principal -> principal instanceof CurrentUser)
                .flatMap(principal -> Optional.of((CurrentUser) principal))
                .orElseThrow(() -> new AuthenticationServiceException(format("Can work only with class %s", UserDetails.class)));
    }

    public String getLoggedUserName() {
        return getLoggedUserDetails().getUsername();
    }

    public User getLoggedUser() {
        final String loggedUserName = getLoggedUserName();
        return Optional.ofNullable(userRepository.findOne(loggedUserName))
                .orElseThrow(() -> new AuthenticationServiceException(format("Must have user with name: %s in DB.", loggedUserName)));
    }

}
