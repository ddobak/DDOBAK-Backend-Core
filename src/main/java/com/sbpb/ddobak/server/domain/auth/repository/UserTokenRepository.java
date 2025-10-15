package com.sbpb.ddobak.server.domain.auth.repository;

import com.sbpb.ddobak.server.domain.auth.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 토큰 레포지토리
 * 리프레시 토큰 관리를 위한 데이터 액세스
 */
@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    
    /**
     * 사용자 ID로 토큰 조회
     * @param userId 사용자 ID
     * @return 사용자 토큰 (Optional)
     */
    Optional<UserToken> findByUserId(Long userId);
    
    /**
     * 리프레시 토큰으로 토큰 조회
     * @param refreshToken 리프레시 토큰
     * @return 사용자 토큰 (Optional)
     */
    Optional<UserToken> findByRefreshToken(String refreshToken);
    
    /**
     * 사용자 ID로 토큰 삭제
     * @param userId 사용자 ID
     */
    @Modifying
    void deleteByUserId(Long userId);
}
