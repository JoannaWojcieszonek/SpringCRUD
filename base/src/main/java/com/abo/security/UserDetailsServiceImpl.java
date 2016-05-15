package com.abo.security;

import com.abo.user.persistance.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserDetailsServiceImpl implements UserDetailsService {

    private final @NonNull UserRepository userRepository;

    //TODO method responsible for load information about user, if user are not found method must throw UsernameNotFoundException
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return Optional.ofNullable(userRepository.findOne(s))
                .map(user -> CurrentUser.builder()
                        .login(user.getLogin())
                        .passwordHash(user.getPasswordHash())
                        .role(user.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(s));
    }

}
