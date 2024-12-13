package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.DBLicenseType;
import ru.mtuci.everence.repository.LicenseTypeRepository;

import java.util.Optional;

@Service
public class LicenseTypeServiceImpl {
    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseTypeServiceImpl(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    public Optional<DBLicenseType> getLicenseTypeById(Long id) {
        return licenseTypeRepository.findById(id);
    }

    public Long createLicenseType(Long duration, String description, String name) {
        DBLicenseType licenseType = new DBLicenseType();
        licenseType.setDescription(description);
        licenseType.setName(name);
        licenseType.setDefaultDuration(duration);
        licenseTypeRepository.save(licenseType);
        return licenseType.getId();
    }


    public String updateLicenseType(Long id, Long duration, String description, String name) {
        return getLicenseTypeById(id)
                .map(licenseType -> {
                    licenseType.setName(name);
                    licenseType.setDefaultDuration(duration);
                    licenseType.setDescription(description);
                    licenseTypeRepository.save(licenseType);
                    return "OK";
                })
                .orElse("License Type Not Found");
    }

}