```markdown
# Dormitory Allocation Server

[![Java](https://img.shields.io/badge/Java-17-blue)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Серверная часть автоматизированной системы расселения студентов общежития (дипломный проект).  
Система позволяет студентам подавать заявки на заселение, а коменданту – управлять комнатами, запускать алгоритм распределения с учётом приоритетов (средний балл, льготы) и жёстких ограничений (пол, страна проживания).

Проект представляет собой монолитное Spring Boot приложение с чёткой слоистой архитектурой.


 Технологический стек
- Java 17 (LTS)
- Spring Boot 3.2.x (Web, Security, Data JPA, Validation)
- Spring Data JPA (Hibernate)
- PostgreSQL 14+ – основная база данных
- Spring Security + JWT – аутентификация и авторизация
- Liquibase – управление схемой БД (миграции)
- Maven – сборка проекта
- Docker + Docker Compose – контейнеризация
- SpringDoc OpenAPI 3 – генерация документации API (Swagger UI)
- JUnit 5, Mockito, Testcontainers – тестирование

Структура проекта
```
dormitory-allocation-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/dormitory/
│   │   │       ├── config/               # Конфигурации (Security, OpenAPI, Liquibase)
│   │   │       ├── controller/            # REST контроллеры
│   │   │       ├── dto/                   # Data Transfer Objects (запросы/ответы)
│   │   │       ├── entity/                 # JPA сущности (User, Student, Room, Request, Allocation)
│   │   │       ├── enums/                   # Перечисления (Role, Gender, RoomType, RequestStatus)
│   │   │       ├── exception/                # Глобальный обработчик ошибок, кастомные исключения
│   │   │       ├── mapper/                    # Мапперы (например, MapStruct)
│   │   │       ├── repository/                 # Интерфейсы Spring Data JPA
│   │   │       ├── security/                    # JWT фильтры, UserDetailsService, JwtUtils
│   │   │       ├── service/                      # Бизнес-логика (приоритет, совместимость, распределение)
│   │   │       └── util/                         # Вспомогательные утилиты
│   │   └── resources/
│   │       ├── db/
│   │       │   └── changelog/               # Liquibase миграции (v1.0/..., master.xml)
│   │       ├── api/                          # OpenAPI спецификация (openapi.yaml) – опционально
│   │       ├── application.yml                # Основная конфигурация приложения
│   │       ├── application-dev.yml             # Профиль разработки
│   │       ├── application-test.yml            # Профиль для тестов
│   │       └── application-prod.yml            # Профиль продакшн
│   └── test/
│       ├── java/                               # Модульные и интеграционные тесты
│       └── resources/                           # Тестовые ресурсы
├── .gitignore
├── LICENSE
├── README.md
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

 Требования к окружению
- Java 17 или выше
- Maven 3.6+
- PostgreSQL 14+ (для локального запуска)
- Docker (для запуска через контейнеры и интеграционных тестов)

---

 Настройка базы данных

 Локально
1. Создайте базу данных и пользователя PostgreSQL:
```sql
CREATE DATABASE dormitory_db;
CREATE USER dorm_user WITH PASSWORD 'dorm_pass';
GRANT ALL PRIVILEGES ON DATABASE dormitory_db TO dorm_user;
```
2. Убедитесь, что параметры подключения в `src/main/resources/application.yml` совпадают:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dormitory_db
    username: dorm_user
    password: dorm_pass
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
```
3. При необходимости создайте файл `application-local.yml` (игнорируемый git) для локальных настроек.

---

Сборка и запуск

Локально
1. Сборка проекта (в корневой папке):
```bash
mvn clean install
```
2. Применение миграций Liquibase (если не включено auto-ddl):
```bash
mvn liquibase:update
```
3. Запуск приложения:
```bash
mvn spring-boot:run
```
После запуска API будет доступно по адресу `http://localhost:8080`.

Через Docker
В корне проекта находится `docker-compose.yml`, который поднимает PostgreSQL и само приложение.
```bash
docker-compose up -d
```
Приложение будет доступно на `http://localhost:8080`.

---

Тестирование

- Модульные тесты (сервисы, утилиты):
```bash
mvn test
```
- Интеграционные тесты (контроллеры, с Testcontainers) – выполняются при `mvn verify` или отдельно:
```bash
mvn failsafe:integration-test
```
- Запуск всех тестов:
```bash
mvn verify
```

---

Документация API

После запуска приложения документация в формате OpenAPI доступна:
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Swagger UI позволяет не только просматривать эндпоинты, но и выполнять тестовые запросы (с подстановкой JWT токена).

---

Основные эндпоинты

