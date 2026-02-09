package com.org.dermavera.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoUserResponse {

    private Long id; // 카카오 고유 ID

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    public static class KakaoAccount {

        private Profile profile;
        private String email;

        @Getter
        public static class Profile {
            private String nickname;
        }
    }
}
