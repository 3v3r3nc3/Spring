package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.DBDevice;
import ru.mtuci.everence.model.ApplicationUser;
import ru.mtuci.everence.repository.DeviceRepository;

import java.util.Optional;

//TODO: 1. registerOrUpdateDevice - откуда клиент получит id записи из вашей базы данных?

@Service
public class DeviceServiceImpl {
    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Optional<DBDevice> getDeviceByIdAndUser(ApplicationUser user, Long id) {
        return deviceRepository.findByIdAndUser(id, user);
    }

    public Optional<DBDevice> getDeviceByInfo(ApplicationUser user, String mac_address, String name) {
        return deviceRepository.findByUserAndMacAddressAndName(user, mac_address, name);
    }

    public void deleteLastDevice(ApplicationUser user) {
        Optional<DBDevice> lastDevice = deviceRepository.findTopByUserOrderByIdDesc(user);
        lastDevice.ifPresent(deviceRepository::delete);
    }

    public DBDevice registerOrUpdateDevice(String mac, String name, ApplicationUser user, Long deviceId) {
        DBDevice newDevice = getDeviceByIdAndUser(user, deviceId)
                .orElse(new DBDevice());

        newDevice.setName(name);
        newDevice.setMacAddress(mac);
        newDevice.setUser(user);

        return deviceRepository.save(newDevice);
    }

}
