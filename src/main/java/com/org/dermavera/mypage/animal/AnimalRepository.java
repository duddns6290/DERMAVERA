package com.org.dermavera.mypage.animal;

import com.org.dermavera.entity.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    //유저 동물 목록 조회
    List<Animal> findAllByUser_UserPk(Long userPk);
    //특정 동물 조회
    Optional<Animal> findByIdAndUser_UserPk(Long animalId, Long userPk);
}
