package com.smartexam.controller;

import com.smartexam.dto.Dtos.ExamSummary;
import com.smartexam.entity.Exam;
import com.smartexam.repository.*;
import com.smartexam.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class SubjectController {
    private final TopicRepository topics;
    private final ExamRepository exams;
    private final QuestionRepository questions;

    public record Subject(Long id, String name, int questionCount, int testCount) {}

    @GetMapping("/subjects")
    @Transactional(readOnly = true)
    public List<Subject> subjects() {
        List<Exam> all = exams.findByPracticeFalse();
        return topics.findAll().stream().map(t -> new Subject(t.getId(), t.getName(),
                questions.findByTopicId(t.getId()).size(),
                (int) all.stream().filter(e -> has(e, t.getId())).count())).toList();
    }

    @GetMapping("/subjects/{id}/tests")
    @Transactional(readOnly = true)
    public List<ExamSummary> tests(@PathVariable Long id) {
        return exams.findByPracticeFalse().stream().filter(e -> has(e, id)).map(ExamService::toSummary).toList();
    }

    private boolean has(Exam e, Long topicId) {
        return e.getQuestions().stream().anyMatch(q -> q.getTopic().getId().equals(topicId));
    }
}
