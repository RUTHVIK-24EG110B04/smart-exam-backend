package com.smartexam.controller;

import com.smartexam.dto.Dtos.*;
import com.smartexam.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final ExamService service;

    @GetMapping("/exams")
    public List<ExamSummary> exams(Authentication auth) { return service.listExams(auth.getName()); }

    @PostMapping("/exams/{id}/start")
    public StartResponse start(@PathVariable Long id, Authentication auth) { return service.start(auth.getName(), id); }

    @PostMapping("/attempts/{id}/submit")
    public ResultResponse submit(@PathVariable Long id, @RequestBody SubmitRequest req, Authentication auth) {
        return service.submit(auth.getName(), id, req.answers());
    }

    @GetMapping("/attempts")
    public List<AttemptSummary> history(Authentication auth) { return service.history(auth.getName()); }

    @GetMapping("/attempts/{id}")
    public ResultResponse result(@PathVariable Long id, Authentication auth) { return service.result(auth.getName(), id); }

    @GetMapping("/analytics")
    public List<TopicAccuracy> analytics(Authentication auth) { return service.analytics(auth.getName()); }

    @PostMapping("/practice")
    public ExamSummary practice(Authentication auth) { return service.generatePractice(auth.getName()); }
}
