package com.org.dermavera.diagnosis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * skin-ai POST /v1/diagnose 응답 구조.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkinAiDiagnoseResponse {

    private String request_id;
    private ModelInfo model;
    private DiagnoseInput input;
    private String top_label;
    private List<Prediction> predictions;
    private int processing_ms;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelInfo {
        private String name;
        private String version;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiagnoseInput {
        private String filename;
        private String animal_type;
        private String body_part;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Prediction {
        private String label;
        private double score;
    }
}
