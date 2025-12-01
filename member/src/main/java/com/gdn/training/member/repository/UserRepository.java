package com.gdn.training.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.training.member.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
