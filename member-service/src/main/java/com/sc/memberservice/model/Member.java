package com.sc.memberservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "members")
public class Member {
    @Id
    private String memberId = UUID.randomUUID().toString();
    private String email;
    private String username;
    private String password;
}
