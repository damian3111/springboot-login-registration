package com.example.project.repository;

import com.example.project.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE ConfirmationToken c SET c.confirmedAt = ?1 WHERE c.id = ?2")
    int updateConfirmedAt(LocalDateTime localDateTime, Long id);

    @Query("Select c FROM ConfirmationToken c JOIN FETCH c.appUser u WHERE u.email = ?1 ORDER BY c.createdAt DESC LIMIT 1")
    ConfirmationToken findLatestCreatedConfirmationToken(String email);

}
