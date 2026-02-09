//package com.org.dermavera.mypage;
//
//import com.org.dermavera.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//
////테스트용 db에 있는 fk 제약때문에 나중에 로그인된 곳에서 받아오는 방식으로 변경예정
//public interface UserRepository extends JpaRepository<User, Long> {
//    default User getTestUser() {
//        return findById(1L)
//                .orElseThrow(() -> new IllegalStateException("테스트 유저가 없습니다."));
//    }
//}
