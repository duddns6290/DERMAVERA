package com.org.dermavera.auth;

import com.org.dermavera.auth.dto.LoginRequest;
import com.org.dermavera.auth.dto.SignupRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    // 카카오 로그인 콜백 처리
    @GetMapping("/kakao/callback")
    public void kakaoCallback(
            @RequestParam String code,
            HttpServletResponse response
    ) throws IOException {
        String jwt = authService.kakaoLoginWithCode(code);
        String frontendUrl = authService.getFrontendUrl();
        response.sendRedirect(frontendUrl + "/?token=" + jwt);
    }

    @GetMapping("/kakao/login")
    public void kakaoLoginRedirect(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl =
                "https://kauth.kakao.com/oauth/authorize" +
                        "?response_type=code" +
                        "&client_id=916c1a9d07a09835090dac879922caaf" +
                        "&redirect_uri=http://localhost:8080/auth/kakao/callback";

        response.sendRedirect(kakaoAuthUrl);
    }

}

