package com.smartexam.controller;

import com.smartexam.dto.Dtos.*;
import com.smartexam.entity.Role;
import com.smartexam.entity.User;
import com.smartexam.repository.UserRepository;
import com.smartexam.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest r) {
        if (r.email() == null || r.email().isBlank() || r.password() == null || r.password().length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid email and password (min 6 chars) required");
        if (users.existsByEmail(r.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setName(r.name());
        u.setEmail(r.email());
        u.setPassword(encoder.encode(r.password()));
        u.setRole(Role.STUDENT);          // public registration creates students only
        users.save(u);
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), u.getName(), u.getRole().name());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest r) {
        User u = users.findByEmail(r.email())
                .filter(x -> encoder.matches(r.password(), x.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), u.getName(), u.getRole().name());
    }
}
