package com.gdn.training.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "members")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Member {

    // what a member needs
    // - id : identifier
    // - username : to login, must be unique
    // - email : to register, must be unique
    // - password : for both, how do i add validation for characters? how do i hash
    // the password?
    // - token : to save token created after login to access login-only feature
    // - createddate etc : looks cool, also because it exists in actual member data
    // usually (scrap if too much work)

    // things to do:
    // - how to verify register validation?
    // - unique username, email, password format
    // - how to hash password?
    // - how to make login?
    // - how to do tokens?

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, name = "hash_password")
    private String hashPassword;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_date")
    private Date createdDate;

    @CreatedBy
    @Column(nullable = false, updatable = false, name = "created_by")
    private String createdBy;

    @LastModifiedDate
    @Column(nullable = true, name = "updated_date")
    private Date updatedDate;

    @LastModifiedBy
    @Column(nullable = true, name = "updated_by")
    private String updatedBy;

    @Column(nullable = true, name = "last_logout")
    private Date lastLogout;

    public Member() {
    }

    public Member(String username, String email, String hashPassword) {
        this.username = username;
        this.email = email;
        this.hashPassword = hashPassword;
        this.createdDate = new Date();
        this.createdBy = "SYSTEM";
    }
}