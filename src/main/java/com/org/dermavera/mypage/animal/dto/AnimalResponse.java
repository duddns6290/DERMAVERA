package com.org.dermavera.mypage.animal.dto;

import com.org.dermavera.entity.Animal;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnimalResponse {
    private Long id;
    private String name;
    private Integer age;
    private String species;
    private LocalDateTime createdDate;

    public static AnimalResponse from(Animal a) {
        return AnimalResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .age(a.getAge())
                .species(a.getSpecies())
                .createdDate(a.getCreatedDate())
                .build();
    }
}
