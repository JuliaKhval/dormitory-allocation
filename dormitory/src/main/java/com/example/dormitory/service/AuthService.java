package com.example.dormitory.service;

import com.example.dormitory.dto.JwtResponse;
import com.example.dormitory.dto.LoginRequest;
import com.example.dormitory.entity.Credential;
import com.example.dormitory.entity.User;
import com.example.dormitory.repository.CredentialRepository;
import com.example.dormitory.repository.UserRepository;
import com.example.dormitory.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public JwtResponse authenticate(LoginRequest request) {
        Credential credential = credentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        // Временно сравниваем пароль в открытом виде (позже добавим шифрование)
        if (!credential.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        User user = credential.getUser();
        String role = user.getRoles().isEmpty() ? "STUDENT" : user.getRoles().get(0).getName().name();
        String token = jwtUtils.generateJwtToken(credential.getEmail(), user.getId(), role);
        return new JwtResponse(token, user.getId(), credential.getEmail(), role);
    }
}