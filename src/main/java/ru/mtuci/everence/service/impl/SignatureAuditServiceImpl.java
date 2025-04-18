package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.ApplicationSignatureAudit;
import ru.mtuci.everence.repository.SignatureAuditRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class SignatureAuditServiceImpl {

    private final SignatureAuditRepository repository;

    public SignatureAuditServiceImpl(SignatureAuditRepository repository) {
        this.repository = repository;
    }

    public void createSignatureAudit(
            UUID signatureId,
            Long userId,
            String changedType,
            Date changedAt,
            String fieldChanged
    ) {
        ApplicationSignatureAudit audit = createAudit(
                signatureId, userId, changedType, changedAt, fieldChanged
        );
        repository.save(audit);
    }

    private ApplicationSignatureAudit createAudit(
            UUID signatureId,
            Long userId,
            String changedType,
            Date changedAt,
            String fieldChanged
    ) {
        ApplicationSignatureAudit audit = new ApplicationSignatureAudit();

        audit.setSignatureId(signatureId);
        audit.setChangedAt(changedAt);
        audit.setChangedType(changedType);
        audit.setFieldChanged(fieldChanged);
        audit.setChangedBy(userId);

        return audit;
    }

    public List<ApplicationSignatureAudit> getAllAuditRecords() {
        return repository.findAll();
    }
}
