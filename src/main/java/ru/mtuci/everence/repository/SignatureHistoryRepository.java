package ru.mtuci.everence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.everence.model.ApplicationSignatureHistory;

public interface SignatureHistoryRepository extends JpaRepository<ApplicationSignatureHistory, Long> {
}
