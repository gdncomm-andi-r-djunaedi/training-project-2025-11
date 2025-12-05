package com.blibli.member.repository;


import com.blibli.member.entity.UserRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRegisterRepository extends JpaRepository<UserRegister,String> {
    @Query(value = "SELECT COUNT(u)>0 FROM users u WHERE user_email =:userEmail",nativeQuery = true)
    boolean isexistByEmail(String userEmail);
    @Query(value = "SELECT u.password FROM users u WHERE user_email =:userEmail",nativeQuery = true)
    String getPasswordByUserName(String userEmail);
}
