package com.blibli.AuthService.repository;

import com.blibli.AuthService.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    @Modifying
    @Query("delete from RevokedToken r where r.expiryEpochMillis < :now")
    void deleteExpired(@Param("now") Long now);
}
