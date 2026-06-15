package com.example.dormitory.service;

import com.example.dormitory.dto.JwtResponse;
import com.example.dormitory.dto.LoginRequest;
import com.example.dormitory.security.JwtUtils;
import com.example.dormitory.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public JwtResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .filter(r -> r.equals("ADMIN") || r.equals("WARDEN") || r.equals("STUDENT"))
                .sorted((a, b) -> rolePriority(a) - rolePriority(b))
                .findFirst()
                .orElse("STUDENT");
        return new JwtResponse(token, userDetails.getUser().getId(), request.getEmail(), role);
    }

    private static int rolePriority(String role) {
        return switch (role) {
            case "ADMIN" -> 0;
            case "WARDEN" -> 1;
            default -> 2;
        };
    }
}