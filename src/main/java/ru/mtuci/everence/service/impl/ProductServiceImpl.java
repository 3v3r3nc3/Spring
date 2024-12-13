package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.DBProduct;
import ru.mtuci.everence.repository.ProductRepository;

import java.util.Optional;



@Service
public class ProductServiceImpl {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<DBProduct> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public String updateProduct(Long id, String name, Boolean isBlocked) {
        Optional<DBProduct> product = getProductById(id);
        if (product.isEmpty()) {
            return "Product Not Found";
        }

        DBProduct newProduct = product.get();
        newProduct.setName(name);
        newProduct.setBlocked(isBlocked);
        productRepository.save(newProduct);
        return "OK";
    }

    public Long createProduct(String name, Boolean isBlocked){
        DBProduct product = new DBProduct();
        product.setBlocked(isBlocked);
        product.setName(name);
        productRepository.save(product);
        return productRepository.findTopByOrderByIdDesc().get().getId();
    }
}
