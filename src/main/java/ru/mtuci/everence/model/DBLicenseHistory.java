package ru.mtuci.everence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "license_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DBLicenseHistory {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "licenseId")
    private DBLicense license;

    @ManyToOne
    @JoinColumn(name = "userId")
    private ApplicationUser user;

    private String status;

    private Date changeDate;

    private String description;
}

