package com.org.dermavera.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DiagnoseResponse {

    private Long diagnosisId;
    private String requestId;
    private String topLabel;
    private Double score;
    private List<PredictionItem> predictions;
    private int processingMs;
    private String imageFilename;

    public static DiagnoseResponse from(SkinAiDiagnoseResponse ai, Long savedDiagnosisId) {
        double topScore = ai.getPredictions().isEmpty()
                ? 0.0
                : ai.getPredictions().get(0).getScore();

        return DiagnoseResponse.builder()
                .diagnosisId(savedDiagnosisId)
                .requestId(ai.getRequest_id())
                .topLabel(ai.getTop_label())
                .score(topScore)
                .predictions(ai.getPredictions().stream()
                        .map(p -> new PredictionItem(p.getLabel(), p.getScore()))
                        .collect(Collectors.toList()))
                .processingMs(ai.getProcessing_ms())
                .imageFilename(ai.getInput() != null ? ai.getInput().getFilename() : null)
                .build();
    }

    @Getter
    @AllArgsConstructor
    public static class PredictionItem {
        private final String label;
        private final double score;
    }
}
