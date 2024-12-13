package ru.mtuci.everence.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.everence.model.ApplicationRole;
import ru.mtuci.everence.model.ApplicationUser;
import ru.mtuci.everence.model.RegisterRequest;
import ru.mtuci.everence.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String email = request.getEmail();

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("This email is already taken.");
            }

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            ApplicationUser newUser = new ApplicationUser();
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRole(ApplicationRole.USER);
            newUser.setUsername(request.getUsername());

            userRepository.save(newUser);

            return ResponseEntity.status(HttpStatus.OK).body("Registration: successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }
}
