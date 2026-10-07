package com.smartexam.repository;

import com.smartexam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByPracticeFalse();
    List<Exam> findByOwnerIdAndPracticeTrue(Long ownerId);
}
