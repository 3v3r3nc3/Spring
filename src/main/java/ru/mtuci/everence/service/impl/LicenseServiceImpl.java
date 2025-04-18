package ru.mtuci.everence.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.everence.model.*;
import ru.mtuci.everence.repository.LicenseRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.security.*;
import java.util.stream.Collectors;



@Service
public class LicenseServiceImpl {
    private final LicenseRepository licenseRepository;
    private final LicenseTypeServiceImpl licenseTypeService;
    private final ProductServiceImpl productService;
    private final DeviceLicenseServiceImpl deviceLicenseService;
    private final LicenseHistoryServiceImpl licenseHistoryService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final DeviceServiceImpl deviceServiceImpl;

    public LicenseServiceImpl(LicenseRepository licenseRepository, LicenseTypeServiceImpl licenseTypeService,
                              ProductServiceImpl productService, DeviceLicenseServiceImpl deviceLicenseService,
                              LicenseHistoryServiceImpl licenseHistoryService, UserDetailsServiceImpl userDetailsServiceImpl, DeviceServiceImpl deviceServiceImpl) {
        this.licenseRepository = licenseRepository;
        this.licenseTypeService = licenseTypeService;
        this.productService = productService;
        this.deviceLicenseService = deviceLicenseService;
        this.licenseHistoryService = licenseHistoryService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.deviceServiceImpl = deviceServiceImpl;
    }

    public Optional<DBLicense> getLicenseById(Long id) {
        return licenseRepository.findById(id);
    }

    public Long createLicense(Long productId, Long ownerId, Long licenseTypeId, ApplicationUser user, Long count) {
        DBLicenseType licenseType = licenseTypeService.getLicenseTypeById(licenseTypeId).orElseThrow(() -> new NoSuchElementException("License type not found"));
        DBProduct product = productService.getProductById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        ApplicationUser owner = userDetailsServiceImpl.getUserById(ownerId).orElseThrow(() -> new NoSuchElementException("User not found"));

        String uid = generateUniqueCode(licenseRepository);
        DBLicense newLicense = new DBLicense();
        newLicense.setCode(uid);
        newLicense.setProduct(product);
        newLicense.setLicenseType(licenseType);
        newLicense.setBlocked(product.isBlocked());
        newLicense.setDeviceCount(count);
        newLicense.setOwnerId(owner);
        newLicense.setDuration(licenseType.getDefaultDuration());
        newLicense.setDescription(licenseType.getDescription());

        licenseRepository.save(newLicense);

        licenseHistoryService.createNewRecord("Not activated", "Created new license", user, newLicense);

        return newLicense.getId();
    }

    private String generateUniqueCode(LicenseRepository licenseRepository) {
        String uid;
        do {
            uid = UUID.randomUUID().toString();
        } while (licenseRepository.findByCode(uid).isPresent());
        return uid;
    }





    public Ticket getActiveLicensesForDevice(DBDevice device, String code) {
        List<DBDeviceLicense> applicationDeviceLicensesList = deviceLicenseService.getAllLicenseById(device);
        List<Long> licenseIds = applicationDeviceLicensesList.stream()
                .map(license -> license.getLicense() != null ? license.getLicense().getId() : null)
                .collect(Collectors.toList());
        Optional<DBLicense> applicationLicense = licenseRepository.findByIdInAndCode(licenseIds, code);

        Ticket ticket = new Ticket();

        if (applicationLicense.isEmpty()){
            ticket.setInfo("License was not found");
            ticket.setStatus("Error");
            return ticket;
        }
        ticket = createTicket(applicationLicense.get().getUser(), device, applicationLicense.get(),
                "Info about license", "OK");

        return ticket;
    }


    private String makeSignature(Ticket ticket)  {
        try{
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            ObjectMapper objectMapper = new ObjectMapper();
            String res = objectMapper.writeValueAsString(ticket);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(res.getBytes());

            return Base64.getEncoder().encodeToString(signature.sign());
        }
        catch (Exception e){
            return "Something went wrong. The signature is not valid";
        }
    }

