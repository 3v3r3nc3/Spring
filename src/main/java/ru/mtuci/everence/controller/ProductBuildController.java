package ru.mtuci.everence.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.everence.model.*;
import ru.mtuci.everence.service.impl.ProductServiceImpl;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductBuildController {

    private final ProductServiceImpl productService;

    @PostMapping("/build")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createProduct(@RequestBody ProductBuildRequest request) {
        try {

            Long id = productService.createProduct(request.getName(), request.getIsBlocked());

            return ResponseEntity.status(HttpStatus.OK).body("Product create: successfully.\nID: " + id);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a mistake somewhere..");
        }
    }
}
