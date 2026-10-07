package com.smartexam.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(String name, String email, String password) {}
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token, String name, String role) {}

    public record QuestionRequest(Long topicId, String text, String optionA, String optionB,
                                  String optionC, String optionD, String correctOption, String difficulty) {}
    public record ExamRequest(String title, int durationMinutes, List<Long> questionIds) {}
    public record ExamSummary(Long id, String title, int durationMinutes, boolean practice, int questionCount) {}

    // Question as shown to a student (NO correct answer)
    public record StudentQuestion(Long id, String topic, String text,
                                  String optionA, String optionB, String optionC, String optionD) {}
    public record StartResponse(Long attemptId, String examTitle, int durationMinutes, List<StudentQuestion> questions) {}

    public record SubmitRequest(Map<Long, String> answers) {}
    public record ReviewItem(Long questionId, String topic, String text, String selected,
                             String correctOption, boolean correct) {}
    public record ResultResponse(Long attemptId, String examTitle, int score, int total,
                                 double percentage, List<ReviewItem> review) {}

    public record TopicAccuracy(Long topicId, String topicName, long attempted, long correct,
                                double accuracy, boolean weak) {}
    public record AttemptSummary(Long id, String examTitle, boolean practice, int score, int total,
                                 LocalDateTime submittedAt) {}

    // ---- Admin dashboard ----
    public record Totals(long students, long activeStudents, long exams, long questions, long topics,
                         long testsTaken, long practiceTests, double avgPct, double passRate) {}
    public record ExamStat(Long examId, String title, long attempts, double avgPct,
                           double highestPct, double lowestPct, double passRate) {}
    public record TopicPerformance(Long topicId, String topicName, long attempted, long correct, double accuracy) {}
    public record HardQuestion(Long questionId, String text, String topic, long attempted, long wrong, double wrongRate) {}
    public record StudentRank(String name, String email, long tests, double avgPct, double bestPct,
                              LocalDateTime lastActive) {}
    public record RecentAttempt(Long id, String student, String exam, int score, int total, double pct,
                                LocalDateTime submittedAt) {}
    public record DayCount(String date, long count) {}
    public record Bucket(String range, long count) {}
    public record DashboardResponse(Totals totals, List<ExamStat> exams, List<TopicPerformance> topics,
                                    List<HardQuestion> hardest, List<StudentRank> students,
                                    List<RecentAttempt> recent, List<DayCount> perDay, List<Bucket> distribution) {}
}
