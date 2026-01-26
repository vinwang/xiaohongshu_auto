package com.xhs.service;

import com.xhs.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByPhone(String phone);
    User createUser(User user);
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    User setCurrentUser(Long id);
    Optional<User> getCurrentUser();
    User updateUserLoginStatus(Long id, boolean isLoggedIn);
    Optional<User> getLoggedInUser();
    long count();
    long countActiveUsers();
}