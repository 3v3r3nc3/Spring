package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.DBLicenseHistory;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LicenseHistoryRepository extends JpaRepository<DBLicenseHistory, Long> {
    Optional<DBLicenseHistory> findById(Long id);
    List<DBLicenseHistory> findByChangeDateBetween(Date startDate, Date endDate);

}
