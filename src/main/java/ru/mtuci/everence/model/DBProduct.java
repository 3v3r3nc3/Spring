package ru.mtuci.everence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DBProduct {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private boolean isBlocked;
}

