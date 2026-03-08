package com.org.dermavera.user;

import com.org.dermavera.entity.User;
import com.org.dermavera.repository.UserRepository;
import com.org.dermavera.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getMyInfo(String subject) {
        Long userPk = Long.parseLong(subject);
        User user = userRepository.findById(userPk)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        return new UserResponse(user);
    }
}
