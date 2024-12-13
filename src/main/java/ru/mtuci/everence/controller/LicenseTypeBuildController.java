package ru.mtuci.everence.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.everence.model.LicenseTypeBuildRequest;
import ru.mtuci.everence.service.impl.LicenseTypeServiceImpl;

@RestController
@RequestMapping("/type")
@RequiredArgsConstructor
public class LicenseTypeBuildController {

    private final LicenseTypeServiceImpl licenseTypeService;

    @PostMapping("/build")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createLicenseType(@RequestBody LicenseTypeBuildRequest request) {
        try {

            Long id = licenseTypeService.createLicenseType(request.getDuration(), request.getDescription(), request.getName());

            return ResponseEntity.status(HttpStatus.OK).body("New type added successfully.\nID: " + id);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a mistake somewhere..");
        }
    }
}
