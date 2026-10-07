package com.smartexam.service;

import com.smartexam.dto.Dtos.*;
import com.smartexam.entity.*;
import com.smartexam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {
    // Adaptive rule: a topic is WEAK if accuracy < 60% after at least 5 attempted questions
    private static final double WEAK_THRESHOLD = 60.0;
    private static final int MIN_ATTEMPTED = 5;
    private static final int PRACTICE_SIZE = 10;

    private final UserRepository users;
    private final ExamRepository exams;
    private final QuestionRepository questions;
    private final AttemptRepository attempts;
    private final AttemptAnswerRepository answers;

    private User user(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public static ExamSummary toSummary(Exam e) {
        return new ExamSummary(e.getId(), e.getTitle(), e.getDurationMinutes(), e.isPractice(), e.getQuestions().size());
    }

    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    private Attempt ownedAttempt(String email, Long attemptId) {
        User u = user(email);
        Attempt a = attempts.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found"));
        if (!a.getUser().getId().equals(u.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your attempt");
        return a;
    }

    @Transactional(readOnly = true)
    public List<ExamSummary> listExams(String email) {
        User u = user(email);
        List<Exam> list = new ArrayList<>(exams.findByPracticeFalse());
        list.addAll(exams.findByOwnerIdAndPracticeTrue(u.getId()));
        return list.stream().map(ExamService::toSummary).toList();
    }

    @Transactional
    public StartResponse start(String email, Long examId) {
        User u = user(email);
        Exam e = exams.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
        if (e.isPractice() && !u.getId().equals(e.getOwnerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your practice test");

        Attempt a = new Attempt();
        a.setUser(u); a.setExam(e);
        a.setTotal(e.getQuestions().size());
        a.setStartedAt(LocalDateTime.now());
        attempts.save(a);

        List<StudentQuestion> qs = e.getQuestions().stream()
                .map(q -> new StudentQuestion(q.getId(), q.getTopic().getName(), q.getText(),
                        q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()))
                .toList();
        return new StartResponse(a.getId(), e.getTitle(), e.getDurationMinutes(), qs);
    }

    @Transactional
    public ResultResponse submit(String email, Long attemptId, Map<Long, String> given) {
        Attempt a = ownedAttempt(email, attemptId);
        if (a.getSubmittedAt() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already submitted");

        int score = 0;
        for (Question q : a.getExam().getQuestions()) {
            String sel = given == null ? null : given.get(q.getId());
            boolean ok = sel != null && sel.equalsIgnoreCase(q.getCorrectOption());
            if (ok) score++;
            AttemptAnswer aa = new AttemptAnswer();
            aa.setAttempt(a); aa.setQuestion(q); aa.setSelectedOption(sel); aa.setCorrect(ok);
            answers.save(aa);
        }
        a.setScore(score);
        a.setSubmittedAt(LocalDateTime.now());
        attempts.save(a);
        return result(email, attemptId);
    }

    @Transactional(readOnly = true)
    public ResultResponse result(String email, Long attemptId) {
        Attempt a = ownedAttempt(email, attemptId);
        if (a.getSubmittedAt() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attempt not submitted yet");
        List<ReviewItem> review = answers.findByAttemptId(attemptId).stream()
                .map(x -> new ReviewItem(x.getQuestion().getId(), x.getQuestion().getTopic().getName(),
                        x.getQuestion().getText(), x.getSelectedOption(),
                        x.getQuestion().getCorrectOption(), x.isCorrect()))
                .toList();
        double pct = a.getTotal() == 0 ? 0 : round1(a.getScore() * 100.0 / a.getTotal());
        return new ResultResponse(a.getId(), a.getExam().getTitle(), a.getScore(), a.getTotal(), pct, review);
    }

    @Transactional(readOnly = true)
    public List<AttemptSummary> history(String email) {
        User u = user(email);
        return attempts.findByUserIdAndSubmittedAtNotNullOrderBySubmittedAtDesc(u.getId()).stream()
                .map(a -> new AttemptSummary(a.getId(), a.getExam().getTitle(), a.getExam().isPractice(),
                        a.getScore(), a.getTotal(), a.getSubmittedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopicAccuracy> analytics(String email) {
        User u = user(email);
        return answers.topicStats(u.getId()).stream().map(s -> {
            long attempted = s.getAttempted().longValue();
            long correct = s.getCorrect().longValue();
            double acc = attempted == 0 ? 0 : round1(correct * 100.0 / attempted);
            boolean weak = attempted >= MIN_ATTEMPTED && acc < WEAK_THRESHOLD;
            return new TopicAccuracy(s.getTopicId(), s.getTopicName(), attempted, correct, acc, weak);
        }).toList();
    }

    // Builds a small practice test from the student's weakest topic
    @Transactional
    public ExamSummary generatePractice(String email) {
        User u = user(email);
        TopicAccuracy weakest = analytics(email).stream()
                .filter(TopicAccuracy::weak)
                .min(Comparator.comparingDouble(TopicAccuracy::accuracy))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No weak topic found yet. Attempt at least 5 questions in a topic and score below 60%."));

        List<AttemptAnswer> past = answers.findByUserAndTopic(u.getId(), weakest.topicId());
        Set<Long> wrong = past.stream().filter(x -> !x.isCorrect())
                .map(x -> x.getQuestion().getId()).collect(Collectors.toSet());
        Set<Long> seen = past.stream().map(x -> x.getQuestion().getId()).collect(Collectors.toSet());

        List<Question> pool = new ArrayList<>(questions.findByTopicId(weakest.topicId()));
        Collections.shuffle(pool);
        // priority: previously wrong (0) -> never seen (1) -> answered correctly before (2)
        pool.sort(Comparator.comparingInt((Question q) ->
                wrong.contains(q.getId()) ? 0 : seen.contains(q.getId()) ? 2 : 1));
        List<Question> chosen = pool.stream().limit(PRACTICE_SIZE).toList();

        Exam e = new Exam();
        e.setTitle("Practice: " + weakest.topicName());
        e.setDurationMinutes(Math.max(5, chosen.size()));
        e.setPractice(true);
        e.setOwnerId(u.getId());
        e.setQuestions(new ArrayList<>(chosen));
        exams.save(e);
        return toSummary(e);
    }
}
