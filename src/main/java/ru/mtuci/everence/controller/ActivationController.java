package ru.mtuci.everence.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.everence.configuration.JwtTokenProvider;
import ru.mtuci.everence.model.LicenseActivationRequest;
import ru.mtuci.everence.model.DBDevice;
import ru.mtuci.everence.model.ApplicationUser;
import ru.mtuci.everence.model.Ticket;
import ru.mtuci.everence.service.impl.DeviceServiceImpl;
import ru.mtuci.everence.service.impl.LicenseServiceImpl;
import ru.mtuci.everence.service.impl.UserDetailsServiceImpl;


@RestController
@RequestMapping("/license")
@RequiredArgsConstructor
public class ActivationController {

    private final DeviceServiceImpl deviceService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final LicenseServiceImpl licenseService;

    @PostMapping("/activation")
    public ResponseEntity<?> activateLicense(@RequestBody LicenseActivationRequest request, HttpServletRequest req) {
        try {
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();
            DBDevice device = deviceService.registerOrUpdateDevice(request.getMacAddress(), request.getName(), user, request.getDeviceId());

            Ticket ticket = licenseService.activateLicense(request.getActivationCode(), device, user);

            if (!ticket.getStatus().equals("OK")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ticket.getInfo());
            }

            return ResponseEntity.status(HttpStatus.OK).body(ticket);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a mistake somewhere..");
        }
    }
}
