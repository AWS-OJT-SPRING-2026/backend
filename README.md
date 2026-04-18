# 🎓 SlotHubEdu Backend

> **SlotHubEdu API Server**
>
> Backend RESTful API cho hệ thống e-learning SlotHubEdu, xây dựng bằng Spring Boot 3.5.6 + Java 17 + PostgreSQL.

---

## 📋 Mục lục

- [Tổng quan](#-tổng-quan)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt & Khởi chạy](#-cài-đặt--khởi-chạy)
- [Cấu hình môi trường](#-cấu-hình-môi-trường)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [API Endpoints](#-api-endpoints)
- [Xác thực & Phân quyền](#-xác-thực--phân-quyền)
- [Cơ sở dữ liệu](#-cơ-sở-dữ-liệu)
- [Quy ước phát triển](#-quy-ước-phát-triển)
- [Docker & Triển khai](#-docker--triển-khai)
- [Testing](#-testing)

---

## 🌟 Tổng quan

SlotHubEdu Backend là API server cung cấp toàn bộ logic nghiệp vụ cho nền tảng e-learning. Hệ thống hỗ trợ ba vai trò chính (**Admin**, **Teacher**, **Student**) với đầy đủ tính năng quản lý lớp học, bài tập, kiểm tra, thời khóa biểu, thông báo, và AI chat.

**Base URL:** `http://localhost:8080/api`

---

## 🛠 Công nghệ sử dụng

| Mục | Công nghệ | Phiên bản |
|-----|-----------|-----------|
| **Framework** | Spring Boot | 3.5.6 |
| **Language** | Java | 17 |
| **Database** | PostgreSQL | 15+ |
| **ORM** | Spring Data JPA (Hibernate) | – |
| **Security** | Spring Security + OAuth2 Resource Server | – |
| **Authentication** | JWT + AWS Cognito | – |
| **DTO Mapping** | MapStruct | 1.5.5 |
| **Boilerplate** | Lombok | – |
| **Cloud Storage** | AWS S3 | – |
| **User Management** | AWS Cognito | – |
| **Image Upload** | Cloudinary | 1.38.0 |
| **Email** | Spring Mail (Gmail SMTP) | – |
| **File Export** | Apache POI (Excel) + OpenPDF (PDF) | 5.2.5 / 1.3.39 |
| **Vector DB** | pgvector | 0.1.6 |
| **Monitoring** | Spring Actuator | – |
| **Build Tool** | Maven | – |
| **Containerization** | Docker | – |
| **CI/CD** | AWS CodeBuild + ECR | – |

---

## 📦 Yêu cầu hệ thống

- **Java** 17 trở lên
- **Maven** 3.9+ (hoặc dùng Maven Wrapper `./mvnw`)
- **PostgreSQL** 15+ đang chạy tại `localhost:5432`
- **AWS Account** (cho S3, Cognito) – tùy chọn cho development local

---

## 🚀 Cài đặt & Khởi chạy

### Cách 1: Chạy trực tiếp

```bash
# 1. Di chuyển vào thư mục BE
cd BE

# 2. Tạo file cấu hình môi trường
cp .env.example .env   # Tạo từ template hoặc tự tạo file .env

# 3. Khởi động PostgreSQL (nếu chưa chạy)
# Xem phần Docker bên dưới để chạy PostgreSQL qua Docker

# 4. Khởi chạy application
./mvnw spring-boot:run          # Linux/Mac
mvnw.cmd spring-boot:run        # Windows
```

### Cách 2: Chạy với Docker Compose

```bash
# Khởi động PostgreSQL + pgAdmin
docker-compose up -d

# Sau đó chạy application
./mvnw spring-boot:run
```

Server sẽ khởi chạy tại **`http://localhost:8080/api`**

### Tài khoản mặc định (Seed Data)

Khi khởi chạy lần đầu, `DataInit.java` sẽ tự động tạo dữ liệu mẫu:

| Vai trò | Username | Password |
|---------|----------|----------|
| Admin | `admin` | `admin123` |
| Teacher | `teacher1` | `teacher123` |

> Seed data chạy idempotent – kiểm tra `isDataAlreadyInitialized()` trước khi insert.

---

## ⚙️ Cấu hình môi trường

Tạo file `.env` tại thư mục `BE/` với các biến sau:

```env
# ========================
# Database Configuration
# ========================
DB_HOST=localhost
DB_NAME=EDU_CARE
DB_USERNAME=postgres
DB_PASSWORD=123456

# ========================
# AWS Configuration
# ========================
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# ========================
# AWS Cognito
# ========================
COGNITO_ISSUER_URI=https://cognito-idp.ap-southeast-1.amazonaws.com/your-pool-id
COGNITO_APP_CLIENT_ID=your_client_id
COGNITO_USER_POOL_ID=your_pool_id

# ========================
# Email (Gmail SMTP)
# ========================
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

### Cấu hình `application.yml` chính

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
  jpa:
    hibernate:
      ddl-auto: update    # ⚠️ KHÔNG dùng create-drop
    properties:
      hibernate:
        jdbc:
          time_zone: Asia/Ho_Chi_Minh
  jackson:
    time-zone: Asia/Ho_Chi_Minh
```

> **⚠️ Quan trọng:** Schema được quản lý bởi `hibernate.ddl-auto: update`. **Tuyệt đối không** đổi sang `create-drop` trong dự án này.

---

## 📁 Cấu trúc dự án

```
BE/
├── src/
│   └── main/
│       ├── java/ojt/aws/educare/
│       │   ├── EduCareApplication.java      # Main class
│       │   ├── configuration/               # Cấu hình hệ thống
│       │   │   ├── SecurityConfig.java          # Security & CORS
│       │   │   ├── CustomJwtDecoder.java         # JWT decoder
│       │   │   ├── JwtAuthenticationEntryPoint.java # Xử lý 401
│       │   │   ├── CognitoUserSyncFilter.java   # Đồng bộ user Cognito
│       │   │   ├── CognitoUserSyncService.java  # Logic sync Cognito
│       │   │   ├── CognitoAdminConfig.java      # Cognito admin client
│       │   │   ├── CognitoAccessDeniedHandler.java # Xử lý 403
│       │   │   ├── CurrentUserProvider.java     # Lấy user hiện tại
│       │   │   ├── DataInit.java                # Seed data ban đầu
│       │   │   ├── EncoderConfig.java           # Password encoder
│       │   │   ├── S3Config.java                # AWS S3 client
│       │   │   └── TimezoneVerificationConfig.java # Kiểm tra timezone
│       │   ├── controller/                  # REST Controllers
│       │   │   ├── AuthenticationController.java    # Đăng nhập/Đăng xuất
│       │   │   ├── UserController.java              # CRUD người dùng
│       │   │   ├── ClassroomController.java         # Quản lý lớp học
│       │   │   ├── AssignmentController.java        # Bài tập & kiểm tra
│       │   │   ├── TimetableController.java         # Thời khóa biểu
│       │   │   ├── NotificationController.java      # Thông báo
│       │   │   ├── StudentController.java           # API học sinh
│       │   │   ├── TeacherController.java           # API giáo viên
│       │   │   ├── DocumentController.java          # Tài liệu
│       │   │   ├── FeedbackController.java          # Phản hồi bài nộp
│       │   │   ├── ChatSessionController.java       # AI Chat
│       │   │   ├── QuizDraftController.java         # Bản nháp đề thi
│       │   │   ├── StudentDashboardController.java  # Dashboard HS
│       │   │   ├── StudentMaterialController.java   # Tài liệu HS
│       │   │   ├── StudentNoteController.java       # Ghi chú HS
│       │   │   ├── SubjectController.java           # Môn học
│       │   │   ├── TeacherQuestionController.java   # Câu hỏi GV
│       │   │   ├── UserSettingsController.java      # Cài đặt user
│       │   │   ├── WeeklyProgressController.java    # Tiến độ tuần
│       │   │   └── QuoteController.java             # Câu nói hay
│       │   ├── service/                     # Service interfaces
│       │   │   ├── Impl/                        # Service implementations
│       │   │   ├── AuthenticationService.java
│       │   │   ├── UserService.java
│       │   │   ├── ClassroomService.java
│       │   │   ├── AssignmentService.java
│       │   │   ├── TimetableService.java
│       │   │   ├── NotificationService.java
│       │   │   ├── StudentService.java
│       │   │   ├── TeacherService.java
│       │   │   ├── FeedbackService.java
│       │   │   ├── ChatSessionService.java
│       │   │   ├── EmailService.java
│       │   │   ├── S3UploadService.java
│       │   │   └── ...
│       │   ├── entity/                      # JPA Entities (41 entities)
│       │   │   ├── User.java
│       │   │   ├── Role.java
│       │   │   ├── Student.java
│       │   │   ├── Teacher.java
│       │   │   ├── Classroom.java
│       │   │   ├── Assignment.java
│       │   │   ├── Question.java
│       │   │   ├── Submission.java
│       │   │   ├── Timetable.java
│       │   │   ├── Notification.java
│       │   │   └── ...
│       │   ├── repository/                  # Spring Data JPA Repositories
│       │   │   ├── UserRepository.java
│       │   │   ├── ClassroomRepository.java
│       │   │   ├── AssignmentRepository.java
│       │   │   ├── TimetableRepository.java
│       │   │   └── ... (25 repositories)
│       │   ├── dto/                         # Data Transfer Objects
│       │   │   ├── request/                     # Request DTOs
│       │   │   └── response/                    # Response DTOs
│       │   ├── mapper/                      # MapStruct Mappers (22 mappers)
│       │   │   ├── UserMapper.java
│       │   │   ├── ClassroomMapper.java
│       │   │   ├── AssignmentMapper.java
│       │   │   └── ...
│       │   ├── exception/                   # Exception handling
│       │   │   ├── ErrorCode.java               # Tất cả mã lỗi
│       │   │   ├── AppException.java            # Custom exception
│       │   │   └── GlobalExceptionHandler.java  # Exception handler toàn cục
│       │   └── scheduler/                   # Scheduled tasks
│       │       └── NotificationScheduler.java   # Gửi thông báo tự động
│       └── resources/
│           └── application.yml              # Cấu hình chính
├── SQL/                                     # SQL scripts
├── deploy/                                  # Deployment configs
├── .env                                     # Biến môi trường (không commit)
├── Dockerfile                               # Multi-stage Docker build
├── docker-compose.yml                       # PostgreSQL + pgAdmin
├── buildspec.yml                            # AWS CodeBuild spec
├── pom.xml                                  # Maven dependencies
├── mvnw / mvnw.cmd                          # Maven Wrapper
└── HELP.md                                  # Spring Boot help
```

---

## 🏗 Kiến trúc hệ thống

### Kiến trúc lớp (Layered Architecture)

```
┌─────────────────────────────────────────┐
│              Controller                  │  ← Nhận HTTP request
│  (REST endpoints, validation)           │
├─────────────────────────────────────────┤
│           Service Interface              │  ← Business logic contract
├─────────────────────────────────────────┤
│        Service Implementation            │  ← Business logic
│           (service/Impl/)               │
├─────────────────────────────────────────┤
│              Repository                  │  ← Data access (JPA)
├─────────────────────────────────────────┤
│               Entity                     │  ← Domain model
├─────────────────────────────────────────┤
│            PostgreSQL DB                 │  ← Persistence
└─────────────────────────────────────────┘
```

### Luồng xử lý Request

```
Client Request
    → SecurityFilter (JWT validation)
    → CognitoUserSyncFilter (sync user nếu cần)
    → Controller
    → Service (business logic)
    → Repository (database query)
    → MapStruct Mapper (Entity ↔ DTO)
    → ApiResponse<T> wrapper
    → JSON Response
```

### Response Envelope

Tất cả API response đều được bọc trong `ApiResponse<T>`:

```java
// Cách sử dụng trong Controller
return ApiResponse.<MyResponse>builder()
    .result(result)
    .build();
```

```json
{
  "code": 1000,
  "message": "Success",
  "result": { ... }
}
```

### Xử lý lỗi

Throw `AppException` ở bất kỳ đâu, `GlobalExceptionHandler` sẽ tự động catch và trả về response phù hợp:

```java
throw new AppException(ErrorCode.USER_NOT_FOUND);
```

> **Quy tắc:** Chỉ thêm error code mới vào file `exception/ErrorCode.java`.

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/auth/login` | Đăng nhập | ❌ |
| `POST` | `/auth/logout` | Đăng xuất | ✅ |
| `POST` | `/auth/introspect` | Kiểm tra token | ❌ |

### User Management

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/users` | Tạo user mới (đăng ký) | ❌ |
| `GET` | `/users` | Danh sách users | ✅ Admin |
| `GET` | `/users/{id}` | Thông tin user | ✅ |
| `PUT` | `/users/{id}` | Cập nhật user | ✅ |
| `DELETE` | `/users/{id}` | Xóa user | ✅ Admin |
| `GET` | `/users/my-info` | Thông tin bản thân | ✅ |

### Classroom

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/classrooms` | Danh sách lớp | ✅ |
| `POST` | `/classrooms` | Tạo lớp mới | ✅ Admin |
| `PUT` | `/classrooms/{id}` | Cập nhật lớp | ✅ Admin |
| `DELETE` | `/classrooms/{id}` | Xóa lớp | ✅ Admin |

### Assignment & Test

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/assignments` | Danh sách bài tập | ✅ |
| `POST` | `/assignments` | Tạo bài tập/kiểm tra | ✅ Teacher |
| `PUT` | `/assignments/{id}` | Cập nhật bài tập | ✅ Teacher |
| `DELETE` | `/assignments/{id}` | Xóa bài tập | ✅ Teacher |

### Timetable

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/timetable` | Danh sách thời khóa biểu | ✅ |
| `POST` | `/timetable` | Tạo TKB mới | ✅ Admin |
| `PUT` | `/timetable/{id}` | Cập nhật TKB | ✅ Admin |
| `DELETE` | `/timetable/{id}` | Xóa TKB | ✅ Admin |

### Notification

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/notifications` | Danh sách thông báo | ✅ |
| `POST` | `/notifications` | Tạo thông báo | ✅ Teacher |
| `PUT` | `/notifications/{id}/read` | Đánh dấu đã đọc | ✅ |

### Others

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET/POST` | `/feedback/**` | Phản hồi bài nộp | ✅ |
| `GET/POST` | `/documents/**` | Tài liệu | ✅ |
| `GET/POST` | `/chat-sessions/**` | AI Chat | ✅ |
| `GET/POST` | `/quiz-drafts/**` | Bản nháp đề thi | ✅ |
| `GET` | `/subjects` | Danh sách môn học | ✅ |
| `GET/POST` | `/student-notes/**` | Ghi chú học sinh | ✅ |
| `GET` | `/student-dashboard/**` | Dashboard HS | ✅ |
| `GET/POST` | `/weekly-progress/**` | Tiến độ tuần | ✅ |
| `GET` | `/actuator/health` | Health check | ❌ |

> **Lưu ý:** Danh sách trên là tổng quan. Xem chi tiết tại từng Controller.
> Public endpoints được whitelist tại `SecurityConfig.PUBLIC_ENDPOINTS`.

---

## 🔐 Xác thực & Phân quyền

### Luồng xác thực

```
Login Request → AuthenticationController
    → Cognito validate / Local JWT
    → Trả về JWT token
    → Client lưu token
    → Gửi kèm trong header: Authorization: Bearer <token>
```

### Vai trò (Roles)

| Role | Mô tả | Quyền |
|------|-------|-------|
| `ADMIN` | Quản trị viên | Toàn quyền hệ thống |
| `TEACHER` | Giáo viên | Quản lý lớp, tạo bài, chấm điểm |
| `STUDENT` | Học sinh | Học tập, làm bài, xem kết quả |

### Cấu hình Security

- **JWT Decoder:** `CustomJwtDecoder.java` – Giải mã và xác thực token
- **Entry Point:** `JwtAuthenticationEntryPoint.java` – Trả về 401 khi token không hợp lệ
- **Method Security:** `@PreAuthorize` annotation trên controller methods
- **Cognito Sync:** `CognitoUserSyncFilter` tự động đồng bộ thông tin user từ Cognito

---

## 🗄 Cơ sở dữ liệu

### Thông tin kết nối

| Thuộc tính | Giá trị |
|------------|---------|
| **Host** | `localhost` |
| **Port** | `5432` |
| **Database** | `EDU_CARE` |
| **Username** | `postgres` |
| **Password** | `123456` |
| **Timezone** | `Asia/Ho_Chi_Minh` |

### Các Entity chính

```
User ──┬── Student ──── ClassMember ──── Classroom
       │                                    │
       └── Teacher ─────────────────────────┘
                                            │
Classroom ──── Assignment ──── Question ──── Answer
                   │
                   └── Submission ──── SubmissionAnswer
                   
Classroom ──── Timetable
Classroom ──── ClassroomMaterial

User ──── Notification
User ──── UserSettings
User ──── AIChatSession

Student ──── StudentNote
Student ──── LearningProfile

Subject ──── QuestionBank ──── Question

Book ──── Chapter ──── Section ──── Subsection
                         │
                         └── ContentBlock

Teacher ──── Feedback ──── Submission
```

### Docker Compose (PostgreSQL)

```bash
# Khởi động PostgreSQL + pgAdmin
docker-compose up -d

# Truy cập pgAdmin: http://localhost:5050
# Email: admin@educare.com
# Password: admin
```

> **⚠️ Lưu ý:** Schema tự động cập nhật qua `hibernate.ddl-auto: update`. Không cần chạy migration thủ công.

---

## 📐 Quy ước phát triển

### Quy tắc bắt buộc

1. **Layered Architecture:** `Controller → Service (interface) → Impl → Repository → Entity`
2. **Response envelope:** Luôn trả về `ApiResponse<T>` từ Controller
3. **Error handling:** Throw `AppException(ErrorCode.XXX)`, không tự tạo exception class mới
4. **DTO mapping:** Sử dụng MapStruct interface trong `mapper/`, không map thủ công
5. **Error codes:** Chỉ thêm mới vào `exception/ErrorCode.java`

### Naming Convention

| Loại | Convention | Ví dụ |
|------|-----------|-------|
| Entity | PascalCase, số ít | `Classroom`, `Assignment` |
| Controller | `{Entity}Controller` | `ClassroomController` |
| Service Interface | `{Entity}Service` | `ClassroomService` |
| Service Impl | `{Entity}ServiceImpl` | `ClassroomServiceImpl` |
| Repository | `{Entity}Repository` | `ClassroomRepository` |
| Mapper | `{Entity}Mapper` | `ClassroomMapper` |
| Request DTO | `{Action}{Entity}Request` | `CreateClassroomRequest` |
| Response DTO | `{Entity}Response` | `ClassroomResponse` |

### Lưu ý MapStruct

- Dùng `@Mapping(target = "field", ignore = true)` để bỏ qua field không liên quan
- **Luôn rebuild** sau khi thay đổi mapper: `./mvnw clean compile`
- Lombok + MapStruct annotation processors đã được cấu hình trong `pom.xml`

### Timezone

- JVM timezone: `Asia/Ho_Chi_Minh` (verify bởi `TimezoneVerificationConfig`)
- Hibernate JDBC timezone: `Asia/Ho_Chi_Minh`
- Jackson serialization timezone: `Asia/Ho_Chi_Minh`

---

## 🐳 Docker & Triển khai

### Dockerfile (Multi-stage Build)

```dockerfile
# Build stage
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /app/target/BE-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build & Run với Docker

```bash
# Build image
docker build -t educare-backend .

# Chạy container
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_NAME=EDU_CARE \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=123456 \
  educare-backend
```

### CI/CD (AWS CodeBuild)

Pipeline được cấu hình qua `buildspec.yml`:
1. **Pre-build:** Login vào Amazon ECR
2. **Build:** Build Docker image
3. **Post-build:** Push image lên ECR với tag `latest` + commit hash

---

## 🧪 Testing

```bash
# Chạy tất cả tests
./mvnw test

# Build mà bỏ qua tests
./mvnw clean package -DskipTests

# Chạy một test cụ thể
./mvnw test -Dtest=UserServiceTest
```

### Health Check

```bash
# Kiểm tra trạng thái server
curl http://localhost:8080/api/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

---

## 📞 Liên hệ

- **Project:** SlotHubEdu – OJT Program
- **Team:** AWS OJT SP26
- **Group:** `ojt.sp26.aws`
- **Frontend:** Xem [FE/README.md](../FE/README.md)

---

<p align="center">
  <b>© 2026 SlotHubEdu. All rights reserved.</b>
</p>
