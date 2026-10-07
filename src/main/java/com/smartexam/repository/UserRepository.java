package com.smartexam.repository;

import com.smartexam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartexam.entity.Role;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}
