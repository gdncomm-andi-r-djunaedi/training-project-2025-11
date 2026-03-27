package com.example.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "user_mail", nullable = false, unique = true)
    private String userMail;
    @Column(name = "user_phone_number", nullable = false, unique = true)
    private String userPhoneNumber;
    @Column(name = "password", nullable = false)
    private String password;

}

