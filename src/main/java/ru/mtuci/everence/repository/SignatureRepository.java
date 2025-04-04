package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.ApplicationSignature;
import ru.mtuci.everence.model.SignatureStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SignatureRepository extends JpaRepository<ApplicationSignature, UUID> {
    List<ApplicationSignature> findByStatus(SignatureStatus status);

    List<ApplicationSignature> findByUpdatedAtAfter(Date since);

    List<ApplicationSignature> findByIdIn(List<UUID> guids);

    Optional<ApplicationSignature> findById(UUID id);
}

