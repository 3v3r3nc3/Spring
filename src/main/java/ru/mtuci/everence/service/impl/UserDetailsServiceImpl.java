package ru.mtuci.everence.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.ApplicationUser;
import ru.mtuci.everence.model.UserDetailsImpl;
import ru.mtuci.everence.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return UserDetailsImpl.fromApplicationUser(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"))
        );
    }


    public Optional<ApplicationUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<ApplicationUser> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
