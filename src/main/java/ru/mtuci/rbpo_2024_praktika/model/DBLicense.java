package ru.mtuci.rbpo_2024_praktika.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Entity
@Table(name = "license")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DBLicense {

    @Id
    @GeneratedValue
    private Long id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "userId")
    private ApplicationUser user;

    @ManyToOne
    @JoinColumn(name = "productId")
    private DBProduct product;

    @ManyToOne
    @JoinColumn(name = "typeId")
    private DBLicenseType licenseType;

    private Date firstActivationDate;

    private Date endingDate;

    private boolean blocked;

    private int deviceCount;

    private Long ownerId;

    private int duration;

    private String description;
}