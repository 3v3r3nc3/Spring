package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.DBDeviceLicense;

import java.util.List;
import java.util.Optional;

public interface DeviceLicenseRepository extends JpaRepository<DBDeviceLicense, Long> {
    Optional<DBDeviceLicense> findById(Long id);
    List<DBDeviceLicense> findByDeviceId(Long deviceId);
    Long countByLicenseId(Long licenseId);
}