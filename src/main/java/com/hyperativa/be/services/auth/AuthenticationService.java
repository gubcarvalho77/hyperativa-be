package com.hyperativa.be.services.auth;

import com.hyperativa.be.dtos.mapping.UserDetailsMapper;
import com.hyperativa.be.model.User;
import com.hyperativa.be.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class AuthenticationService implements UserDetailsService {

    private final UserDetailsMapper userDetailsMapper;

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {

        Optional<User> userOptional;
        if (EmailValidator.getInstance().isValid(value)) {
            userOptional = userRepository.findByEmail(value);
        } else {
            userOptional = userRepository.findByUsername(value);
        }

        return userOptional
                .map(userDetailsMapper::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(value));
    }
}
