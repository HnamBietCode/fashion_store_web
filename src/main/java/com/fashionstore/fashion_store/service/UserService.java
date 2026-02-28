package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User registerUser(String fullName, String email, String password);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    List<User> getAllUsers();

    User toggleUserActive(Long id);

    User updateProfile(Long userId, String fullName, String phone, String address);

    boolean changePassword(Long userId, String oldPassword, String newPassword);
}