package com.fashionstore.fashion_store.repository;

import com.fashionstore.fashion_store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA tự động tạo query từ tên method!
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
}