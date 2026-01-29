package com.org.dermavera.mypage.animal;

import com.org.dermavera.mypage.animal.dto.AnimalRequest;
import com.org.dermavera.mypage.animal.dto.AnimalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/users/{userPk}/animals")
public class MyPageAnimalController {

    private final MyPageAnimalService myPageAnimalService;
    //유저가 등록한 모든 동물 조회
    @GetMapping
    public List<AnimalResponse> getMyAnimals(@PathVariable Long userPk) {
        return myPageAnimalService.getMyAnimals(userPk);
    }

    //특정 동물 조회
    @GetMapping("/{animalId}")
    public AnimalResponse getMyAnimal(
            @PathVariable Long userPk,
            @PathVariable Long animalId) {
        return myPageAnimalService.getMyAnimal(userPk, animalId);
    }

    //동물 정보 등록
    @PostMapping
    public AnimalResponse createMyAnimal(
            @PathVariable Long userPk,
            @RequestBody AnimalRequest request) {
        return myPageAnimalService.createMyAnimal(userPk, request);
    }

    //동물 정보 수정
    @PutMapping("/{animalId}")
    public AnimalResponse updateMyAnimal(
            @PathVariable Long userPk,
            @PathVariable Long animalId,
            @RequestBody AnimalRequest request) {
        return myPageAnimalService.updateMyAnimal(userPk, animalId, request);
    }

    //동물 정보 삭제
    @DeleteMapping("/{animalId}")
    public void deleteMyAnimal(
            @PathVariable Long userPk,
            @PathVariable Long animalId) {
        myPageAnimalService.deleteMyAnimal(userPk, animalId);
    }
}
