package com.example.dormitory.service;

import com.example.dormitory.dto.CreateUserDto;
import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.entity.*;
import com.example.dormitory.enums.Gender;
import com.example.dormitory.enums.RoleName;
import com.example.dormitory.mapper.UserMapper;
import com.example.dormitory.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DormitoryRepository dormitoryRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final CountryRepository countryRepository;
    private final BenefitTypeRepository benefitTypeRepository;
    private final StudentDetailRepository studentDetailRepository;

    public UserProfileDto getProfileByUser(User user) {
        return userMapper.toUserProfileDto(user);
    }

    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAllWithRoles().stream()
                .map(userMapper::toUserProfileDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileDto createUser(CreateUserDto dto) {
        // 1. Проверка, что email не занят
        if (credentialRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already taken");

        // 2. Создание пользователя
        User user = User.builder()
                .fullName(dto.getFullName())
                .build();
        user = userRepository.save(user);

        // 3. Создание учётных данных
        Credential credential = Credential.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .user(user)
                .build();
        credentialRepository.save(credential);

        // 4. Назначение ролей
        List<Role> roles = dto.getRoles().stream()
                .map(name -> roleRepository.findByName(RoleName.valueOf(name))
                        .orElseThrow(() -> new RuntimeException("Role not found: " + name)))
                .collect(Collectors.toList());
        user.setRoles(roles);
        user = userRepository.save(user);

        // 5. Если есть роль STUDENT и переданы данные студента, создаём StudentDetail
        if (roles.stream().anyMatch(r -> r.getName() == RoleName.STUDENT)) {
            if (dto.getGroupId() == null || dto.getCountryId() == null) {
                throw new RuntimeException("Для создания студента необходимо указать groupId и countryId");
            }

            StudyGroup group = studyGroupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Study group not found"));
            Country country = countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException("Country not found"));

            StudentDetail studentDetail = StudentDetail.builder()
                    .user(user)
                    .group(group)
                    .country(country)
                    .gender(Gender.valueOf(dto.getGender()))
                    .averageScore(dto.getAverageScore())
                    .phoneNumber(dto.getPhoneNumber())
                    .build();

            // Добавляем льготы, если указаны
            if (dto.getBenefitTypeIds() != null && !dto.getBenefitTypeIds().isEmpty()) {
                List<BenefitType> benefits = benefitTypeRepository.findAllById(dto.getBenefitTypeIds());
                studentDetail.setBenefits(benefits);
            }

            studentDetailRepository.save(studentDetail);
        }

        // 6. Возвращаем DTO
        return userMapper.toUserProfileDto(user);
    }
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public UserProfileDto updateRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(RoleName.valueOf(name))
                        .orElseThrow(() -> new RuntimeException("Role not found")))
                .collect(Collectors.toList());
        user.setRoles(roles);
        return userMapper.toUserProfileDto(userRepository.save(user));
    }
    @Transactional
    public void assignWardenToDormitory(Long dormitoryId, Long wardenUserId) {
        User warden = userRepository.findById(wardenUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isWarden = warden.getRoles().stream().anyMatch(r -> r.getName() == RoleName.WARDEN);
        if (!isWarden) {
            throw new RuntimeException("User is not a warden");
        }
        Dormitory dormitory = dormitoryRepository.findById(dormitoryId)
                .orElseThrow(() -> new RuntimeException("Dormitory not found"));
        dormitory.setWarden(warden);
        dormitoryRepository.save(dormitory);
    }

}