package com.studentmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.studentmanagement.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken>findByRefreshToken(String refreshToken);

    @Modifying 
    @Query("update RefreshToken r set r.revoked=true where r.user.id=:userId and r.revoked=false")
    void revokeRefreshToken(Long userId);
}
