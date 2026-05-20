package com.example.dormitory.security;

import com.example.dormitory.entity.User;
import com.example.dormitory.enums.RoleName;
import com.example.dormitory.repository.DormitoryRepository;
import com.example.dormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final DormitoryRepository dormitoryRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByCredentialEmail(email)
                .orElseThrow();
        AtomicReference<Long> dormitoryId = new AtomicReference<>();
        if (user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.WARDEN)) {
            dormitoryRepository.findByWardenId(user.getId()).ifPresent(d -> dormitoryId.set(d.getId()));
        }
        return new UserDetailsImpl(user, dormitoryId.get());
    }
}