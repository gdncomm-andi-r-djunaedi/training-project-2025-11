package com.blibli.AuthService.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    private String jti;

    private Long expiryEpochMillis;

}
