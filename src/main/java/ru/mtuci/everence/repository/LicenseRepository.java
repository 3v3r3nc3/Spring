package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.DBLicense;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<DBLicense, Long> {
    Optional<DBLicense> findById(Long id);
    Optional<DBLicense> findByCode(String code);
    Optional<DBLicense> findByIdInAndCode(List<Long> ids, String code);
}
