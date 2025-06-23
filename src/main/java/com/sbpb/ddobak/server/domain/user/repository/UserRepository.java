package com.sbpb.ddobak.server.domain.user.repository;

import com.sbpb.ddobak.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 삭제되지 않은 사용자 조회 (이메일)
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * 삭제되지 않은 사용자 조회 (ID)
     */
    Optional<User> findByIdAndIsDeletedFalse(String id);
} 