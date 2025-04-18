package ru.mtuci.everence.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.everence.configuration.JwtTokenProvider;
import ru.mtuci.everence.model.*;
import ru.mtuci.everence.service.impl.SignatureAuditServiceImpl;
import ru.mtuci.everence.service.impl.SignatureServiceImpl;
import ru.mtuci.everence.service.impl.UserDetailsServiceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/signature")
@RequiredArgsConstructor
public class SignatureController {

    private final SignatureServiceImpl signatureService;
    private final SignatureAuditServiceImpl signatureAuditService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @GetMapping("/actual")
    public ResponseEntity<?> getAllActualSignatures() {
        try {
            return ResponseEntity.ok(signatureService.getAllActualSignatures(SignatureStatus.ACTUAL));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }

    @PostMapping("/updated-after")
    public ResponseEntity<?> getSignaturesUpdatedAfter(@RequestBody SignatureUpdatedAfterRequest request) {
        try {
            var result = signatureService.getSignaturesUpdatedAfter(request.getSince());
            return ResponseEntity.ok(result != null ? result : "Invalid date");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }

    @PostMapping("/by-guids")
    public ResponseEntity<?> getSignaturesByGuids(@RequestBody SignatureGUIDRequest request) {
        try {
            var found = signatureService.getSignaturesByGuids(request.getGuids());
            return ResponseEntity.ok(found);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> addSignature(@RequestBody SignatureAddRequest request, HttpServletRequest req) {
        try {
            var token = req.getHeader("Authorization");
            var email = jwtTokenProvider.getUsername(token.substring(7));
            var userId = userDetailsService.getUserByEmail(email).orElseThrow().getId();

            var added = signatureService.addSignature(
                    request.getThreatName(),
                    request.getFirstBytes(),
                    request.getRemainderLength(),
                    request.getFileType(),
                    request.getOffsetStart(),
                    request.getOffsetEnd(),
                    userId
            );

            return ResponseEntity.ok(added);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> deleteSignature(@RequestBody SignatureDeleteRequest request, HttpServletRequest req) {
        try {
            var token = req.getHeader("Authorization");
            var email = jwtTokenProvider.getUsername(token.substring(7));
            var userId = userDetailsService.getUserByEmail(email).orElseThrow().getId();

            var outcome = signatureService.deleteSignature(request.getSignatureUUID(), userId);
            return ResponseEntity.ok(outcome);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateSignature(@RequestBody SignaturesUpdateRequest request, HttpServletRequest req) {
        try {
            var token = req.getHeader("Authorization");
            var email = jwtTokenProvider.getUsername(token.substring(7));
            var userId = userDetailsService.getUserByEmail(email).orElseThrow().getId();

            var updated = signatureService.updateSignature(
                    request.getSignatureId(),
                    request.getThreatName(),
                    request.getFirstBytes(),
                    request.getRemainderLength(),
                    request.getFileType(),
                    request.getOffsetStart(),
                    request.getOffsetEnd(),
                    userId,
                    request.getStatus()
            );

            return ResponseEntity.ok(updated != null ? updated : "Signature not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }

    @PostMapping("/by-status")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> getSignaturesByStatus(@RequestBody SignatureStatusRequest request) {
        try {
            var results = signatureService.getAllActualSignatures(request.getStatus());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }

    @GetMapping("/audit")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> signatureAudit() {
        try {
            var audits = signatureAuditService.getAllAuditRecords();
            return ResponseEntity.ok(audits);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There is a mistake somewhere..");
        }
    }

    @GetMapping(value = "/manifest", produces = MediaType.MULTIPART_MIXED_VALUE)
    public ResponseEntity<MultiValueMap<String, Object>> getSignatureManifest() {
        try {
            List<ApplicationSignature> signatures = signatureService.getAllActualSignatures(SignatureStatus.ACTUAL);
            int signatureCount = signatures.size();
            List<String> signatureEntries = new ArrayList<>();
            ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();

            for (ApplicationSignature signature : signatures) {
                String entry = signature.getId() + ":" + signature.getDigitalSignature();
                signatureEntries.add(entry);
                writeSignatureDataToStream(dataOutputStream, signature);
            }

            byte[] manifest = createManifest(signatureCount, signatureEntries);
            byte[] data = dataOutputStream.toByteArray();

            return createMultipartResponse(manifest, data);
        } catch (IOException ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<MultiValueMap<String, Object>> createMultipartResponse(byte[] manifest, byte[] data) {
        var partMap = new LinkedMultiValueMap<String, Object>();

        var manifestPart = new ByteArrayResource(manifest) {
            @Override
            public String getFilename() {
                return "manifest.bin";
            }
        };

        var dataPart = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return "data.bin";
            }
        };

        partMap.add("manifest", new HttpEntity<>(manifestPart, generateHeaders("manifest.bin")));
        partMap.add("data", new HttpEntity<>(dataPart, generateHeaders("data.bin")));

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("multipart/mixed")).body(partMap);
    }

    private HttpHeaders generateHeaders(String name) {
        var headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(name).build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return headers;
    }

    private void writeSignatureDataToStream(ByteArrayOutputStream out, ApplicationSignature sig) throws IOException {
        writeUuidBE(out, sig.getId());
        out.write('|');
        writeStringBE(out, sig.getThreatName());
        out.write('|');
        writeStringBE(out, sig.getFirstBytes());
        out.write('|');
        writeStringBE(out, sig.getRemainderHash());
        out.write('|');
        writeIntBE(out, sig.getRemainderLength());
        out.write('|');
        writeStringBE(out, sig.getFileType());
        out.write('|');
        writeIntBE(out, sig.getOffsetStart());
        out.write('|');
        writeIntBE(out, sig.getOffsetEnd());
        out.write(new byte[]{(byte) 0xFF, 0x00, 0x11, 0x22, 0x33});
    }

    private byte[] createManifest(int count, List<String> entries) throws IOException {
        var out = new ByteArrayOutputStream();
        writeIntBE(out, count);
        for (var entry : entries) writeStringBE(out, entry);
        var hash = signatureService.makeHash(out.toString(StandardCharsets.UTF_8));
        writeStringBE(out, signatureService.makeSignature(hash));
        return out.toByteArray();
    }

    private void writeUuidBE(ByteArrayOutputStream out, UUID uuid) {
        writeLongBE(out, uuid.getMostSignificantBits());
        writeLongBE(out, uuid.getLeastSignificantBits());
    }

    private void writeLongBE(ByteArrayOutputStream out, long val) {
        for (int i = 7; i >= 0; i--) out.write((byte) (val >> (i * 8)));
    }

    private void writeIntBE(ByteArrayOutputStream out, int val) {
        for (int i = 3; i >= 0; i--) out.write((byte) (val >> (i * 8)));
    }

    private void writeStringBE(ByteArrayOutputStream out, String val) {
        var bytes = val.getBytes();
        writeIntBE(out, bytes.length);
        out.write(bytes, 0, bytes.length);
    }
}