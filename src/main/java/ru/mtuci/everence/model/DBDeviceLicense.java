package ru.mtuci.everence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "device_license")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DBDeviceLicense {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "licenseId")
    private DBLicense license;

    @ManyToOne
    @JoinColumn(name = "deviceId")
    private DBDevice device;

    private Date activateDate;
}
