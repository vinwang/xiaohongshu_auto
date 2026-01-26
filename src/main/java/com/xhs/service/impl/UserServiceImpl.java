package com.xhs.service.impl;

import com.xhs.entity.User;
import com.xhs.repository.UserRepository;
import com.xhs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        // 检查手机号是否已存在
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("手机号已被使用");
        }
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已被使用");
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在") {{
                }});

        // 更新用户信息
        if (userDetails.getUsername() != null) {
            // 检查新用户名是否已被其他用户使用
            Optional<User> existingUser = userRepository.findByUsername(userDetails.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new IllegalArgumentException("用户名已被使用");
            }
            user.setUsername(userDetails.getUsername());
        }

        if (userDetails.getPhone() != null) {
            // 检查新手机号是否已被其他用户使用
            Optional<User> existingUser = userRepository.findByPhone(userDetails.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new IllegalArgumentException("手机号已被使用");
            }
            user.setPhone(userDetails.getPhone());
        }

        if (userDetails.getDisplayName() != null) {
            user.setDisplayName(userDetails.getDisplayName());
        }

        if (userDetails.getIsActive() != null) {
            user.setIsActive(userDetails.getIsActive());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在") {{
                }});
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public User setCurrentUser(Long id) {
        // 将所有用户的isCurrent设置为false
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            user.setIsCurrent(false);
        }
        userRepository.saveAll(allUsers);

        // 将指定用户的isCurrent设置为true
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在") {{
                }});
        user.setIsCurrent(true);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getCurrentUser() {
        return userRepository.findByIsCurrentTrue();
    }

    @Override
    @Transactional
    public User updateUserLoginStatus(Long id, boolean isLoggedIn) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在") {{
                }});
        user.setIsLoggedIn(isLoggedIn);
        if (isLoggedIn) {
            user.setLastLoginAt(LocalDateTime.now());
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getLoggedInUser() {
        return userRepository.findByIsLoggedInTrue();
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public long countActiveUsers() {
        return userRepository.countByIsActiveTrue();
    }
}