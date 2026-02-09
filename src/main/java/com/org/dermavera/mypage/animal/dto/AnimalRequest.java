package com.org.dermavera.mypage.animal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AnimalRequest {
    @NotBlank
    private String name;
    private Integer age;
    private String species;
}