| Метод | Эндпоинт                          | Роль         | Описание |
|-------|-----------------------------------|--------------|----------|
| POST  | `/api/v1/auth/register`           | Неавториз.   | Регистрация студента |
| POST  | `/api/v1/auth/login`              | Неавториз.   | Вход, получение JWT |
| GET   | `/api/v1/students/me`             | Студент      | Профиль текущего студента |
| PUT   | `/api/v1/students/me`             | Студент      | Обновление профиля |
| GET   | `/api/v1/rooms`                   | Студент, Комендант | Список комнат с фильтрацией |
| POST  | `/api/v1/requests`                | Студент      | Подача заявки |
| GET   | `/api/v1/requests/my`             | Студент      | Свои заявки |
| DELETE| `/api/v1/requests/{id}`           | Студент      | Отмена заявки (если статус позволяет) |
| GET   | `/api/v1/admin/requests`          | Комендант    | Все заявки с фильтрацией |
| POST  | `/api/v1/admin/allocate/run`      | Комендант    | Запуск алгоритма распределения (предварительный результат) |
| POST  | `/api/v1/admin/allocate/confirm`  | Комендант    | Подтверждение распределения и публикация |
| POST  | `/api/v1/admin/rooms`             | Комендант    | Добавление новой комнаты |
| PUT   | `/api/v1/admin/rooms/{id}`        | Комендант    | Редактирование комнаты |
| DELETE| `/api/v1/admin/rooms/{id}`        | Комендант    | Удаление комнаты (только если свободна) |

Полное описание всех параметров и моделей см. в Swagger UI.

---

Примеры запросов

Регистрация студента
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ivanov@example.com",
    "password": "secret123",
    "firstName": "Иван",
    "lastName": "Иванов",
    "middleName": "Иванович",
    "faculty": "ФИТ",
    "course": 2,
    "groupName": "ПИ-21-1",
    "gender": "MALE",
    "country": "Россия",
    "averageScore": 4.8,
    "benefits": false
  }'
```
Ответ:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Вход в систему
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ivanov@example.com",
    "password": "secret123"
  }'
```
Получение списка комнат (с фильтром по типу)
```bash
curl -X GET "http://localhost:8080/api/v1/rooms?type=MALE&building=1" \
  -H "Authorization: Bearer <token>"
```

Подача заявки с предпочитаемыми соседями
bash
curl -X POST http://localhost:8080/api/v1/requests \
-H "Authorization: Bearer <token>" \
-H "Content-Type: application/json" \
-d '{
"preferredRoommates": [2, 5]
}'


---

Бизнес-логика (алгоритмы)

Расчёт приоритета студента
- Вход: средний балл (0..100), наличие льгот (boolean).
- Формула: `priority = averageScore + (benefits ? 50 : 0)`. При равенстве приоритет отдаётся раньше поданной заявке (учитывается `createdAt`).

Проверка совместимости студента с комнатой и соседями
Жёсткие правила:
1. Пол – комната должна соответствовать полу студента (если не MIXED).
2. Вместимость – в комнате должно быть свободное место (`room.capacity > currentOccupants`).
3. Страна – все уже заселённые в комнату студенты и новый студент должны иметь одну и ту же страну (если студент не указал согласие на интернациональное соседство – функционал может быть добавлен позже).
4. Предпочитаемые соседи – если студент указал список, все эти студенты уже должны быть в комнате (или добавляются одновременно) и соответствовать правилу страны.

Алгоритм автоматического распределения (жадный)
1. Все активные заявки (статус `PENDING`) сортируются по убыванию приоритета (при равенстве – по дате подачи).
2. Для каждой заявки перебираются комнаты, подходящие по типу, вместимости и текущему составу (с проверкой совместимости).
3. При нахождении подходящей комнаты студент закрепляется за ней (создаётся временная запись распределения).
4. Не распределённые студенты попадают в отчёт.
5. После ручного подтверждения комендантом распределение фиксируется в таблице `allocations`, статусы заявок меняются на `ALLOCATED`, и студенты видят результат.



Профили запуска

- `dev` – профиль разработки (по умолчанию). Использует локальную БД, подробное логирование, автогенерация схемы Hibernate (если нужно отключить Liquibase).
- `test` – для интеграционных тестов (Testcontainers, отдельная БД).
- `prod` – продакшн-профиль (минимум логов, внешняя БД, строгие настройки безопасности).

Активация профиля:
```bash
java -jar target/dormitory-*.jar --spring.profiles.active=prod
```

---

Миграции базы данных

Управление схемой БД осуществляется через Liquibase. Миграции находятся в:
```
src/main/resources/db/changelog/
```
- `db.changelog-master.xml` – корневой файл, подключающий версионные миграции.
- Папка `v1.0/` – содержит изменения первой версии (создание таблиц, индексов, начальные данные).

Применение миграций вручную:
```bash
mvn liquibase:update
```
При запуске приложения миграции применяются автоматически, если `spring.liquibase.enabled=true` (по умолчанию).

---

Разработка

Добавление новой сущности
1. Создайте JPA-сущность в пакете `entity`.
2. Добавьте интерфейс репозитория в `repository`.
3. Реализуйте бизнес-логику в `service`.
4. Создайте Liquibase-миграцию для новой таблицы.
5. Создайте DTO, маппер и контроллер.
6. Опишите новые эндпоинты в OpenAPI спецификации (если используете файл) или аннотациями.
7. Напишите тесты (модульные для сервиса, интеграционные для контроллера).

Настройка IDE
- Рекомендуется IntelliJ IDEA с плагинами:
    - Lombok
    - Spring Boot
    - JPA Buddy (опционально)
    - SonarLint (для анализа кода)
- Для работы с БД – встроенные инструменты IDEA или DBeaver.
