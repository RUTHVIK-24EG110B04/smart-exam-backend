package com.smartexam.service;

import com.smartexam.dto.Dtos.*;
import com.smartexam.entity.Attempt;
import com.smartexam.entity.Role;
import com.smartexam.entity.User;
import com.smartexam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {
    private static final double PASS_PCT = 50.0;   // score >= 50% counts as a pass
    private static final int DAYS = 14;

    private final UserRepository users;
    private final ExamRepository exams;
    private final QuestionRepository questions;
    private final TopicRepository topics;
    private final AttemptRepository attempts;
    private final AttemptAnswerRepository answers;

    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }
    private static double pct(Attempt a) {
        return (a.getTotal() == null || a.getTotal() == 0 || a.getScore() == null) ? 0 : a.getScore() * 100.0 / a.getTotal();
    }
    private static double avg(Collection<Attempt> list) {
        return list.isEmpty() ? 0 : round1(list.stream().mapToDouble(AdminAnalyticsService::pct).average().orElse(0));
    }
    private static double passRate(Collection<Attempt> list) {
        return list.isEmpty() ? 0 : round1(list.stream().filter(a -> pct(a) >= PASS_PCT).count() * 100.0 / list.size());
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<Attempt> all = attempts.findBySubmittedAtNotNull();
        List<Attempt> tests = all.stream().filter(a -> !a.getExam().isPractice()).toList();   // admin-created exams
        long practice = all.size() - tests.size();

        // ---- per student ----
        Map<Long, List<Attempt>> byUser = tests.stream().collect(Collectors.groupingBy(a -> a.getUser().getId()));
        List<StudentRank> studentRows = new ArrayList<>();
        for (User u : users.findByRole(Role.STUDENT)) {
            List<Attempt> mine = byUser.getOrDefault(u.getId(), List.of());
            double best = mine.stream().mapToDouble(AdminAnalyticsService::pct).max().orElse(0);
            LocalDateTime last = mine.stream().map(Attempt::getSubmittedAt).max(Comparator.naturalOrder()).orElse(null);
            studentRows.add(new StudentRank(u.getName(), u.getEmail(), mine.size(), avg(mine), round1(best), last));
        }
        studentRows.sort(Comparator.comparingDouble(StudentRank::avgPct).reversed()
                .thenComparing(Comparator.comparingLong(StudentRank::tests).reversed()));
        long active = studentRows.stream().filter(s -> s.tests() > 0).count();

        Totals totals = new Totals(studentRows.size(), active, exams.findByPracticeFalse().size(),
                questions.count(), topics.count(), tests.size(), practice, avg(tests), passRate(tests));

        // ---- per exam ----
        List<ExamStat> examRows = new ArrayList<>();
        exams.findByPracticeFalse().forEach(e -> {
            List<Attempt> list = tests.stream().filter(a -> a.getExam().getId().equals(e.getId())).toList();
            double hi = list.stream().mapToDouble(AdminAnalyticsService::pct).max().orElse(0);
            double lo = list.stream().mapToDouble(AdminAnalyticsService::pct).min().orElse(0);
            examRows.add(new ExamStat(e.getId(), e.getTitle(), list.size(), avg(list), round1(hi), round1(lo), passRate(list)));
        });
        examRows.sort(Comparator.comparingLong(ExamStat::attempts).reversed());

        // ---- topics (all students) ----
        List<TopicPerformance> topicRows = answers.allTopicStats().stream().map(s -> {
            long att = s.getAttempted().longValue(), cor = s.getCorrect().longValue();
            return new TopicPerformance(s.getTopicId(), s.getTopicName(), att, cor, att == 0 ? 0 : round1(cor * 100.0 / att));
        }).sorted(Comparator.comparingDouble(TopicPerformance::accuracy)).toList();

        // ---- hardest questions ----
        List<HardQuestion> hardest = answers.allQuestionStats().stream().map(s -> {
            long att = s.getAttempted().longValue(), wr = s.getWrong().longValue();
            return new HardQuestion(s.getQuestionId(), s.getQuestionText(), s.getTopicName(), att, wr,
                    att == 0 ? 0 : round1(wr * 100.0 / att));
        }).filter(h -> h.attempted() >= 1 && h.wrong() > 0)
          .sorted(Comparator.comparingDouble(HardQuestion::wrongRate).reversed()
                  .thenComparing(Comparator.comparingLong(HardQuestion::attempted).reversed()))
          .limit(5).toList();

        // ---- recent submissions ----
        List<RecentAttempt> recent = tests.stream()
                .sorted(Comparator.comparing(Attempt::getSubmittedAt).reversed()).limit(8)
                .map(a -> new RecentAttempt(a.getId(), a.getUser().getName(), a.getExam().getTitle(),
                        a.getScore(), a.getTotal(), round1(pct(a)), a.getSubmittedAt())).toList();

        // ---- tests per day (last 14 days) ----
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> perDayMap = tests.stream()
                .collect(Collectors.groupingBy(a -> a.getSubmittedAt().toLocalDate(), Collectors.counting()));
        List<DayCount> perDay = new ArrayList<>();
        for (int i = DAYS - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            perDay.add(new DayCount(d.getDayOfMonth() + "/" + d.getMonthValue(), perDayMap.getOrDefault(d, 0L)));
        }

        // ---- score distribution ----
        long[] b = new long[5];
        tests.forEach(a -> b[Math.min(4, (int) (pct(a) / 20))]++);
        String[] labels = {"0-20%", "20-40%", "40-60%", "60-80%", "80-100%"};
        List<Bucket> dist = new ArrayList<>();
        for (int i = 0; i < 5; i++) dist.add(new Bucket(labels[i], b[i]));

        return new DashboardResponse(totals, examRows, topicRows, hardest, studentRows, recent, perDay, dist);
    }
}
