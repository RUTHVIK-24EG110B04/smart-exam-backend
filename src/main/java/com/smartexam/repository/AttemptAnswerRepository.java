package com.smartexam.repository;

import com.smartexam.entity.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    interface TopicStat {
        Long getTopicId();
        String getTopicName();
        Number getAttempted();
        Number getCorrect();
    }

    interface QuestionStat {
        Long getQuestionId();
        String getQuestionText();
        String getTopicName();
        Number getAttempted();
        Number getWrong();
    }

    List<AttemptAnswer> findByAttemptId(Long attemptId);

    // Topic-wise accuracy across ALL students (admin view)
    @Query("select q.topic.id as topicId, q.topic.name as topicName, count(aa) as attempted, " +
           "sum(case when aa.correct = true then 1 else 0 end) as correct " +
           "from AttemptAnswer aa join aa.question q group by q.topic.id, q.topic.name")
    List<TopicStat> allTopicStats();

    // Per-question wrong counts across ALL students (admin view)
    @Query("select q.id as questionId, q.text as questionText, q.topic.name as topicName, count(aa) as attempted, " +
           "sum(case when aa.correct = false then 1 else 0 end) as wrong " +
           "from AttemptAnswer aa join aa.question q group by q.id, q.text, q.topic.name")
    List<QuestionStat> allQuestionStats();

    // Topic-wise accuracy for one student
    @Query("select q.topic.id as topicId, q.topic.name as topicName, count(aa) as attempted, " +
           "sum(case when aa.correct = true then 1 else 0 end) as correct " +
           "from AttemptAnswer aa join aa.question q " +
           "where aa.attempt.user.id = :uid group by q.topic.id, q.topic.name")
    List<TopicStat> topicStats(@Param("uid") Long uid);

    @Query("select aa from AttemptAnswer aa where aa.attempt.user.id = :uid and aa.question.topic.id = :tid")
    List<AttemptAnswer> findByUserAndTopic(@Param("uid") Long uid, @Param("tid") Long tid);
}
