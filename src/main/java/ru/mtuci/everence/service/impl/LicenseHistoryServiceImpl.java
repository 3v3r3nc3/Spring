package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.DBLicense;
import ru.mtuci.everence.model.DBLicenseHistory;
import ru.mtuci.everence.model.ApplicationUser;
import ru.mtuci.everence.repository.LicenseHistoryRepository;


import java.util.Date;


@Service
public class LicenseHistoryServiceImpl {
    private final LicenseHistoryRepository licenseHistoryRepository;


    public LicenseHistoryServiceImpl(LicenseHistoryRepository licenseHistoryRepository) {
        this.licenseHistoryRepository = licenseHistoryRepository;
    }


    public DBLicenseHistory createNewRecord(String status, String description, ApplicationUser user, DBLicense license){
        DBLicenseHistory newHistory = new DBLicenseHistory();
        newHistory.setLicense(license);
        newHistory.setStatus(status);
        newHistory.setChangeDate(new Date());
        newHistory.setDescription(description);
        newHistory.setUser(user);

        return licenseHistoryRepository.save(newHistory);
    }
}
