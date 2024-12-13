package ru.mtuci.everence.model;

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

    private Long deviceCount;

    @ManyToOne
    @JoinColumn(name = "ownerId", referencedColumnName = "id")
    private ApplicationUser ownerId;

    private Long duration;

    private String description;
}

