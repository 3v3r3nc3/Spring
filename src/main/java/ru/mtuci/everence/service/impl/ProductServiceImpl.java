package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.DBProduct;
import ru.mtuci.everence.repository.ProductRepository;

import java.util.Optional;



@Service
public class ProductServiceImpl {
    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    public Optional<DBProduct> getProductById(Long id) {
        return repository.findById(id);
    }

    public String updateProduct(Long id, String name, Boolean isBlocked) {
        return getProductById(id)
                .map(product -> {
                    product.setName(name);
                    product.setBlocked(isBlocked);
                    repository.save(product);
                    return "OK";
                })
                .orElse("Product Not Found");
    }

    public Long createProduct(String name, Boolean isBlocked) {
        DBProduct product = new DBProduct();
        product.setName(name);
        product.setBlocked(isBlocked);
        repository.save(product);
        return repository.findTopByOrderByIdDesc().map(DBProduct::getId).orElse(null);
    }
}
