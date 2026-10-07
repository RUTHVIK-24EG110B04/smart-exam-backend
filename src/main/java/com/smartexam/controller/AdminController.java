package com.smartexam.controller;

import com.smartexam.dto.Dtos.*;
import com.smartexam.entity.*;
import com.smartexam.repository.*;
import com.smartexam.service.AdminAnalyticsService;
import com.smartexam.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TopicRepository topics;
    private final QuestionRepository questions;
    private final ExamRepository exams;
    private final AdminAnalyticsService analytics;

    // ---- Dashboard / analytics ----
    @GetMapping("/dashboard")
    public DashboardResponse dashboard() { return analytics.dashboard(); }

    // ---- Topics ----
    @GetMapping("/topics")
    public List<Topic> listTopics() { return topics.findAll(); }

    @PostMapping("/topics")
    public Topic addTopic(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
        Topic t = new Topic();
        t.setName(name.trim());
        return topics.save(t);
    }

    @DeleteMapping("/topics/{id}")
    public void deleteTopic(@PathVariable Long id) { topics.deleteById(id); }

    // ---- Questions ----
    @GetMapping("/questions")
    public List<Question> listQuestions(@RequestParam(required = false) Long topicId) {
        return topicId == null ? questions.findAll() : questions.findByTopicId(topicId);
    }

    @PostMapping("/questions")
    public Question addQuestion(@RequestBody QuestionRequest r) { return questions.save(apply(new Question(), r)); }

    @PutMapping("/questions/{id}")
    public Question updateQuestion(@PathVariable Long id, @RequestBody QuestionRequest r) {
        Question q = questions.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        return questions.save(apply(q, r));
    }

    @DeleteMapping("/questions/{id}")
    public void deleteQuestion(@PathVariable Long id) { questions.deleteById(id); }

    private Question apply(Question q, QuestionRequest r) {
        if (r.correctOption() == null || !r.correctOption().toUpperCase().matches("[A-D]"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "correctOption must be A, B, C or D");
        q.setTopic(topics.findById(r.topicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid topic")));
        q.setText(r.text());
        q.setOptionA(r.optionA()); q.setOptionB(r.optionB());
        q.setOptionC(r.optionC()); q.setOptionD(r.optionD());
        q.setCorrectOption(r.correctOption().toUpperCase());
        q.setDifficulty(r.difficulty() == null ? "MEDIUM" : r.difficulty());
        return q;
    }

    // ---- Exams ----
    @GetMapping("/exams")
    @Transactional(readOnly = true)
    public List<ExamSummary> listExams() {
        return exams.findByPracticeFalse().stream().map(ExamService::toSummary).toList();
    }

    @PostMapping("/exams")
    @Transactional
    public ExamSummary addExam(@RequestBody ExamRequest r) {
        if (r.questionIds() == null || r.questionIds().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one question");
        Exam e = new Exam();
        e.setTitle(r.title());
        e.setDurationMinutes(r.durationMinutes());
        e.setPractice(false);
        e.setQuestions(new ArrayList<>(questions.findAllById(r.questionIds())));
        return ExamService.toSummary(exams.save(e));
    }

    @DeleteMapping("/exams/{id}")
    public void deleteExam(@PathVariable Long id) { exams.deleteById(id); }
}
