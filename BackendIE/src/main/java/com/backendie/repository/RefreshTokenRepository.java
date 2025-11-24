package com.backendie.repository;

import com.backendie.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Devuelve token solo si no está revocado
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    List<RefreshToken> findAllByUserId(Long userId);

    List<RefreshToken> findAllByUserIdAndRevokedFalse(Long userId);

    @Modifying
    @Transactional
    @Query("delete from RefreshToken r where r.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("delete from RefreshToken r where r.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}

