package com.authguard.repository;

import com.authguard.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByKeycloakUserId(String keycloakUserId);
}
