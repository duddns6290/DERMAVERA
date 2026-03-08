package com.org.dermavera.diagnosis;

import com.org.dermavera.diagnosis.dto.DiagnoseResponse;
import com.org.dermavera.diagnosis.dto.DiagnosisHistoryItem;
import com.org.dermavera.diagnosis.dto.SkinAiDiagnoseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.dermavera.entity.Animal;
import com.org.dermavera.entity.Diagnosis;
import com.org.dermavera.mypage.animal.AnimalRepository;
import com.org.dermavera.repository.DiagnosisRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisService.class);

    @Qualifier("skinAiWebClient")
    private final WebClient skinAiWebClient;

    @Value("${skin.ai.internal-token:}")
    private String internalToken;

    private final AnimalRepository animalRepository;
    private final DiagnosisRepository diagnosisRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public DiagnoseResponse diagnose(Long userPk, Long animalId, MultipartFile image,
                                     String animalType, String bodyPart, int topK) throws IOException {

        log.info("diagnose start: userPk={}, animalId={}, image={}, animal_type={}, body_part={}, top_k={}",
                userPk, animalId, image != null ? image.getSize() : 0, animalType, bodyPart, topK);

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 선택해 주세요. (form 필드명은 반드시 'image'로 보내주세요.)");
        }

        Animal animal = animalRepository.findByIdAndUser_UserPk(animalId, userPk)
                .orElseThrow(() -> {
                    log.warn("동물 없음: userPk={}, animalId={}", userPk, animalId);
                    return new EntityNotFoundException("내 동물 정보가 없습니다.");
                });

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        byte[] imageBytes = image.getInputStream().readAllBytes();
        String filename = image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
        MediaType imageContentType;
        try {
            String contentType = image.getContentType();
            imageContentType = (contentType != null && !contentType.isBlank())
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.IMAGE_JPEG;
        } catch (Exception e) {
            imageContentType = MediaType.IMAGE_JPEG;
        }
        builder.part("image", new ByteArrayResource(imageBytes))
                .contentType(imageContentType)
                .filename(filename);
        if (animalType != null && !animalType.isBlank()) {
            builder.part("animal_type", animalType);
        }
        if (bodyPart != null && !bodyPart.isBlank()) {
            builder.part("body_part", bodyPart);
        }
        builder.part("top_k", String.valueOf(Math.max(1, Math.min(topK, 10))));

        var spec = skinAiWebClient.post()
                .uri("/v1/diagnose")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()));

        if (internalToken != null && !internalToken.isBlank()) {
            spec = spec.header("X-Internal-Token", internalToken);
        }

        SkinAiDiagnoseResponse aiResponse;
        try {
            aiResponse = spec
                    .retrieve()
                    .onStatus(sc -> sc.value() >= 400 && sc.value() < 500, res -> res.bodyToMono(String.class)
                            .map(body -> new SkinAiUnavailableException("skin-ai 4xx: " + res.statusCode() + " " + body)))
                    .onStatus(sc -> sc.value() >= 500, res -> res.bodyToMono(String.class)
                            .map(body -> new SkinAiUnavailableException("skin-ai 5xx: " + res.statusCode() + " " + body)))
                    .bodyToMono(SkinAiDiagnoseResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("skin-ai 호출 실패: userPk={}, animalId={}, error={}", userPk, animalId, e.getMessage(), e);
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof SkinAiUnavailableException) {
                throw (SkinAiUnavailableException) cause;
            }
            throw new SkinAiUnavailableException("진단 서비스를 일시적으로 사용할 수 없습니다. (연결 실패 또는 타임아웃)", e);
        }

        if (aiResponse == null) {
            log.error("skin-ai 응답 null: userPk={}, animalId={}", userPk, animalId);
            throw new SkinAiUnavailableException("진단 서비스 응답이 없습니다.");
        }

        double topScore = aiResponse.getPredictions().isEmpty()
                ? 0.0
                : aiResponse.getPredictions().get(0).getScore();

        Diagnosis diagnosis = Diagnosis.create(
                animal,
                aiResponse.getTop_label(),
                topScore,
                image.getOriginalFilename() != null ? image.getOriginalFilename() : "image"
        );
        diagnosis = diagnosisRepository.save(diagnosis);
        log.info("diagnose 완료: diagnosisId={}, topLabel={}, processingMs={}", diagnosis.getId(), aiResponse.getTop_label(), aiResponse.getProcessing_ms());

        return DiagnoseResponse.from(aiResponse, diagnosis.getId());
    }

    /**
     * 유저 전체 진단 이력 (최신순).
     * 해당 userPk의 동물들에 대한 모든 진단 기록.
     */
    @Transactional(readOnly = true)
    public List<DiagnosisHistoryItem> getDiagnosesByUser(Long userPk) {
        List<Diagnosis> list = diagnosisRepository.findAllByAnimal_User_UserPkOrderByCreatedDateDesc(userPk);
        return list.stream().map(this::toHistoryItem).collect(Collectors.toList());
    }

    /**
     * 특정 동물의 진단 이력 (최신순).
     * 동물이 해당 userPk 소유인지 검증 후 반환.
     */
    @Transactional(readOnly = true)
    public List<DiagnosisHistoryItem> getDiagnosesByAnimal(Long userPk, Long animalId) {
        Animal animal = animalRepository.findByIdAndUser_UserPk(animalId, userPk)
                .orElseThrow(() -> new EntityNotFoundException("내 동물 정보가 없습니다."));
        List<Diagnosis> list = diagnosisRepository.findAllByAnimal_IdOrderByCreatedDateDesc(animal.getId());
        return list.stream().map(this::toHistoryItem).collect(Collectors.toList());
    }

    private DiagnosisHistoryItem toHistoryItem(Diagnosis d) {
        return DiagnosisHistoryItem.builder()
                .diagnosisId(d.getId())
                .animalId(d.getAnimal().getId())
                .animalName(d.getAnimal().getName())
                .topLabel(d.getStatus())
                .score(d.getScore())
                .predictions(Collections.emptyList())
                .imageFilename(d.getImage())
                .createdDate(d.getCreatedDate())
                .build();
    }

    private List<DiagnoseResponse.PredictionItem> parsePredictions(String predictionsJson) {
        if (predictionsJson == null || predictionsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<SkinAiDiagnoseResponse.Prediction> list = objectMapper.readValue(
                    predictionsJson,
                    new TypeReference<List<SkinAiDiagnoseResponse.Prediction>>() {});
            return list.stream()
                    .map(p -> new DiagnoseResponse.PredictionItem(p.getLabel(), p.getScore()))
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.warn("predictions JSON 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
