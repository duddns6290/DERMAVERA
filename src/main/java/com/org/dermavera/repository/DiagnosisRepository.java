package com.org.dermavera.repository;

import com.org.dermavera.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    List<Diagnosis> findAllByAnimal_IdOrderByCreatedDateDesc(Long animalId);

    /** 유저 전체 진단 이력 (최신순) */
    List<Diagnosis> findAllByAnimal_User_UserPkOrderByCreatedDateDesc(Long userPk);
}
