package com.test.backend.repository;

import com.test.backend.entity.TokenBlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlackList,Integer> {
    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM TokenBlackList t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
