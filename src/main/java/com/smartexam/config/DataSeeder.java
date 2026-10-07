package com.smartexam.config;

import com.smartexam.entity.*;
import com.smartexam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final TopicRepository topics;
    private final QuestionRepository questions;
    private final ExamRepository exams;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        User admin = new User();
        admin.setName("Admin"); admin.setEmail("admin@exam.com");
        admin.setPassword(encoder.encode("admin123")); admin.setRole(Role.ADMIN);
        users.save(admin);

        Topic java = topic("Java");
        Topic dbms = topic("DBMS");
        Topic dsa = topic("DSA");
        List<Question> all = new ArrayList<>();

        all.add(q(java, "Which keyword is used to inherit a class in Java?", "extends", "implements", "inherits", "super", "A"));
        all.add(q(java, "What is the default value of an int instance variable?", "0", "null", "undefined", "1", "A"));
        all.add(q(java, "Which collection does not allow duplicate elements?", "ArrayList", "LinkedList", "HashSet", "Vector", "C"));
        all.add(q(java, "Which of these is NOT a primitive type?", "int", "boolean", "String", "char", "C"));
        all.add(q(java, "Which annotation marks the main class of a Spring Boot app?", "@SpringBootApplication", "@Service", "@Entity", "@Component", "A"));

        all.add(q(dbms, "Which SQL command quickly removes all rows from a table?", "TRUNCATE", "SELECT", "UPDATE", "INSERT", "A"));
        all.add(q(dbms, "Which key uniquely identifies each row in a table?", "Primary key", "Foreign key", "Composite index", "Alias", "A"));
        all.add(q(dbms, "Which normal form removes partial dependency?", "1NF", "2NF", "3NF", "BCNF", "B"));
        all.add(q(dbms, "Which clause filters grouped results?", "WHERE", "HAVING", "ORDER BY", "LIMIT", "B"));
        all.add(q(dbms, "In ACID, what does 'I' stand for?", "Integrity", "Isolation", "Indexing", "Inheritance", "B"));

        all.add(q(dsa, "What is the time complexity of binary search?", "O(n)", "O(log n)", "O(n log n)", "O(1)", "B"));
        all.add(q(dsa, "Which data structure follows LIFO order?", "Queue", "Stack", "Array", "Tree", "B"));
        all.add(q(dsa, "What is the worst-case time complexity of quicksort?", "O(n log n)", "O(n^2)", "O(n)", "O(log n)", "B"));
        all.add(q(dsa, "Which traversal of a BST gives sorted order?", "Preorder", "Postorder", "Inorder", "Level order", "C"));
        all.add(q(dsa, "Which data structure is used in BFS?", "Stack", "Queue", "Heap", "Set", "B"));

        Exam e = new Exam();
        e.setTitle("Full Stack Basics - Mock Test 1");
        e.setDurationMinutes(15);
        e.setPractice(false);
        e.setQuestions(new ArrayList<>(all));
        exams.save(e);
    }

    private Topic topic(String name) {
        Topic t = new Topic(); t.setName(name); return topics.save(t);
    }

    private Question q(Topic t, String text, String a, String b, String c, String d, String correct) {
        Question q = new Question();
        q.setTopic(t); q.setText(text);
        q.setOptionA(a); q.setOptionB(b); q.setOptionC(c); q.setOptionD(d);
        q.setCorrectOption(correct); q.setDifficulty("MEDIUM");
        return questions.save(q);
    }
}
