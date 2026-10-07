package com.smartexam.repository;

import com.smartexam.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByUserIdAndSubmittedAtNotNullOrderBySubmittedAtDesc(Long userId);
    List<Attempt> findBySubmittedAtNotNull();
}
