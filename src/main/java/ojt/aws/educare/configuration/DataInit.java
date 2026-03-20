package ojt.aws.educare.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInit {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassMemberRepository classMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TimetableRepository timetableRepository;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            log.info("Bắt đầu kiểm tra và khởi tạo dữ liệu mẫu...");

            // Kiểm tra nếu đã có dữ liệu thì không khởi tạo lại
            if (isDataAlreadyInitialized()) {
                log.info("Dữ liệu đã được khởi tạo trước đó. Bỏ qua quá trình khởi tạo.");
                return;
            }

            // 1. Tạo Roles
            Role adminRole = createRoleIfNotExists("ADMIN", "Quản trị viên hệ thống");
            Role teacherRole = createRoleIfNotExists("TEACHER", "Giáo viên");
            Role studentRole = createRoleIfNotExists("STUDENT", "Học sinh");

            // 2. Tạo Users
            User admin = createUserIfNotExists("admin", "admin123", "Admin System",
                    "admin@educare.com", "0999888777", adminRole);

            // Tạo 9 giáo viên (tăng lên để có đủ giáo viên cho các lớp)
            List<User> teacherUsers = createTeacherUsers(teacherRole);

            // 3. Tạo Teachers (chi tiết)
            List<Teacher> teacherDetails = createTeacherDetails(teacherUsers);

            // 4. Tạo Students với đầy đủ thông tin
            List<Student> studentDetails = createStudentsWithFullInfo(studentRole);

            // 5. Tạo Subjects
            List<Subject> subjects = createSubjects();

            // 6. Tạo Classrooms với nhiều giáo viên hơn
            List<Classroom> classrooms = createClassrooms(subjects, teacherDetails);

            // 7. Tạo ClassMembers - phân lớp cho học sinh
            addStudentsToClasses(studentDetails, classrooms);

            // 8. Tạo Timetables - lịch học không trùng giờ cho giáo viên
            createTimetablesWithoutConflict(classrooms, teacherDetails);

            log.info("Khởi tạo dữ liệu mẫu thành công!");
            log.info("- Roles: 3 roles (ADMIN, TEACHER, STUDENT)");
            log.info("- Users: 1 admin, {} teachers, {} students", teacherUsers.size(), studentDetails.size());
            log.info("- Subjects: {} môn học", subjects.size());
            log.info("- Classrooms: {} lớp học", classrooms.size());
            log.info("- Timetables: Đã tạo lịch học đảm bảo mỗi giáo viên không dạy 2 lớp cùng lúc");
        };
    }

    /**
     * Tạo danh sách user cho giáo viên
     */
    private List<User> createTeacherUsers(Role teacherRole) {
        List<User> teacherUsers = new ArrayList<>();

        String[][] teacherInfo = {
                {"teacher1", "Nguyễn Văn An", "teacher1@educare.com", "0912345678"},
                {"teacher2", "Trần Thị Bình", "teacher2@educare.com", "0923456789"},
                {"teacher3", "Lê Văn Cường", "teacher3@educare.com", "0934567890"},
                {"teacher4", "Phạm Thị Dung", "teacher4@educare.com", "0945678901"},
                {"teacher5", "Hoàng Văn Em", "teacher5@educare.com", "0956789012"},
                {"teacher6", "Vũ Thị Phương", "teacher6@educare.com", "0967890123"},
                {"teacher7", "Đặng Văn Giang", "teacher7@educare.com", "0978901234"},
                {"teacher8", "Bùi Thị Hà", "teacher8@educare.com", "0989012345"},
                {"teacher9", "Ngô Văn Inh", "teacher9@educare.com", "0990123456"}
        };

        for (String[] info : teacherInfo) {
            User user = createUserIfNotExists(
                    info[0], "teacher123", info[1], info[2], info[3], teacherRole
            );
            teacherUsers.add(user);
        }

        return teacherUsers;
    }

    /**
     * Tạo chi tiết cho giáo viên
     */
    private List<Teacher> createTeacherDetails(List<User> teacherUsers) {
        List<Teacher> teachers = new ArrayList<>();

        String[][] teacherDetails = {
                {"Nguyễn Văn An", "Toán học", "MALE", "true", "1985-05-15"},
                {"Trần Thị Bình", "Ngữ văn", "FEMALE", "true", "1988-08-20"},
                {"Lê Văn Cường", "Tiếng Anh", "MALE", "false", "1990-03-10"},
                {"Phạm Thị Dung", "Vật lý", "FEMALE", "false", "1987-11-25"},
                {"Hoàng Văn Em", "Hóa học", "MALE", "false", "1989-07-18"},
                {"Vũ Thị Phương", "Sinh học", "FEMALE", "false", "1991-09-22"},
                {"Đặng Văn Giang", "Lịch sử", "MALE", "false", "1986-04-30"},
                {"Bùi Thị Hà", "Địa lý", "FEMALE", "false", "1992-12-12"},
                {"Ngô Văn Inh", "Tin học", "MALE", "false", "1988-06-08"}
        };

        for (int i = 0; i < teacherUsers.size(); i++) {
            String[] details = teacherDetails[i];
            Teacher teacher = createTeacherDetailIfNotExists(
                    teacherUsers.get(i),
                    details[0],
                    details[1],
                    details[2],
                    Boolean.parseBoolean(details[3]),
                    LocalDate.parse(details[4])
            );
            teachers.add(teacher);
        }

        return teachers;
    }

    /**
     * Tạo danh sách môn học
     */
    private List<Subject> createSubjects() {
        return Arrays.asList(
                createSubjectIfNotExists("Toán 10", "Môn Toán lớp 10", 10, 4),
                createSubjectIfNotExists("Toán 11", "Môn Toán lớp 11", 11, 4),
                createSubjectIfNotExists("Toán 12", "Môn Toán lớp 12", 12, 4),
                createSubjectIfNotExists("Ngữ văn 10", "Môn Ngữ văn lớp 10", 10, 3),
                createSubjectIfNotExists("Ngữ văn 11", "Môn Ngữ văn lớp 11", 11, 3),
                createSubjectIfNotExists("Ngữ văn 12", "Môn Ngữ văn lớp 12", 12, 3),
                createSubjectIfNotExists("Tiếng Anh 10", "Môn Tiếng Anh lớp 10", 10, 3),
                createSubjectIfNotExists("Tiếng Anh 11", "Môn Tiếng Anh lớp 11", 11, 3),
                createSubjectIfNotExists("Tiếng Anh 12", "Môn Tiếng Anh lớp 12", 12, 3),
                createSubjectIfNotExists("Vật lý 10", "Môn Vật lý lớp 10", 10, 2),
                createSubjectIfNotExists("Vật lý 11", "Môn Vật lý lớp 11", 11, 2),
                createSubjectIfNotExists("Hóa học 10", "Môn Hóa học lớp 10", 10, 2),
                createSubjectIfNotExists("Hóa học 11", "Môn Hóa học lớp 11", 11, 2),
                createSubjectIfNotExists("Sinh học 10", "Môn Sinh học lớp 10", 10, 2),
                createSubjectIfNotExists("Lịch sử 10", "Môn Lịch sử lớp 10", 10, 2),
                createSubjectIfNotExists("Địa lý 10", "Môn Địa lý lớp 10", 10, 2),
                createSubjectIfNotExists("Tin học 10", "Môn Tin học lớp 10", 10, 2)
        );
    }

    /**
     * Tạo danh sách lớp học với phân bổ giáo viên hợp lý
     */
    private List<Classroom> createClassrooms(List<Subject> subjects, List<Teacher> teachers) {
        List<Classroom> classrooms = new ArrayList<>();

        // Phân bổ giáo viên cho các lớp (mỗi giáo viên dạy 2-3 lớp)
        classrooms.add(createClassroomIfNotExists("10A1", findSubject(subjects, "Toán 10"), teachers.get(0)));
        classrooms.add(createClassroomIfNotExists("10A2", findSubject(subjects, "Toán 10"), teachers.get(0)));
        classrooms.add(createClassroomIfNotExists("10B1", findSubject(subjects, "Ngữ văn 10"), teachers.get(1)));
        classrooms.add(createClassroomIfNotExists("10B2", findSubject(subjects, "Ngữ văn 10"), teachers.get(1)));
        classrooms.add(createClassroomIfNotExists("10C1", findSubject(subjects, "Tiếng Anh 10"), teachers.get(2)));
        classrooms.add(createClassroomIfNotExists("10C2", findSubject(subjects, "Tiếng Anh 10"), teachers.get(2)));

        classrooms.add(createClassroomIfNotExists("11A1", findSubject(subjects, "Toán 11"), teachers.get(0)));
        classrooms.add(createClassroomIfNotExists("11A2", findSubject(subjects, "Toán 11"), teachers.get(0)));
        classrooms.add(createClassroomIfNotExists("11B1", findSubject(subjects, "Ngữ văn 11"), teachers.get(1)));
        classrooms.add(createClassroomIfNotExists("11C1", findSubject(subjects, "Tiếng Anh 11"), teachers.get(2)));

        classrooms.add(createClassroomIfNotExists("12A1", findSubject(subjects, "Toán 12"), teachers.get(0)));
        classrooms.add(createClassroomIfNotExists("12B1", findSubject(subjects, "Ngữ văn 12"), teachers.get(1)));
        classrooms.add(createClassroomIfNotExists("12C1", findSubject(subjects, "Tiếng Anh 12"), teachers.get(2)));

        // Thêm các lớp cho giáo viên mới
        classrooms.add(createClassroomIfNotExists("10D1", findSubject(subjects, "Vật lý 10"), teachers.get(3)));
        classrooms.add(createClassroomIfNotExists("10D2", findSubject(subjects, "Vật lý 10"), teachers.get(3)));
        classrooms.add(createClassroomIfNotExists("11D1", findSubject(subjects, "Vật lý 11"), teachers.get(3)));

        classrooms.add(createClassroomIfNotExists("10E1", findSubject(subjects, "Hóa học 10"), teachers.get(4)));
        classrooms.add(createClassroomIfNotExists("10E2", findSubject(subjects, "Hóa học 10"), teachers.get(4)));
        classrooms.add(createClassroomIfNotExists("11E1", findSubject(subjects, "Hóa học 11"), teachers.get(4)));

        classrooms.add(createClassroomIfNotExists("10F1", findSubject(subjects, "Sinh học 10"), teachers.get(5)));
        classrooms.add(createClassroomIfNotExists("10F2", findSubject(subjects, "Sinh học 10"), teachers.get(5)));

        classrooms.add(createClassroomIfNotExists("10G1", findSubject(subjects, "Lịch sử 10"), teachers.get(6)));
        classrooms.add(createClassroomIfNotExists("10G2", findSubject(subjects, "Lịch sử 10"), teachers.get(6)));

        classrooms.add(createClassroomIfNotExists("10H1", findSubject(subjects, "Địa lý 10"), teachers.get(7)));
        classrooms.add(createClassroomIfNotExists("10H2", findSubject(subjects, "Địa lý 10"), teachers.get(7)));

        classrooms.add(createClassroomIfNotExists("10I1", findSubject(subjects, "Tin học 10"), teachers.get(8)));
        classrooms.add(createClassroomIfNotExists("10I2", findSubject(subjects, "Tin học 10"), teachers.get(8)));

        return classrooms;
    }

    /**
     * Tìm subject theo tên
     */
    private Subject findSubject(List<Subject> subjects, String name) {
        return subjects.stream()
                .filter(s -> s.getSubjectName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn: " + name));
    }

    /**
     * Tạo lịch học đảm bảo không trùng giờ cho giáo viên
     */
    private void createTimetablesWithoutConflict(List<Classroom> classrooms, List<Teacher> teachers) {
        LocalDateTime now = LocalDateTime.now();

        // Map để theo dõi lịch đã đăng ký của từng giáo viên - dùng Integer thay vì Long
        Map<Integer, Set<LocalDateTime>> teacherSchedules = new HashMap<>();

        // Các khung giờ học cố định trong tuần
        LocalTime[][] timeSlots = {
                {LocalTime.of(7, 0), LocalTime.of(8, 30)},  // Sáng: 7h-8h30
                {LocalTime.of(8, 45), LocalTime.of(10, 15)}, // Sáng: 8h45-10h15
                {LocalTime.of(10, 30), LocalTime.of(12, 0)}, // Sáng: 10h30-12h
                {LocalTime.of(13, 30), LocalTime.of(15, 0)}, // Chiều: 13h30-15h
                {LocalTime.of(15, 15), LocalTime.of(16, 45)} // Chiều: 15h15-16h45
        };

        // Các ngày trong tuần (thứ 2 - thứ 6)
        int[] daysOfWeek = {1, 2, 3, 4, 5}; // Monday = 1, Friday = 5

        // Tạo lịch cho 4 tuần
        for (int week = 0; week < 4; week++) {
            for (int day : daysOfWeek) {
                for (LocalTime[] slot : timeSlots) {
                    // Random chọn một lớp để xếp lịch vào khung giờ này
                    for (Classroom classroom : shuffleList(classrooms)) {
                        // SỬA: teacherId là Integer (không cần longValue)
                        Integer teacherId = classroom.getTeacher().getTeacherID();

                        LocalDateTime startTime = now.plusWeeks(week)
                                .with(java.time.DayOfWeek.of(day))
                                .withHour(slot[0].getHour())
                                .withMinute(slot[0].getMinute())
                                .withSecond(0).withNano(0);

                        LocalDateTime endTime = startTime
                                .withHour(slot[1].getHour())
                                .withMinute(slot[1].getMinute());

                        // Kiểm tra giáo viên có rảnh không
                        if (isTeacherAvailable(teacherSchedules, teacherId, startTime)) {
                            // Kiểm tra lớp này đã có lịch chưa
                            if (!timetableRepository.existsByClassroomAndStartTime(classroom, startTime)) {
                                createTimetableIfNotExists(
                                        classroom,
                                        classroom.getTeacher(),
                                        startTime,
                                        endTime,
                                        determineTopic(classroom, "Tuần " + (week + 1) + " - Tiết " + (Arrays.asList(timeSlots).indexOf(slot) + 1)),
                                        getStatusForTime(startTime)
                                );

                                // Đánh dấu lịch đã đăng ký
                                teacherSchedules
                                        .computeIfAbsent(teacherId, k -> new HashSet<>())
                                        .add(startTime);

                                break; // Thoát vòng lặp classroom sau khi xếp được lịch
                            }
                        }
                    }
                }
            }
        }

        // Tạo một số lịch học đặc biệt (quá khứ, bị hủy)
        createSpecialTimetables(classrooms, teacherSchedules);
    }

    /**
     * Kiểm tra giáo viên có rảnh không
     */
    private boolean isTeacherAvailable(Map<Integer, Set<LocalDateTime>> teacherSchedules,
                                       Integer teacherId, LocalDateTime startTime) {
        Set<LocalDateTime> schedules = teacherSchedules.get(teacherId);
        if (schedules == null) {
            return true;
        }

        // Kiểm tra không có lịch nào trong vòng 2 giờ trước và sau
        return schedules.stream().noneMatch(scheduled ->
                Math.abs(scheduled.until(startTime, java.time.temporal.ChronoUnit.HOURS)) < 2
        );
    }

    /**
     * Xác định trạng thái dựa theo thời gian
     */
    private TimetableStatus getStatusForTime(LocalDateTime startTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startTime.isBefore(now.minusHours(2))) {
            return TimetableStatus.COMPLETED;
        } else if (startTime.isBefore(now) && startTime.plusHours(2).isAfter(now)) {
            return TimetableStatus.ONGOING;
        } else if (startTime.isAfter(now)) {
            return TimetableStatus.SCHEDULED;
        }
        return TimetableStatus.SCHEDULED;
    }

    /**
     * Tạo các lịch học đặc biệt
     */
    private void createSpecialTimetables(List<Classroom> classrooms,
                                         Map<Integer, Set<LocalDateTime>> teacherSchedules) {
        LocalDateTime now = LocalDateTime.now();

        for (Classroom classroom : classrooms.subList(0, Math.min(5, classrooms.size()))) {
            // SỬA: teacherId là Integer (không cần longValue)
            Integer teacherId = classroom.getTeacher().getTeacherID();

            // Tạo lịch học bị hủy
            LocalDateTime cancelledTime = now.plusWeeks(3)
                    .with(java.time.DayOfWeek.FRIDAY)
                    .withHour(14).withMinute(0);

            if (isTeacherAvailable(teacherSchedules, teacherId, cancelledTime)) {
                createTimetableIfNotExists(
                        classroom,
                        classroom.getTeacher(),
                        cancelledTime,
                        cancelledTime.plusHours(2),
                        determineTopic(classroom, "Buổi học đặc biệt (ĐÃ HỦY)"),
                        TimetableStatus.CANCELLED
                );
            }

            // Tạo lịch học trong quá khứ
            LocalDateTime pastTime = now.minusWeeks(2)
                    .with(java.time.DayOfWeek.MONDAY)
                    .withHour(9).withMinute(0);

            if (isTeacherAvailable(teacherSchedules, teacherId, pastTime)) {
                createTimetableIfNotExists(
                        classroom,
                        classroom.getTeacher(),
                        pastTime,
                        pastTime.plusHours(2),
                        determineTopic(classroom, "Buổi học tuần trước"),
                        TimetableStatus.COMPLETED
                );
            }
        }
    }

    /**
     * Xáo trộn danh sách để phân bố lịch đều hơn
     */
    private <T> List<T> shuffleList(List<T> list) {
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled, new Random(System.nanoTime()));
        return shuffled;
    }

    /**
     * Tạo danh sách học sinh với đầy đủ thông tin
     */
    private List<Student> createStudentsWithFullInfo(Role studentRole) {
        // Tạo users cho học sinh
        List<User> studentUsers = Arrays.asList(
                createUserIfNotExists("student1", "student123", "Phạm Thị Dung",
                        "student1@educare.com", "0945678901", studentRole),
                createUserIfNotExists("student2", "student123", "Hoàng Văn Em",
                        "student2@educare.com", "0956789012", studentRole),
                createUserIfNotExists("student3", "student123", "Vũ Thị Phương",
                        "student3@educare.com", "0967890123", studentRole),
                createUserIfNotExists("student4", "student123", "Đặng Văn Giang",
                        "student4@educare.com", "0978901234", studentRole),
                createUserIfNotExists("student5", "student123", "Bùi Thị Hà",
                        "student5@educare.com", "0989012345", studentRole),
                createUserIfNotExists("student6", "student123", "Ngô Văn Inh",
                        "student6@educare.com", "0990123456", studentRole),
                createUserIfNotExists("student7", "student123", "Dương Thị Khánh",
                        "student7@educare.com", "0901234567", studentRole),
                createUserIfNotExists("student8", "student123", "Lý Văn Lâm",
                        "student8@educare.com", "0912345670", studentRole),
                createUserIfNotExists("student9", "student123", "Mai Thị Mai",
                        "student9@educare.com", "0923456781", studentRole),
                createUserIfNotExists("student10", "student123", "Trịnh Văn Nam",
                        "student10@educare.com", "0934567892", studentRole),
                createUserIfNotExists("student11", "student123", "Nguyễn Thị Lan",
                        "student11@educare.com", "0945678902", studentRole),
                createUserIfNotExists("student12", "student123", "Trần Văn Hùng",
                        "student12@educare.com", "0956789013", studentRole),
                createUserIfNotExists("student13", "student123", "Lê Thị Hoa",
                        "student13@educare.com", "0967890124", studentRole),
                createUserIfNotExists("student14", "student123", "Phạm Văn Tuấn",
                        "student14@educare.com", "0978901235", studentRole),
                createUserIfNotExists("student15", "student123", "Hoàng Thị Thu",
                        "student15@educare.com", "0989012346", studentRole)
        );

        // Tạo student details với đầy đủ thông tin
        Student student1 = createStudentDetailIfNotExists(
                studentUsers.get(0), "Phạm Thị Dung",
                LocalDate.of(2008, 5, 15), "FEMALE",
                "123 Nguyễn Trãi, Hà Nội",
                "Phạm Văn Hùng", "0912345678", "phamhung@email.com", "Father"
        );

        Student student2 = createStudentDetailIfNotExists(
                studentUsers.get(1), "Hoàng Văn Em",
                LocalDate.of(2008, 8, 22), "MALE",
                "456 Lê Lợi, Hà Nội",
                "Hoàng Thị Lan", "0923456789", "hoanglan@email.com", "Mother"
        );

        Student student3 = createStudentDetailIfNotExists(
                studentUsers.get(2), "Vũ Thị Phương",
                LocalDate.of(2008, 3, 10), "FEMALE",
                "789 Trần Phú, Hà Nội",
                "Vũ Văn Nam", "0934567890", "vunam@email.com", "Father"
        );

        Student student4 = createStudentDetailIfNotExists(
                studentUsers.get(3), "Đặng Văn Giang",
                LocalDate.of(2008, 11, 5), "MALE",
                "321 Hoàng Hoa Thám, Hà Nội",
                "Đặng Thị Hương", "0945678901", "danghuong@email.com", "Mother"
        );

        Student student5 = createStudentDetailIfNotExists(
                studentUsers.get(4), "Bùi Thị Hà",
                LocalDate.of(2008, 7, 18), "FEMALE",
                "654 Tây Sơn, Hà Nội",
                "Bùi Văn Tùng", "0956789012", "buitung@email.com", "Father"
        );

        Student student6 = createStudentDetailIfNotExists(
                studentUsers.get(5), "Ngô Văn Inh",
                LocalDate.of(2008, 1, 25), "MALE",
                "987 Kim Mã, Hà Nội",
                "Ngô Thị Mai", "0967890123", "ngomai@email.com", "Mother"
        );

        Student student7 = createStudentDetailIfNotExists(
                studentUsers.get(6), "Dương Thị Khánh",
                LocalDate.of(2007, 9, 12), "FEMALE",
                "147 Giải Phóng, Hà Nội",
                "Dương Văn Sơn", "0978901234", "duongson@email.com", "Father"
        );

        Student student8 = createStudentDetailIfNotExists(
                studentUsers.get(7), "Lý Văn Lâm",
                LocalDate.of(2007, 4, 30), "MALE",
                "258 Cầu Giấy, Hà Nội",
                "Lý Thị Hồng", "0989012345", "lyhong@email.com", "Mother"
        );

        Student student9 = createStudentDetailIfNotExists(
                studentUsers.get(8), "Mai Thị Mai",
                LocalDate.of(2007, 12, 3), "FEMALE",
                "369 Bạch Mai, Hà Nội",
                "Mai Văn Đức", "0990123456", "maiduc@email.com", "Father"
        );

        Student student10 = createStudentDetailIfNotExists(
                studentUsers.get(9), "Trịnh Văn Nam",
                LocalDate.of(2007, 6, 17), "MALE",
                "741 Xã Đàn, Hà Nội",
                "Trịnh Thị Hoa", "0901234567", "trinhhoa@email.com", "Mother"
        );

        Student student11 = createStudentDetailIfNotExists(
                studentUsers.get(10), "Nguyễn Thị Lan",
                LocalDate.of(2006, 2, 28), "FEMALE",
                "852 Hoàng Quốc Việt, Hà Nội",
                "Nguyễn Văn Bình", "0912345670", "nguyenbinh@email.com", "Father"
        );

        Student student12 = createStudentDetailIfNotExists(
                studentUsers.get(11), "Trần Văn Hùng",
                LocalDate.of(2006, 10, 8), "MALE",
                "963 Trường Chinh, Hà Nội",
                "Trần Thị Thanh", "0923456781", "tranthanh@email.com", "Mother"
        );

        Student student13 = createStudentDetailIfNotExists(
                studentUsers.get(12), "Lê Thị Hoa",
                LocalDate.of(2006, 5, 20), "FEMALE",
                "159 Láng Hạ, Hà Nội",
                "Lê Văn Quân", "0934567892", "lequan@email.com", "Father"
        );

        Student student14 = createStudentDetailIfNotExists(
                studentUsers.get(13), "Phạm Văn Tuấn",
                LocalDate.of(2006, 8, 14), "MALE",
                "753 Nguyễn Chí Thanh, Hà Nội",
                "Phạm Thị Hà", "0945678903", "phamha@email.com", "Mother"
        );

        Student student15 = createStudentDetailIfNotExists(
                studentUsers.get(14), "Hoàng Thị Thu",
                LocalDate.of(2006, 3, 5), "FEMALE",
                "951 Võ Chí Công, Hà Nội",
                "Hoàng Văn Phúc", "0956789014", "hoangphuc@email.com", "Father"
        );

        return Arrays.asList(
                student1, student2, student3, student4, student5,
                student6, student7, student8, student9, student10,
                student11, student12, student13, student14, student15
        );
    }

    /**
     * Phân học sinh vào các lớp
     */
    private void addStudentsToClasses(List<Student> students, List<Classroom> classrooms) {
        // Lớp 10
        addStudentToClassIfNotExists(students.get(0), classrooms.get(0)); // Phạm Thị Dung - 10A1
        addStudentToClassIfNotExists(students.get(1), classrooms.get(0)); // Hoàng Văn Em - 10A1
        addStudentToClassIfNotExists(students.get(2), classrooms.get(1)); // Vũ Thị Phương - 10A2
        addStudentToClassIfNotExists(students.get(3), classrooms.get(1)); // Đặng Văn Giang - 10A2
        addStudentToClassIfNotExists(students.get(4), classrooms.get(2)); // Bùi Thị Hà - 10B1
        addStudentToClassIfNotExists(students.get(5), classrooms.get(3)); // Ngô Văn Inh - 10C1

        // Lớp 11
        addStudentToClassIfNotExists(students.get(6), classrooms.get(6)); // Dương Thị Khánh - 11A1
        addStudentToClassIfNotExists(students.get(7), classrooms.get(6)); // Lý Văn Lâm - 11A1
        addStudentToClassIfNotExists(students.get(8), classrooms.get(7)); // Mai Thị Mai - 11A2
        addStudentToClassIfNotExists(students.get(9), classrooms.get(7)); // Trịnh Văn Nam - 11A2
        addStudentToClassIfNotExists(students.get(10), classrooms.get(8)); // Nguyễn Thị Lan - 11B1
        addStudentToClassIfNotExists(students.get(11), classrooms.get(9)); // Trần Văn Hùng - 11C1

        // Lớp 12
        addStudentToClassIfNotExists(students.get(12), classrooms.get(10)); // Lê Thị Hoa - 12A1
        addStudentToClassIfNotExists(students.get(13), classrooms.get(11)); // Phạm Văn Tuấn - 12B1
        addStudentToClassIfNotExists(students.get(14), classrooms.get(12)); // Hoàng Thị Thu - 12C1

        // Thêm học sinh cho các lớp mới
        if (students.size() > 15 && classrooms.size() > 20) {
            addStudentToClassIfNotExists(students.get(0), classrooms.get(13)); // 10D1
            addStudentToClassIfNotExists(students.get(1), classrooms.get(14)); // 10D2
            addStudentToClassIfNotExists(students.get(2), classrooms.get(16)); // 10E1
            addStudentToClassIfNotExists(students.get(3), classrooms.get(19)); // 10G1
        }
    }

    /**
     * Kiểm tra xem dữ liệu đã được khởi tạo chưa
     */
    private boolean isDataAlreadyInitialized() {
        // Kiểm tra nếu đã có ít nhất 1 role và 1 user thì coi như đã khởi tạo
        long roleCount = roleRepository.count();
        long userCount = userRepository.count();

        if (roleCount > 0 && userCount > 0) {
            log.info("Phát hiện dữ liệu đã tồn tại: {} roles, {} users", roleCount, userCount);
            return true;
        }
        return false;
    }

    private Role createRoleIfNotExists(String roleName, String description) {
        return roleRepository.findByRoleName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setRoleName(roleName);
            role.setDescription(description);
            Role savedRole = roleRepository.save(role);
            log.info("Đã tạo Role: {}", roleName);
            return savedRole;
        });
    }

    private User createUserIfNotExists(String username, String password, String fullName,
                                       String email, String phone, Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .email(email)
                    .phone(phone)
                    .status("ACTIVE")
                    .role(role)
                    .build();
            User savedUser = userRepository.save(user);
            log.info("Đã tạo User: {} / {}", username, password);
            return savedUser;
        });
    }

    private Teacher createTeacherDetailIfNotExists(User user, String fullName, String specialization,
                                                   String gender, boolean isHomeroomTeacher, LocalDate dateOfBirth) {
        return teacherRepository.findByUser(user).orElseGet(() -> {
            Teacher teacher = Teacher.builder()
                    .user(user)
                    .fullName(fullName)
                    .specialization(specialization)
                    .gender(gender)
                    .isHomeroomTeacher(isHomeroomTeacher)
                    .dateOfBirth(dateOfBirth)
                    .build();
            Teacher savedTeacher = teacherRepository.save(teacher);
            log.info("Đã tạo Teacher detail cho: {} ({})", fullName, gender);
            return savedTeacher;
        });
    }

    private Student createStudentDetailIfNotExists(User user, String fullName, LocalDate dateOfBirth,
                                                   String gender, String address, String parentName,
                                                   String parentPhone, String parentEmail, String parentRelationship) {
        return studentRepository.findByUser(user).orElseGet(() -> {
            Student student = Student.builder()
                    .user(user)
                    .fullName(fullName)
                    .dateOfBirth(dateOfBirth)
                    .gender(gender)
                    .address(address)
                    .parentName(parentName)
                    .parentPhone(parentPhone)
                    .parentEmail(parentEmail)
                    .parentRelationship(parentRelationship)
                    .build();
            Student savedStudent = studentRepository.save(student);
            log.info("Đã tạo Student detail cho: {} (SN: {}, {}), PH: {} ({})",
                    fullName, dateOfBirth, gender, parentName, parentRelationship);
            return savedStudent;
        });
    }

    private Subject createSubjectIfNotExists(String subjectName, String description, int gradeLevel, int credits) {
        return subjectRepository.findBySubjectName(subjectName).orElseGet(() -> {
            Subject subject = Subject.builder()
                    .subjectName(subjectName)
                    .description(description)
                    .gradeLevel(gradeLevel)
                    .credits(credits)
                    .isActive(true)
                    .build();
            Subject savedSubject = subjectRepository.save(subject);
            log.info("Đã tạo Subject: {} (Khối {})", subjectName, gradeLevel);
            return savedSubject;
        });
    }

    private Classroom createClassroomIfNotExists(String className, Subject subject, Teacher teacher) {
        // Kiểm tra classroom đã tồn tại chưa dựa trên tên lớp
        return classroomRepository.findByClassName(className).orElseGet(() -> {
            Classroom classroom = Classroom.builder()
                    .className(className)
                    .subject(subject)
                    .teacher(teacher)
                    .status("ACTIVE")
                    .semester(determineSemester(className))
                    .academicYear(determineAcademicYear(className))
                    .maxStudents(40)
                    .startDate(LocalDate.now())
                    .endDate(determineEndDate(className))
                    .build();
            Classroom savedClassroom = classroomRepository.save(classroom);
            log.info("Đã tạo Classroom: {}", className);
            return savedClassroom;
        });
    }

    private Timetable createTimetableIfNotExists(Classroom classroom, Teacher teacher,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 String topic, TimetableStatus status) {
        // Kiểm tra xem đã có timetable cho classroom này trong khoảng thời gian đó chưa
        if (!timetableRepository.existsByClassroomAndStartTime(classroom, startTime)) {
            Timetable timetable = Timetable.builder()
                    .classroom(classroom)
                    .teacher(teacher)
                    .status(status)
                    .startTime(startTime)
                    .endTime(endTime)
                    .topic(topic)
                    .googleMeetLink(generateGoogleMeetLink(classroom, topic))
                    .build();

            Timetable savedTimetable = timetableRepository.save(timetable);
            log.info("Đã tạo Timetable: {} - {} ({})",
                    classroom.getClassName(), topic, startTime.toLocalDate());
            return savedTimetable;
        }
        return null;
    }

    /**
     * Tạo Google Meet link giả định
     */
    private String generateGoogleMeetLink(Classroom classroom, String topic) {
        String baseLink = "https://meet.google.com/";
        String code = classroom.getClassName().replaceAll("[^A-Za-z0-9]", "")
                + "-" + topic.substring(0, Math.min(3, topic.length())).toUpperCase()
                + "-" + System.currentTimeMillis() % 1000;
        return baseLink + code;
    }

    /**
     * Xác định học kỳ dựa trên tên lớp
     */
    private String determineSemester(String className) {
        // Giả sử: Học kỳ 1 cho các lớp có số lẻ, Học kỳ 2 cho các lớp có số chẵn
        if (className.contains("A1") || className.contains("B1") || className.contains("C1") ||
                className.contains("D1") || className.contains("E1") || className.contains("F1") ||
                className.contains("G1") || className.contains("H1") || className.contains("I1")) {
            return "HK1";
        } else {
            return "HK2";
        }
    }

    /**
     * Xác định năm học dựa trên tên lớp
     */
    private String determineAcademicYear(String className) {
        if (className.startsWith("10")) {
            return "2024-2025";
        } else if (className.startsWith("11")) {
            return "2023-2024";
        } else if (className.startsWith("12")) {
            return "2022-2023";
        }
        return "2024-2025";
    }

    /**
     * Xác định ngày kết thúc dựa trên tên lớp
     */
    private LocalDate determineEndDate(String className) {
        LocalDate now = LocalDate.now();
        if (className.startsWith("10")) {
            return now.plusMonths(9).withDayOfMonth(30); // Kết thúc tháng 5 năm sau
        } else if (className.startsWith("11")) {
            return now.plusMonths(9).withDayOfMonth(30);
        } else if (className.startsWith("12")) {
            return now.plusMonths(6).withDayOfMonth(30); // Kết thúc sớm hơn cho lớp 12
        }
        return now.plusMonths(9).withDayOfMonth(30);
    }

    /**
     * Xác định chủ đề bài học dựa trên môn học
     */
    private String determineTopic(Classroom classroom, String session) {
        String subjectName = classroom.getSubject().getSubjectName();
        String className = classroom.getClassName();

        if (subjectName.contains("Toán")) {
            return "Toán học - " + className + " - " + session + ": Hàm số và đồ thị";
        } else if (subjectName.contains("Ngữ văn")) {
            return "Ngữ văn - " + className + " - " + session + ": Phân tích tác phẩm văn học";
        } else if (subjectName.contains("Tiếng Anh")) {
            return "Tiếng Anh - " + className + " - " + session + ": Grammar and Vocabulary";
        } else if (subjectName.contains("Vật lý")) {
            return "Vật lý - " + className + " - " + session + ": Điện học và từ trường";
        } else if (subjectName.contains("Hóa học")) {
            return "Hóa học - " + className + " - " + session + ": Phản ứng oxi hóa khử";
        } else if (subjectName.contains("Sinh học")) {
            return "Sinh học - " + className + " - " + session + ": Cấu trúc tế bào";
        } else if (subjectName.contains("Lịch sử")) {
            return "Lịch sử - " + className + " - " + session + ": Các cuộc kháng chiến chống ngoại xâm";
        } else if (subjectName.contains("Địa lý")) {
            return "Địa lý - " + className + " - " + session + ": Địa hình và khí hậu Việt Nam";
        } else if (subjectName.contains("Tin học")) {
            return "Tin học - " + className + " - " + session + ": Lập trình cơ bản";
        }
        return subjectName + " - " + className + " - " + session;
    }

    private void addStudentToClassIfNotExists(Student student, Classroom classroom) {
        if (!classMemberRepository.existsByStudentAndClassroom(student, classroom)) {
            ClassMemberID classMemberID = new ClassMemberID();
            classMemberID.setClassID(classroom.getClassID());
            classMemberID.setStudentID(student.getStudentID());

            ClassMember classMember = ClassMember.builder()
                    .classMemberID(classMemberID)
                    .classroom(classroom)
                    .student(student)
                    .joinedAt(LocalDateTime.now())
                    .status("ACTIVE")
                    .build();

            classMemberRepository.save(classMember);
            log.info("Đã thêm {} vào lớp {}", student.getFullName(), classroom.getClassName());
        } else {
            log.debug("Học sinh {} đã có trong lớp {}", student.getFullName(), classroom.getClassName());
        }
    }
}