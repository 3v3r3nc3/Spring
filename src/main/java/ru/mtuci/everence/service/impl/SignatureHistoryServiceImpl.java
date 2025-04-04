package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.ApplicationSignature;
import ru.mtuci.everence.model.ApplicationSignatureHistory;
import ru.mtuci.everence.repository.SignatureHistoryRepository;

import java.util.Date;

@Service
public class SignatureHistoryServiceImpl {
    private final SignatureHistoryRepository repository;

    public SignatureHistoryServiceImpl(SignatureHistoryRepository repository) {
        this.repository = repository;
    }

    public void createSignatureHistory(ApplicationSignature signature) {
        ApplicationSignatureHistory signatureHistory = mapToHistory(signature);
        repository.save(signatureHistory);
    }

    private ApplicationSignatureHistory mapToHistory(ApplicationSignature signature) {
        ApplicationSignatureHistory history = new ApplicationSignatureHistory();
        history.setSignatureId(signature.getId());
        Date now = new Date();
        history.setVersionCreatedAt(now);
        history.setThreatName(signature.getThreatName());
        history.setFirstBytes(signature.getFirstBytes());
        history.setRemainderHash(signature.getRemainderHash());
        history.setRemainderLength(signature.getRemainderLength());
        history.setFileType(signature.getFileType());
        history.setOffsetStart(signature.getOffsetStart());
        history.setOffsetEnd(signature.getOffsetEnd());
        history.setDigitalSignature(signature.getDigitalSignature());
        history.setUpdatedAt(signature.getUpdatedAt());
        history.setStatus(signature.getStatus());
        return history;
    }
}
