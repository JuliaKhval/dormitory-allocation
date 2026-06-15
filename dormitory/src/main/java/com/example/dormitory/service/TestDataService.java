package com.example.dormitory.service;

import com.example.dormitory.entity.*;
import com.example.dormitory.enums.Gender;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.enums.RoleName;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestDataService {
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final StudentDetailRepository studentDetailRepository;
    private final RoleRepository roleRepository;
    private final RequestRepository requestRepository;
    private final RequestPreferenceRepository preferenceRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final CountryRepository countryRepository;
    private final BenefitTypeRepository benefitTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Random RANDOM = new Random();
    private static final String[] FIRST_NAMES = {"Иван", "Пётр", "Сидор", "Анна", "Мария", "Елена", "Алексей", "Дмитрий", "Ольга", "Татьяна"};
    private static final String[] LAST_NAMES = {"Иванов", "Петров", "Сидоров", "Кузнецова", "Смирнова", "Волкова", "Морозов", "Новиков"};

    @Transactional
    public void generateTestData(int studentCount, int requestCount) {
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new RuntimeException("Role STUDENT not found"));
        List<StudyGroup> groups = studyGroupRepository.findAll();
        List<Country> countries = countryRepository.findAll();
        List<BenefitType> benefits = benefitTypeRepository.findAll();

        if (groups.isEmpty() || countries.isEmpty()) {
            throw new RuntimeException("Need at least one study group and one country");
        }

        List<User> students = new ArrayList<>();

        // Создаём студентов
        for (int i = 0; i < studentCount; i++) {
            String firstName = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
            String fullName = lastName + " " + firstName + " " + (char) ('А' + RANDOM.nextInt(32));

            User user = User.builder()
                    .fullName(fullName)
                    .build();
            user = userRepository.save(user);

            Credential credential = Credential.builder()
                    .email("student" + i + "@test.com")
                    .password(passwordEncoder.encode("123456"))
                    .user(user)
                    .build();
            credentialRepository.save(credential);

            user.getRoles().add(studentRole);
            userRepository.save(user);

            StudyGroup group = groups.get(RANDOM.nextInt(groups.size()));
            Country country = countries.get(RANDOM.nextInt(countries.size()));
            Gender gender = RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE;
            BigDecimal averageScore = BigDecimal.valueOf(50 + RANDOM.nextDouble() * 50);

            StudentDetail studentDetail = StudentDetail.builder()
                    .user(user)
                    .group(group)
                    .country(country)
                    .gender(gender)
                    .averageScore(averageScore)
                    .phoneNumber("+37529" + (1000000 + RANDOM.nextInt(9000000)))
                    .build();

            if (!benefits.isEmpty() && RANDOM.nextBoolean()) {
                List<BenefitType> selectedBenefits = new ArrayList<>();
                if (RANDOM.nextBoolean()) selectedBenefits.add(benefits.get(0));
                if (RANDOM.nextBoolean() && benefits.size() > 1) selectedBenefits.add(benefits.get(1));
                studentDetail.setBenefits(selectedBenefits);
            }

            studentDetailRepository.save(studentDetail);
            students.add(user);
        }

        // Создаём заявки
        int year = LocalDateTime.now().getYear();
        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < requestCount && i < students.size(); i++) {
            User student = students.get(i);
            Request request = Request.builder()
                    .user(student)
                    .year(year)
                    .build();
            request = requestRepository.save(request);
            requests.add(request);
        }

        // Создаём предпочтения (каждая заявка указывает 0-3 соседей)
        for (Request request : requests) {
            int prefCount = RANDOM.nextInt(4); // 0-3
            List<User> candidates = students.stream()
                    .filter(s -> !s.equals(request.getUser()))
                    .collect(Collectors.toList());
            Collections.shuffle(candidates);

            for (int j = 0; j < Math.min(prefCount, candidates.size()); j++) {
                User preferred = candidates.get(j);
                // Проверка страны
                StudentDetail prefStudent = preferred.getStudentDetail();
                StudentDetail currentStudent = request.getUser().getStudentDetail();
                if (prefStudent == null || currentStudent == null) continue;
                if (!prefStudent.getCountry().getId().equals(currentStudent.getCountry().getId())) continue;

                if (!preferenceRepository.existsByRequestIdAndPreferredUserId(request.getId(), preferred.getId())) {
                    RequestPreference pref = RequestPreference.builder()
                            .requester(request.getUser())
                            .preferredUser(preferred)
                            .year(year)
                            .request(request)
                            .status(RequestPreferenceStatus.PENDING)
                            .build();
                    preferenceRepository.save(pref);
                }
            }
        }

        // Проверяем взаимные предпочтения и автоматически подтверждаем
        for (RequestPreference pref : preferenceRepository.findAll()) {
            if (pref.getStatus() == RequestPreferenceStatus.PENDING) {
                preferenceRepository.findByRequesterAndPreferred(
                        pref.getPreferredUser().getId(),
                        pref.getRequester().getId(),
                        pref.getYear()
                ).ifPresent(mutual -> {
                    if (mutual.getStatus() == RequestPreferenceStatus.PENDING) {
                        pref.setStatus(RequestPreferenceStatus.APPROVED);
                        mutual.setStatus(RequestPreferenceStatus.APPROVED);
                        preferenceRepository.save(pref);
                        preferenceRepository.save(mutual);
                    }
                });
            }
        }
    }

    public List<String> listAvailableDatasets() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:testdata/*.json");
            List<String> names = new ArrayList<>();
            for (Resource r : resources) {
                String filename = r.getFilename();
                if (filename != null && filename.endsWith(".json")) {
                    names.add(filename.substring(0, filename.length() - 5));
                }
            }
            Collections.sort(names);
            return names;
        } catch (Exception e) {
            throw new RuntimeException("Cannot list test datasets", e);
        }
    }

    @Transactional
    public void loadDatasetFromJson(String datasetName) {
        String path = "testdata/" + datasetName + ".json";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Dataset not found: " + datasetName);
            JsonNode root = objectMapper.readTree(is);
            loadFromJsonNode(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dataset: " + datasetName, e);
        }
    }

    private void loadFromJsonNode(JsonNode root) {
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new RuntimeException("Role STUDENT not found"));
        List<StudyGroup> groups = studyGroupRepository.findAll();
        List<Country> countries = countryRepository.findAll();
        List<BenefitType> benefits = benefitTypeRepository.findAll();
        if (groups.isEmpty() || countries.isEmpty()) {
            throw new RuntimeException("Need at least one study group and one country");
        }

        int year = LocalDateTime.now().getYear();
        List<User> createdStudents = new ArrayList<>();
        JsonNode studentsNode = root.get("students");
        if (studentsNode != null && studentsNode.isArray()) {
            for (JsonNode s : studentsNode) {
                User user = createStudentFromJson(s, studentRole, groups, countries, benefits);
                createdStudents.add(user);
                Request request = Request.builder().user(user).year(year).build();
                requestRepository.save(request);
            }
        }

        JsonNode prefsNode = root.get("preferences");
        if (prefsNode != null && prefsNode.isArray()) {
            for (JsonNode p : prefsNode) {
                int from = p.get("from").asInt();
                int to = p.get("to").asInt();
                if (from >= createdStudents.size() || to >= createdStudents.size()) continue;
                User requester = createdStudents.get(from);
                User preferred = createdStudents.get(to);
                Request request = requestRepository.findByUserId(requester.getId()).stream().findFirst().orElse(null);
                if (request == null) continue;
                RequestPreference pref = RequestPreference.builder()
                        .requester(requester)
                        .preferredUser(preferred)
                        .year(year)
                        .request(request)
                        .status(RequestPreferenceStatus.PENDING)
                        .build();
                preferenceRepository.save(pref);
            }
        }
        for (RequestPreference pref : preferenceRepository.findAll()) {
            if (pref.getStatus() == RequestPreferenceStatus.PENDING) {
                preferenceRepository.findByRequesterAndPreferred(
                        pref.getPreferredUser().getId(),
                        pref.getRequester().getId(),
                        pref.getYear()
                ).ifPresent(mutual -> {
                    if (mutual.getStatus() == RequestPreferenceStatus.PENDING) {
                        pref.setStatus(RequestPreferenceStatus.APPROVED);
                        mutual.setStatus(RequestPreferenceStatus.APPROVED);
                        preferenceRepository.save(pref);
                        preferenceRepository.save(mutual);
                    }
                });
            }
        }
    }

    private User createStudentFromJson(JsonNode s, Role studentRole, List<StudyGroup> groups,
                                       List<Country> countries, List<BenefitType> benefits) {
        User user = User.builder().fullName(s.get("fullName").asText()).build();
        user = userRepository.save(user);
        Credential credential = Credential.builder()
                .email(s.get("email").asText())
                .password(passwordEncoder.encode("123456"))
                .user(user)
                .build();
        credentialRepository.save(credential);
        user.getRoles().add(studentRole);
        userRepository.save(user);

        int gi = s.has("groupIndex") ? s.get("groupIndex").asInt(0) : 0;
        int ci = s.has("countryIndex") ? s.get("countryIndex").asInt(0) : 0;
        StudyGroup group = groups.get(Math.min(gi, groups.size() - 1));
        Country country = countries.get(Math.min(ci, countries.size() - 1));
        Gender gender = Gender.valueOf(s.get("gender").asText("MALE"));

        StudentDetail detail = StudentDetail.builder()
                .user(user)
                .group(group)
                .country(country)
                .gender(gender)
                .averageScore(BigDecimal.valueOf(s.get("averageScore").asDouble(70)))
                .build();

        if (s.has("benefitIndexes") && s.get("benefitIndexes").isArray()) {
            List<BenefitType> selected = new ArrayList<>();
            for (JsonNode bi : s.get("benefitIndexes")) {
                int idx = bi.asInt();
                if (idx >= 0 && idx < benefits.size()) selected.add(benefits.get(idx));
            }
            detail.setBenefits(selected);
        }
        studentDetailRepository.save(detail);
        return user;
    }
}