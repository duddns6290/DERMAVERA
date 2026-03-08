package com.org.dermavera.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 진단 이력 목록 조회용 DTO.
 * GET /api/mypage/users/{userPk}/diagnoses 또는
 * GET /api/mypage/users/{userPk}/animals/{animalId}/diagnoses 응답 항목.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisHistoryItem {

    private Long diagnosisId;
    private Long animalId;
    private String animalName;
    private String topLabel;
    private Double score;
    private List<DiagnoseResponse.PredictionItem> predictions;
    private String imageFilename;
    private LocalDateTime createdDate;
}
