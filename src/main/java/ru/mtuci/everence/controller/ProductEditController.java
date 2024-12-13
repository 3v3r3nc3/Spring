package ru.mtuci.everence.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.everence.model.ProductEditRequest;
import ru.mtuci.everence.service.impl.ProductServiceImpl;

import java.util.Objects;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductEditController {

    private final ProductServiceImpl productService;

    @PostMapping("/edit")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateProduct(@RequestBody ProductEditRequest request) {
        try {

            String res = productService.updateProduct(request.getProductId(), request.getName(), request.getIsBlocked());
            if (!Objects.equals(res, "OK")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(res);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Product update: successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a mistake somewhere..");
        }
    }
}