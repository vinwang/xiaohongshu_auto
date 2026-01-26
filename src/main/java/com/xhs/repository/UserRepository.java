package com.xhs.repository;

import com.xhs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    Optional<User> findByIsCurrentTrue();
    Optional<User> findByIsLoggedInTrue();
    long countByIsActiveTrue();
}
