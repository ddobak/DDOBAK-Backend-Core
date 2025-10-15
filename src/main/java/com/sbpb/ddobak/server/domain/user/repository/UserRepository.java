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
     * 이메일로 활성 사용자 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 이메일 존재 여부 확인 (활성 사용자만)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.isDeleted = false")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Apple ID로 활성 사용자 조회 (삭제되지 않은 사용자만)
     * @param appleId Apple OAuth Provider ID
     * @return 사용자 Optional
     */
    @Query("SELECT u FROM User u WHERE u.oauthProvider = 'apple' AND u.oauthProviderId = :appleId AND u.isDeleted = false")
    Optional<User> findByAppleId(@Param("appleId") String appleId);

    /**
     * Apple ID로 사용자 조회 (삭제 여부 무관, 재가입 처리용)
     * @param appleId Apple OAuth Provider ID
     * @return 사용자 Optional
     */
    @Query("SELECT u FROM User u WHERE u.oauthProvider = 'apple' AND u.oauthProviderId = :appleId")
    Optional<User> findByAppleIdIncludingDeleted(@Param("appleId") String appleId);

    /**
     * OAuth 제공자와 Provider ID로 사용자 조회
     * @param provider OAuth 제공자 (apple, google, kakao 등)
     * @param providerId OAuth Provider ID
     * @return 사용자 Optional
     */
    Optional<User> findByOauthProviderAndOauthProviderId(String provider, String providerId);
} 