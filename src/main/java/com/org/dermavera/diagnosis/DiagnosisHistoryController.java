package com.org.dermavera.diagnosis;

import com.org.dermavera.diagnosis.dto.DiagnosisHistoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 유저 전체 진단 이력 API.
 * GET /api/mypage/users/{userPk}/diagnoses
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/users/{userPk}")
public class DiagnosisHistoryController {

    private final DiagnosisService diagnosisService;

    /** 유저의 전체 진단 이력 목록 (최신순) */
    @GetMapping("/diagnoses")
    public List<DiagnosisHistoryItem> getDiagnosesByUser(@PathVariable Long userPk) {
        return diagnosisService.getDiagnosesByUser(userPk);
    }
}
