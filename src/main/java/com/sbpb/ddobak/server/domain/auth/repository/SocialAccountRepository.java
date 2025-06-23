package com.sbpb.ddobak.server.domain.auth.repository;

import com.sbpb.ddobak.server.domain.auth.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 소셜 계정 Repository
 */
@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, String> {

    /**
     * Provider와 Provider User ID로 소셜 계정 조회
     */
    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    /**
     * 사용자 ID로 소셜 계정 목록 조회
     */
    List<SocialAccount> findByUserId(String userId);

    /**
     * Provider와 이메일로 소셜 계정 조회
     */
    Optional<SocialAccount> findByProviderAndEmail(String provider, String email);

    /**
     * 사용자 ID와 Provider로 소셜 계정 조회
     */
    Optional<SocialAccount> findByUserIdAndProvider(String userId, String provider);
} 