package ru.mtuci.everence.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.everence.model.DBLicenseHistory;
import ru.mtuci.everence.model.LicenseHistoryRequest;
import ru.mtuci.everence.service.impl.LicenseHistoryServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor

public class HistoryController {
    private final LicenseHistoryServiceImpl licenseHistoryService;

    @PostMapping("/view")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> viewHistory (@RequestBody LicenseHistoryRequest request) {
        try {
            List<DBLicenseHistory> history = licenseHistoryService.getHistory(request.getFirst(), request.getLast());
            if (history.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No history?((");
            }
            return ResponseEntity.status(HttpStatus.OK).body(history);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a mistake somewhere..");
        }
    }
}
