package com.sbpb.ddobak.server.domain.user.repository;

import com.sbpb.ddobak.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository
 * 사용자 데이터 접근을 위한 인터페이스.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 애플 ID로 사용자 조회
     * @param appleId 애플에서 제공하는 고유 ID
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByAppleId(String appleId);

    /**
     * 이메일로 사용자 조회
     * @param email 사용자 이메일
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 애플 ID로 사용자 존재 여부 확인
     * @param appleId 애플에서 제공하는 고유 ID
     * @return 존재 여부
     */
    boolean existsByAppleId(String appleId);

    /**
     * 이메일로 사용자 존재 여부 확인
     * @param email 사용자 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 활성 상태인 사용자를 애플 ID로 조회
     * @param appleId 애플에서 제공하는 고유 ID
     * @return 활성 사용자 정보 (Optional)
     */
    @Query("SELECT u FROM User u WHERE u.appleId = :appleId AND u.status = 'ACTIVE'")
    Optional<User> findActiveUserByAppleId(@Param("appleId") String appleId);

    /**
     * 활성 상태인 사용자를 이메일로 조회
     * @param email 사용자 이메일
     * @return 활성 사용자 정보 (Optional)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = 'ACTIVE'")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
} 