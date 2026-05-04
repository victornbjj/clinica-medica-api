package com.clinic_api.repository;

import com.clinic_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User,UUID> {

    Optional<User> findByEmail(String email);

}
