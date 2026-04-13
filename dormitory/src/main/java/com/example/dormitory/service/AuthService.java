package com.example.dormitory.service;

import com.example.dormitory.dto.JwtResponse;
import com.example.dormitory.dto.LoginRequest;
import com.example.dormitory.entity.User;
import com.example.dormitory.repository.CredentialRepository;
import com.example.dormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;

    public JwtResponse authenticate(LoginRequest request) {
        var credential = credentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!credential.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        User user = credential.getUser();
        String role = user.getRoles().isEmpty() ? "STUDENT" : user.getRoles().get(0).getName().name();
        return new JwtResponse("dummy-token", user.getId(), credential.getEmail(), role);
    }
}