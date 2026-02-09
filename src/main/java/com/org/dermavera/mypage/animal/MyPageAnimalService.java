package com.org.dermavera.mypage.animal;

import com.org.dermavera.entity.Animal;
import com.org.dermavera.entity.User;
import com.org.dermavera.mypage.UserRepository;
import com.org.dermavera.mypage.animal.dto.AnimalRequest;
import com.org.dermavera.mypage.animal.dto.AnimalResponse;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageAnimalService {
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;

    //유저 동물 목록 조회
    public List<AnimalResponse> getMyAnimals(Long userPk) {
        return animalRepository.findAllByUser_UserPk(userPk).stream()
                .map(AnimalResponse::from)
                .toList();
    }

    //특정 동물 조회
    public AnimalResponse getMyAnimal(Long userPk, Long animalId) {
        Animal animal = animalRepository.findByIdAndUser_UserPk(animalId, userPk)
                .orElseThrow(() -> new EntityNotFoundException("내 동물 정보가 없습니다."));
        return AnimalResponse.from(animal);
    }

    //동물 정보 등록
    @Transactional
    public AnimalResponse createMyAnimal(Long userPk, AnimalRequest request) {
        User user = userRepository.findById(userPk)
                .orElseThrow(() -> new EntityNotFoundException("유저가 없습니다."));

        Animal animal = Animal.create(
                user,
                request.getName(),
                request.getAge(),
                request.getSpecies()
        );

        return AnimalResponse.from(animalRepository.save(animal));
    }

    //동물 정보 수정
    @Transactional
    public AnimalResponse updateMyAnimal(Long userPk, Long animalId, AnimalRequest request) {
        Animal animal = animalRepository.findByIdAndUser_UserPk(animalId, userPk)
                .orElseThrow(() -> new EntityNotFoundException("내 동물 정보가 없습니다."));

        animal.update(
                request.getName(),
                request.getAge(),
                request.getSpecies()
        );

        return AnimalResponse.from(animal);
    }

    //동물 정보 삭제
    @Transactional
    public void deleteMyAnimal(Long userPk, Long animalId) {
        Animal animal = animalRepository.findByIdAndUser_UserPk(animalId, userPk)
                .orElseThrow(() -> new EntityNotFoundException("내 동물 정보가 없습니다."));
        animalRepository.delete(animal);
    }
}
