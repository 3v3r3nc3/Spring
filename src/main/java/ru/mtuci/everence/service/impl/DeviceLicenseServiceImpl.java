package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.DBDevice;
import ru.mtuci.everence.model.DBDeviceLicense;
import ru.mtuci.everence.model.DBLicense;
import ru.mtuci.everence.repository.DeviceLicenseRepository;

import java.util.Date;
import java.util.List;

@Service
public class DeviceLicenseServiceImpl {
    private final DeviceLicenseRepository deviceLicenseRepository;

    public DeviceLicenseServiceImpl(DeviceLicenseRepository deviceLicenseRepository) {
        this.deviceLicenseRepository = deviceLicenseRepository;
    }



    public Long getDeviceCountForLicense(Long licenseId) {
        return deviceLicenseRepository.countByLicenseId(licenseId);
    }

    public List<DBDeviceLicense> getAllLicenseById(DBDevice device) {
        return deviceLicenseRepository.findByDeviceId(device.getId());
    }

    public DBDeviceLicense createDeviceLicense(DBLicense license, DBDevice device) {
        DBDeviceLicense newLicense = new DBDeviceLicense();
        newLicense.setLicense(license);
        newLicense.setDevice(device);
        newLicense.setActivateDate(new Date());
        return deviceLicenseRepository.save(newLicense);
    }
}
