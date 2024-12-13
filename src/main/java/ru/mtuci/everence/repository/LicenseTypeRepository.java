package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.DBLicenseType;

import java.util.Optional;

public interface LicenseTypeRepository extends JpaRepository<DBLicenseType, Long> {
    Optional<DBLicenseType> findById(Long id);
}