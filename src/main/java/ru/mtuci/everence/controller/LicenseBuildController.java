package ru.mtuci.everence.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.everence.configuration.JwtTokenProvider;
import ru.mtuci.everence.model.*;
import ru.mtuci.everence.service.impl.*;

@RestController
@RequestMapping("/license")
@RequiredArgsConstructor
public class LicenseBuildController {

    private final ProductServiceImpl productService;
    private final UserDetailsServiceImpl userService;
    private final LicenseTypeServiceImpl licenseTypeService;
    private final LicenseServiceImpl licenseService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;



    @PostMapping("/build")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createLicense(@RequestBody LicenseBuildRequest request, HttpServletRequest req) {
        try {
            Long productId = request.getProductId();
            Long ownerId = request.getOwnerId();
            Long licenseTypeId = request.getLicenseTypeId();

            if (productService.getProductById(productId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("There is no product with this ID.");
            }

            if (productService.getProductById(productId).get().isBlocked()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("This product is missing.");
            }

            if (userService.getUserById(ownerId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The owner with this ID was not found");
            }

            if (licenseTypeService.getLicenseTypeById(licenseTypeId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("There is no license with the specified ID.");
            }

            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();

            Long id = licenseService.createLicense(productId, ownerId, licenseTypeId, user, request.getCount());

            return ResponseEntity.status(HttpStatus.OK).body("License create: successfully.\nID: " + id);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a mistake somewhere..");
        }
    }
}