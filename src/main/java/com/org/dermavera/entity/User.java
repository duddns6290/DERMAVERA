package com.org.dermavera.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_provider",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_pk")
    private Long userPk;

    // 일반 로그인 ID (소셜 로그인 시 NULL 가능)
    @Column(name = "user_id", unique = true)
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    // 일반 로그인만 사용
    @Column(name = "password")
    private String password;

    // KAKAO
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20)
    private SocialType provider;

    // 카카오에서 내려주는 고유 ID
    @Column(name = "provider_id", length = 100)
    private String providerId;
}
