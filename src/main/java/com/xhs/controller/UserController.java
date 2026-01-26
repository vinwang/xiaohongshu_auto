package com.xhs.controller;

import com.xhs.dto.UserDto;
import com.xhs.entity.User;
import com.xhs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    // 获取所有用户
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    // 获取单个用户
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> ResponseEntity.ok(convertToDto(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 创建用户
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        User user = convertToEntity(userDto);
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(createdUser));
    }

    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        User userDetails = convertToEntity(userDto);
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // 设置当前用户
    @PutMapping("/{id}/current")
    public ResponseEntity<UserDto> setCurrentUser(@PathVariable Long id) {
        User currentUser = userService.setCurrentUser(id);
        return ResponseEntity.ok(convertToDto(currentUser));
    }

    // 获取当前用户
    @GetMapping("/current")
    public ResponseEntity<UserDto> getCurrentUser() {
        Optional<User> currentUser = userService.getCurrentUser();
        return currentUser.map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 更新用户登录状态
    @PutMapping("/{id}/login")
    public ResponseEntity<UserDto> updateLoginStatus(@PathVariable Long id, @RequestParam boolean isLoggedIn) {
        User updatedUser = userService.updateUserLoginStatus(id, isLoggedIn);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    // 获取已登录用户
    @GetMapping("/logged-in")
    public ResponseEntity<UserDto> getLoggedInUser() {
        Optional<User> loggedInUser = userService.getLoggedInUser();
        return loggedInUser.map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 转换实体到DTO
    private UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    // 转换DTO到实体
    private User convertToEntity(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}