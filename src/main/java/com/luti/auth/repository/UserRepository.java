package com.luti.auth.repository;

import com.luti.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserId(Long userId);

    boolean existsByEmail(String email);

    User findByUserId(Long userId);

    User findByEmail(String username);

}