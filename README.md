# Smart Exam Prep - Backend (Spring Boot + MySQL)

## Requirements
- Java 17+
- Maven 3.8+ (or use an IDE such as IntelliJ / VS Code)
- MySQL 8+ running on localhost:3306

## Setup
1. Start MySQL. The database `smart_exam` and all tables are created automatically on first run.
2. Edit `src/main/resources/application.properties` and set your MySQL
   username/password (and change `jwt.secret` to your own long random string).
3. Run:
   ```
   mvn spring-boot:run
   ```
   Server starts on http://localhost:8081

## Default admin (created automatically on first run)
- Email: admin@exam.com
- Password: admin123  (change it after first login)

## Main APIs
- POST /api/auth/register, /api/auth/login
- /api/admin/**   (ADMIN)  - topics, questions, exams, GET /api/admin/dashboard
- /api/student/** (STUDENT) - tests, results, analytics
