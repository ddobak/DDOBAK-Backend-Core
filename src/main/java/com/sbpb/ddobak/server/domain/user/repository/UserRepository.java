package com.sbpb.ddobak.server.domain.user.repository;

import com.sbpb.ddobak.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 레포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * Apple ID로 사용자 조회
     * @param appleId Apple OAuth Provider ID
     * @return 사용자 Optional
     */
    @Query("SELECT u FROM User u WHERE u.oauthProvider = 'apple' AND u.oauthProviderId = :appleId")
    Optional<User> findByAppleId(@Param("appleId") String appleId);

    /**
     * OAuth 제공자와 Provider ID로 사용자 조회
     * @param provider OAuth 제공자 (apple, google, kakao 등)
     * @param providerId OAuth Provider ID
     * @return 사용자 Optional
     */
    Optional<User> findByOauthProviderAndOauthProviderId(String provider, String providerId);
} 