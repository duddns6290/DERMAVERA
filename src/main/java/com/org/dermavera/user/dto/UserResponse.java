package com.org.dermavera.user.dto;

import com.org.dermavera.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {

    private Long userPk;
    private String userId;
    private String userName;

    public UserResponse(User user) {
        this.userPk = user.getUserPk();
        this.userId = user.getUserId();
        this.userName = user.getUserName();
    }
}
