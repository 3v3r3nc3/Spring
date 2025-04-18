package ru.mtuci.everence.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.ApplicationSignature;
import ru.mtuci.everence.model.SignatureStatus;
import ru.mtuci.everence.repository.SignatureAuditRepository;
import ru.mtuci.everence.repository.SignatureHistoryRepository;
import ru.mtuci.everence.repository.SignatureRepository;

import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SignatureServiceImpl {

    private final SignatureRepository signatureRepository;
    private final SignatureHistoryServiceImpl signatureHistoryService;
    private final SignatureAuditServiceImpl signatureAuditService;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private Date lastCheckTime;

    @Autowired
    public SignatureServiceImpl(
            SignatureRepository signatureRepository,
            SignatureHistoryRepository signatureHistoryRepository,
            SignatureAuditRepository signatureAuditRepository,
            SignatureHistoryServiceImpl signatureHistoryService,
            SignatureAuditServiceImpl signatureAuditService,
            PrivateKey privateKey,
            PublicKey publicKey
    ) {
        this.signatureRepository = signatureRepository;
        this.signatureHistoryService = signatureHistoryService;
        this.signatureAuditService = signatureAuditService;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public List<ApplicationSignature> getAllActualSignatures(SignatureStatus status) {
        return signatureRepository.findByStatus(status);
    }

    public List<ApplicationSignature> getSignaturesUpdatedAfter(String since) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date parsedDate = sdf.parse(since);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            calendar.add(Calendar.DATE, 1);

            return signatureRepository.findByUpdatedAtAfter(calendar.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public List<ApplicationSignature> getSignaturesByGuids(List<UUID> guids) {
        return signatureRepository.findByIdIn(guids);
    }

    public ApplicationSignature addSignature(
            String threatName,
            String firstBytes,
            Integer remainderLength,
            String fileType,
            Integer offsetStart,
            Integer offsetEnd,
            Long userId
    ) throws JsonProcessingException {
        String remainderPart = firstBytes.substring(firstBytes.length() - remainderLength * 2);
        String hash = makeHash(remainderPart);

        ApplicationSignature signature = new ApplicationSignature();
        signature.setId(UUID.randomUUID());
        signature.setThreatName(threatName);
        signature.setFirstBytes(firstBytes);
        signature.setRemainderLength(remainderLength);
        signature.setFileType(fileType);
        signature.setOffsetStart(offsetStart);
        signature.setOffsetEnd(offsetEnd);
        signature.setRemainderHash(hash);
        signature.setUpdatedAt(new Date());
        signature.setStatus(SignatureStatus.ACTUAL);

        ObjectMapper objectMapper = new ObjectMapper();
        String digitalSignature = makeSignature(objectMapper.writeValueAsString(signature));
        signature.setDigitalSignature(digitalSignature);

        signatureRepository.save(signature);
        signatureHistoryService.createSignatureHistory(signature);
        signatureAuditService.createSignatureAudit(signature.getId(), userId, "CREATED", new Date(), "All");

        return signature;
    }

    public String deleteSignature(UUID signatureUUID, Long userId) {
        Optional<ApplicationSignature> signature = signatureRepository.findById(signatureUUID);

        if (signature.isEmpty()) {
            return "Wrong UUId";
        }

        ApplicationSignature deletedSignature = signature.get();
        if (deletedSignature.getStatus().equals(SignatureStatus.DELETED)) {
            return "Signature already deleted";
        }

        deletedSignature.setStatus(SignatureStatus.DELETED);
        signatureRepository.save(deletedSignature);

        signatureAuditService.createSignatureAudit(
                signatureUUID,
                userId,
                "DELETED",
                new Date(),
                "Status"
        );

        return "The signature has been successfully marked as DELETED";
    }

    public ApplicationSignature updateSignature(
            UUID signatureUUID,
            String threatName,
            String firstBytes,
            Integer remainderLength,
            String fileType,
            Integer offsetStart,
            Integer offsetEnd,
            Long userId,
            SignatureStatus status
    ) throws JsonProcessingException {
        Optional<ApplicationSignature> signature = signatureRepository.findById(signatureUUID);
        if (signature.isEmpty()) return null;

        ApplicationSignature updated = signature.get();
        signatureHistoryService.createSignatureHistory(updated);

        StringBuilder changedFields = new StringBuilder();

        if (firstBytes != null) {
            updated.setFirstBytes(firstBytes);
            changedFields.append("firstBytes, ");
        }
        if (remainderLength != null) {
            updated.setRemainderLength(remainderLength);
            changedFields.append("remainderLength, ");
        }

        String hash = makeHash(
                updated.getFirstBytes().substring(
                        updated.getFirstBytes().length() - updated.getRemainderLength() * 2
                )
        );
        updated.setRemainderHash(hash);

        if (threatName != null) {
            updated.setThreatName(threatName);
            changedFields.append("threatName, ");
        }
        if (fileType != null) {
            updated.setFileType(fileType);
            changedFields.append("fileType, ");
        }
        if (offsetStart != null) {
            updated.setOffsetStart(offsetStart);
            changedFields.append("offsetStart, ");
        }
        if (offsetEnd != null) {
            updated.setOffsetEnd(offsetEnd);
            changedFields.append("offsetEnd, ");
        }
        if (status != null && EnumSet.of(SignatureStatus.ACTUAL, SignatureStatus.DELETED, SignatureStatus.CORRUPTED).contains(status)) {
            updated.setStatus(status);
            changedFields.append("status, ");
        }

        updated.setUpdatedAt(new Date());

        ObjectMapper objectMapper = new ObjectMapper();
        updated.setDigitalSignature(makeSignature(objectMapper.writeValueAsString(updated)));

        signatureRepository.save(updated);

        if (changedFields.length() > 2) {
            changedFields.setLength(changedFields.length() - 2); // remove last ", "
        }

        signatureAuditService.createSignatureAudit(
                signatureUUID,
                userId,
                "UPDATED",
                new Date(),
                changedFields.toString()
        );

        return updated;
    }

    public String makeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    public String makeSignature(String res) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(res.getBytes());

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            return "Something went wrong. The signature is not valid";
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkSignature() throws JsonProcessingException {
        List<ApplicationSignature> signatures = signatureRepository.findByUpdatedAtAfter(lastCheckTime);
        ObjectMapper objectMapper = new ObjectMapper();

        for (ApplicationSignature signature : signatures) {
            String originalSignature = signature.getDigitalSignature();

            signature.setDigitalSignature(null);
            String recalculatedSignature = makeSignature(objectMapper.writeValueAsString(signature));
            signature.setDigitalSignature(originalSignature);

            if (!recalculatedSignature.equals(originalSignature) &&
                    signature.getStatus() == SignatureStatus.ACTUAL) {

                signatureHistoryService.createSignatureHistory(signature);
                signature.setStatus(SignatureStatus.CORRUPTED);
                signatureRepository.save(signature);

                signatureAuditService.createSignatureAudit(
                        signature.getId(),
                        null,
                        "ERROR",
                        new Date(),
                        "Status"
                );
            }
        }

        lastCheckTime = new Date();
    }
}
