package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.DBProduct;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<DBProduct, Long> {
    Optional<DBProduct> findById(Long id);
    Optional<DBProduct> findTopByOrderByIdDesc();
}
