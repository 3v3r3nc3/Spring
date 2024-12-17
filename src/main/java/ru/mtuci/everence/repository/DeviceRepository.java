package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.DBDevice;
import ru.mtuci.everence.model.ApplicationUser;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<DBDevice, Long> {
    Optional<DBDevice> findById(Long id);
    Optional<DBDevice> findByUserAndMacAddressAndName(ApplicationUser user, String mac_address, String name);
    Optional<DBDevice> findByMacAddressAndUser(String mac, ApplicationUser user);
    Optional<DBDevice> findTopByUserOrderByIdDesc(ApplicationUser user);
}