    public Ticket createTicket(ApplicationUser user, DBDevice device, DBLicense license, String info, String status) {
        Ticket ticket = new Ticket();
        ticket.setCurrentDate(new Date());

        if (user != null) {
            ticket.setUserId(user.getId());
        }

        if (device != null) {
            ticket.setDeviceId(device.getId());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        ticket.setLifetime(calendar.getTime());

        if (license != null) {
            ticket.setActivationDate(license.getFirstActivationDate());
            ticket.setExpirationDate(license.getEndingDate());
            ticket.setLicenseBlocked(license.isBlocked());
        }

        ticket.setInfo(info);
        ticket.setDigitalSignature(makeSignature(ticket));
        ticket.setStatus(status);

        return ticket;
    }


    public Ticket activateLicense(String code, DBDevice device, ApplicationUser user) {
        Ticket ticket = new Ticket();
        Optional<DBLicense> license = licenseRepository.findByCode(code);

        if (license.isEmpty()) {
            ticket.setInfo("The license was not found");
            ticket.setStatus("Error");
            deviceServiceImpl.deleteLastDevice(user);
            return ticket;
        }

        DBLicense newLicense = license.get();

        if (newLicense.isBlocked() ||
                newLicense.getEndingDate() != null && new Date().after(newLicense.getEndingDate()) ||
                newLicense.getUser() != null && !Objects.equals(newLicense.getUser().getId(), user.getId()) ||
                deviceLicenseService.getDeviceCountForLicense(newLicense.getId()) >= newLicense.getDeviceCount()) {
            ticket.setInfo("Activation is not possible");
            ticket.setStatus("Error");
            deviceServiceImpl.deleteLastDevice(user);
            return ticket;
        }

        if (newLicense.getFirstActivationDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(newLicense.getDuration()));
            newLicense.setEndingDate(calendar.getTime());
            newLicense.setFirstActivationDate(new Date());
            newLicense.setUser(user);
        }

        deviceLicenseService.createDeviceLicense(newLicense, device);
        licenseRepository.save(newLicense);
        licenseHistoryService.createNewRecord("Activated", "Valid license", user, newLicense);

        return createTicket(user, device, newLicense, "The license has been successfully activated", "OK");
    }


    public String updateLicense(Long id, Long ownerId, Long productId, Long typeId, Boolean isBlocked, String description, Long deviceCount) {
        Optional<DBLicense> license = getLicenseById(id);
        if (license.isEmpty()) {
            return "License Not Found";
        }

        Optional<DBProduct> product = productService.getProductById(productId);
        if (product.isEmpty()) {
            return "Product Not Found";
        }

        Optional<DBLicenseType> licenseType = licenseTypeService.getLicenseTypeById(typeId);
        if (licenseType.isEmpty()) {
            return "License Type Not Found";
        }

        Optional<ApplicationUser> owner = userDetailsServiceImpl.getUserById(ownerId);
        if (owner.isEmpty()) {
            return "Owner Not Found";
        }

        DBLicense newLicense = license.get();
        newLicense.setCode(String.valueOf(UUID.randomUUID()));
        newLicense.setProduct(product.get());
        newLicense.setLicenseType(licenseType.get());
        newLicense.setDuration(licenseType.get().getDefaultDuration());
        newLicense.setBlocked(isBlocked);
        newLicense.setOwnerId(owner.get());
        newLicense.setDescription(description);
        newLicense.setDeviceCount(deviceCount);

        licenseRepository.save(newLicense);

        return "OK";
    }



    public Ticket renewalLicense(String code, ApplicationUser user) {
        Ticket ticket = new Ticket();
        Optional<DBLicense> license = licenseRepository.findByCode(code);
        if (license.isEmpty()) {
            ticket.setInfo("The license key is not valid");
            ticket.setStatus("Error");
            return ticket;
        }

        DBLicense newLicense = license.get();
        if (newLicense.isBlocked() || newLicense.getEndingDate() == null || new Date().after(newLicense.getEndingDate()) || !Objects.equals(newLicense.getOwnerId().getId(), user.getId()) || newLicense.getFirstActivationDate() == null) {
            ticket.setInfo("It is not possible to renew the license");
            ticket.setStatus("Error");
            return ticket;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(newLicense.getEndingDate());
        calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(newLicense.getDuration()));
        newLicense.setEndingDate(calendar.getTime());

        licenseRepository.save(newLicense);
        licenseHistoryService.createNewRecord("Renewal", "Valid license", user, newLicense);

        return createTicket(user, null, newLicense, "The license has been successfully renewed", "OK");
    }

}
