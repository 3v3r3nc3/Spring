package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.ApplicationSignatureAudit;

public interface SignatureAuditRepository extends JpaRepository<ApplicationSignatureAudit, Long> {
}
