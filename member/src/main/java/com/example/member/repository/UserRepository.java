package com.example.member.repository;

import com.example.member.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserMail(String userMail);

    boolean existsByUserMail(String userMail);

    boolean existsByUserPhoneNumber(String userPhoneNumber);
}

