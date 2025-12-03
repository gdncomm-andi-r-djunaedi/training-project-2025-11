package com.microservice.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(
        name = "members",
        indexes = {
                @Index(name = "idx_members_email", columnList = "email"),
                @Index(name = "idx_members_phone", columnList = "phone_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_members_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uc_members_phone", columnNames = {"phone_number"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true, length = 255)
    private String phoneNumber;

    @Column(name = "address", nullable = true,  length = 255)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

}
