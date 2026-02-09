package com.org.dermavera.auth;

import com.org.dermavera.auth.dto.KakaoUserResponse;
import com.org.dermavera.auth.dto.LoginRequest;
import com.org.dermavera.auth.dto.SignupRequest;
import com.org.dermavera.config.JwtTokenProvider;
import com.org.dermavera.entity.SocialType;
import com.org.dermavera.entity.User;
import com.org.dermavera.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.org.dermavera.auth.dto.KakaoTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    public void signup(SignupRequest request) {

        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .userId(request.getUserId())
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(SocialType.LOCAL)
                .build();

        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        return jwtTokenProvider.createToken(user.getUserId());
    }

    public KakaoTokenResponse getKakaoAccessToken(String code) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", kakaoRedirectUri);
        formData.add("code", code);
        formData.add("client_secret", kakaoClientSecret);

        return webClient.post()
                .uri(kakaoTokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

    public KakaoUserResponse getKakaoUserInfo(String accessToken) {

        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoUserResponse.class)
                .block();
    }

    public String kakaoLogin(String accessToken) {

        KakaoUserResponse userInfo = getKakaoUserInfo(accessToken);

        String kakaoId = String.valueOf(userInfo.getId());
        String nickname = userInfo.getKakaoAccount()
                .getProfile()
                .getNickname();

        User user = userRepository
                .findByProviderAndProviderId(SocialType.KAKAO, kakaoId)
                .orElseGet(() -> {

                    User newUser = User.builder()
                            .userName(nickname)
                            .provider(SocialType.KAKAO)
                            .providerId(kakaoId)
                            .build();

                    return userRepository.save(newUser);
                });

        return jwtTokenProvider.createToken(
                user.getUserPk().toString()
        );
    }
}