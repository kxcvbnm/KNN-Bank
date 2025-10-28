package com.knn.knnbank.role.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.knn.knnbank.role.entity.Role;

public interface RoleRepo extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
