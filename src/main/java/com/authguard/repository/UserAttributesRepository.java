package com.authguard.repository;

import com.authguard.entity.UserAttributes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAttributesRepository extends JpaRepository<UserAttributes, String> {
}
