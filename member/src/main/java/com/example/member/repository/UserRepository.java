package com.example.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.example.member.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
