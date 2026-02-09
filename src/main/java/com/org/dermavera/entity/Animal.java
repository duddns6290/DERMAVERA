package com.org.dermavera.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "animals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "animal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_pk", nullable = false)
    private User user;

    @Column(name = "animal_name", nullable = false)
    private String name;

    @Column(name = "animal_age")
    private Integer age;

    @Column(name = "animal_species")
    private String species;

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    public void update(String name, Integer age, String species) {
        this.name = name;
        this.age = age;
        this.species = species;
    }

    public static Animal create(User user, String name, Integer age, String species) {
        Animal animal = new Animal();
        animal.user = user;
        animal.name = name;
        animal.age = age;
        animal.species = species;
        return animal;
    }

}
