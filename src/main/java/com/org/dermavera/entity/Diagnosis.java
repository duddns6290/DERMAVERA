package com.org.dermavera.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "diagnoses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Column
    private String status;

    @Column
    private Double score;

    @Column
    private String image;

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    public static Diagnosis create(Animal animal, String status, Double score, String image) {
        Diagnosis d = new Diagnosis();
        d.animal = animal;
        d.status = status;
        d.score = score;
        d.image = image;
        return d;
    }

    public void update(String status, Double score, String image) {
        this.status = status;
        this.score = score;
        this.image = image;
    }
}
