package com.gdn.training.apigateway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "members")
@Data
public class Member {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "last_logout")
    private Date lastLogout;

    public Member() {
    }
}
