package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.DBLicense;
import ru.mtuci.everence.model.DBLicenseHistory;
import ru.mtuci.everence.model.ApplicationUser;
import ru.mtuci.everence.repository.LicenseHistoryRepository;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//TODO: 1. Как получить историю?


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

    public List<DBLicenseHistory> getHistory(String first, String last){

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dateFirst = formatter.parse(first);
            Date dateLast = formatter.parse(last);
            return licenseHistoryRepository.findByChangeDateBetween(dateFirst, dateLast);
        } catch (Exception e) {
            return null;
        }
    }
}
