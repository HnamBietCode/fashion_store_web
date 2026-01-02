package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(String fullName, String email, String password) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng! VUi lòng sử dụng Email khác nhé.");
        }

        // Tạo user mới với password được mã hóa
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))  // Mã hóa password
                .role(User.Role.CUSTOMER)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}