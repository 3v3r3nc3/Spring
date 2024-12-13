package ru.mtuci.everence.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.everence.model.LicenseTypeEditRequest;
import ru.mtuci.everence.service.impl.LicenseTypeServiceImpl;

import java.util.Objects;

@RestController
@RequestMapping("/type")
@RequiredArgsConstructor
public class LicenseTypeEditController {

    private final LicenseTypeServiceImpl licenseTypeService;

    @PostMapping("/edit")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateLicenseType(@RequestBody LicenseTypeEditRequest request) {
        try {

            String res = licenseTypeService.updateLicenseType(request.getId(), request.getDuration(),
                    request.getDescription(), request.getName());
            if (!Objects.equals(res, "OK")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(res);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Type of license add: successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a mistake somewhere..");
        }
    }
}
