package com.org.dermavera.diagnosis;

import com.org.dermavera.diagnosis.dto.DiagnoseResponse;
import com.org.dermavera.diagnosis.dto.DiagnosisHistoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/users/{userPk}/animals/{animalId}")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping("/diagnose")
    public DiagnoseResponse diagnose(
            @PathVariable Long userPk,
            @PathVariable Long animalId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "animal_type", required = false) String animalType,
            @RequestParam(value = "body_part", required = false) String bodyPart,
            @RequestParam(value = "top_k", defaultValue = "3") int topK) throws Exception {

        return diagnosisService.diagnose(userPk, animalId, image, animalType, bodyPart, topK);
    }

    /** 해당 동물의 진단 이력 목록 (최신순) */
    @GetMapping("/diagnoses")
    public List<DiagnosisHistoryItem> getDiagnosesByAnimal(
            @PathVariable Long userPk,
            @PathVariable Long animalId) {
        return diagnosisService.getDiagnosesByAnimal(userPk, animalId);
    }
}
