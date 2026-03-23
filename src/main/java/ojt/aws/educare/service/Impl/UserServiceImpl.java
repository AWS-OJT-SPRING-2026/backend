package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ojt.aws.educare.dto.request.*;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.PageResponse;
import ojt.aws.educare.dto.response.StudentResponse;
import ojt.aws.educare.dto.response.TeacherResponse;
import ojt.aws.educare.dto.response.UserResponse;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;

import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.UserMapper;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.EmailService;
import ojt.aws.educare.service.S3UploadService;
import ojt.aws.educare.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    TeacherRepository teacherRepository;
    StudentRepository studentRepository;
    PasswordResetTokenRepository passwordResetTokenRepository;

    EmailService emailService;
    S3UploadService s3UploadService;

    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<UserResponse> registerUser(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRole(role);

        user.setStatus("ACTIVE");
        User savedUser = userRepository.save(user);

        UserResponse userResponse = userMapper.toUserResponse(savedUser);
        resolveAvatar(userResponse);
        return ApiResponse.success("Đăng ký tài khoản thành công", userResponse);
    }

    @Override
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            throw new AppException(ErrorCode.USER_LIST_EMPTY);
        }

        List<UserResponse> userResponses = userMapper.toUserResponseList(users);
        resolveAvatar(userResponses);
        return ApiResponse.success("Lấy danh sách người dùng thành công", userResponses);
    }

    @Override
    public ApiResponse<Object> getUserByID(Integer userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String roleName = user.getRole().getRoleName().toUpperCase();

        if ("STUDENT".equals(roleName) && user.getStudent() != null) {
            StudentResponse studentResponse = userMapper.toStudentResponse(user.getStudent());
            resolveAvatar(studentResponse);
            return ApiResponse.success("Lấy thông tin Học sinh thành công",
                    studentResponse);

        } else if ("TEACHER".equals(roleName) && user.getTeacher() != null) {
            TeacherResponse teacherResponse = userMapper.toTeacherResponse(user.getTeacher());
            resolveAvatar(teacherResponse);
            return ApiResponse.success("Lấy thông tin Giáo viên thành công",
                    teacherResponse);
        }

        UserResponse userResponse = userMapper.toUserResponse(user);
        resolveAvatar(userResponse);
        return ApiResponse.success("Lấy thông tin Người dùng thành công", userResponse);
    }

    @Override
    public ApiResponse<UserResponse> createTeacher(TeacherCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        Role role = roleRepository.findByRoleName("TEACHER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("ACTIVE");
        User savedUser = userRepository.save(user);

        Teacher teacher = userMapper.toTeacher(request);
        teacher.setUser(savedUser);
        teacherRepository.save(teacher);

        UserResponse userResponse = userMapper.toUserResponse(savedUser);
        resolveAvatar(userResponse);
        return ApiResponse.success("Tạo tài khoản Giáo viên thành công", userResponse);
    }

    @Override
    @Transactional
    public ApiResponse<StudentResponse> createStudent(StudentCreateRequest request, MultipartFile avatar) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        Role role = roleRepository.findByRoleName("STUDENT")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("ACTIVE");

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = s3UploadService.uploadFile(avatar);
            user.setAvatarUrl(avatarUrl);
        }

        User savedUser = userRepository.save(user);

        Student student = userMapper.toStudent(request);
        student.setUser(savedUser);
        Student savedStudent = studentRepository.save(student);

        StudentResponse studentResponse = userMapper.toStudentResponse(savedStudent);
        resolveAvatar(studentResponse);
        return ApiResponse.success("Tạo tài khoản Học sinh thành công", studentResponse);
    }

    @Override
    public ApiResponse<PageResponse<UserResponse>> getUsersWithPaginationAndFilter(int page, int size, String roleName,
            String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size);

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : "";
        String searchRole = (roleName != null && !roleName.trim().isEmpty()) ? roleName.toUpperCase() : "";

        Page<User> userPage = userRepository.searchUsersAndFilterByRole(searchKeyword, searchRole, pageable);

        List<UserResponse> userResponses = userMapper.toUserResponseList(userPage.getContent());
        resolveAvatar(userResponses);

        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .data(userResponses)
                .build();

        return ApiResponse.success("Lấy danh sách người dùng thành công", pageResponse);
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateTeacher(Integer userId, TeacherUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        Teacher teacher = user.getTeacher();
        if (teacher == null)
            throw new AppException(ErrorCode.USER_NOT_FOUND);

        userMapper.updateUserFromTeacherRequest(user, request);
        userMapper.updateTeacherFromRequest(teacher, request);

        userRepository.save(user);
        UserResponse userResponse = userMapper.toUserResponse(user);
        resolveAvatar(userResponse);
        return ApiResponse.success("Cập nhật thông tin Giáo viên thành công", userResponse);
    }

    @Override
    @Transactional
    public ApiResponse<StudentResponse> updateStudent(Integer userId, StudentUpdateRequest request,
            MultipartFile newAvatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        Student student = user.getStudent();
        if (student == null)
            throw new AppException(ErrorCode.USER_NOT_FOUND);

        userMapper.updateUserFromStudentRequest(user, request);
        userMapper.updateStudentFromRequest(student, request);

        if (newAvatar != null && !newAvatar.isEmpty()) {
            if (user.getAvatarUrl() != null) {
                s3UploadService.deleteFileFromUrl(user.getAvatarUrl());
            }
            String newAvatarUrl = s3UploadService.uploadFile(newAvatar);
            user.setAvatarUrl(newAvatarUrl);
        }

        userRepository.save(user);
        StudentResponse studentResponse = userMapper.toStudentResponse(student);
        resolveAvatar(studentResponse);
        return ApiResponse.success("Cập nhật thông tin Học sinh thành công", studentResponse);
    }

    @Override
    @Transactional
    public ApiResponse<Void> toggleUserStatus(Integer userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String currentLoggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (user.getUsername().equals(currentLoggedInUsername)) {
            throw new AppException(ErrorCode.DELETE_SELF_INVALID);
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new AppException(ErrorCode.DELETE_OTHER_ADMIN_INVALID);
        }

        String message;
        if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("LOCKED");
            message = "Đã khóa tài khoản thành công";
        } else {
            user.setStatus("ACTIVE");
            message = "Đã mở khóa tài khoản thành công";
        }

        userRepository.save(user);

        return ApiResponse.success(message, null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Integer userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if ("ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new AppException(ErrorCode.DELETE_ADMIN_INVALID);
        }

        if (user.getAvatarUrl() != null) {
            s3UploadService.deleteFileFromUrl(user.getAvatarUrl());
        }

        userRepository.delete(user);

        return ApiResponse.success("Đã xóa vĩnh viễn tài khoản khỏi hệ thống", null);
    }

    @Override
    public ApiResponse<Void> updateLastActive() {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);

        return ApiResponse.success("Đã cập nhật trạng thái hoạt động", null);
    }

    @Override
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String otpCode = generateOtp();

        passwordResetTokenRepository.markAllTokensAsUsed(user.getUserID());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .otpCode(otpCode)
                .expiryDate(LocalDateTime.now().plusMinutes(1).plusSeconds(30))
                .used(false)
                .user(user)
                .createdDate(LocalDateTime.now())
                .build();

        passwordResetTokenRepository.save(resetToken);
        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());
        return ApiResponse.success("Mã OTP đã được gửi đến email của bạn", null);
    }

    @Override
    public ApiResponse<String> verifyOtp(VerifyOtpRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByOtpCodeAndUser_Email(request.getOtpCode(), request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP_OR_EMAIL));

        if (resetToken.getUsed()) {
            throw new AppException(ErrorCode.OTP_ALREADY_USED);
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        return ApiResponse.success("Mã OTP hợp lệ");
    }

    @Override
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORDS_NOT_MATCH);
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByOtpCodeAndUser_Email(request.getOtpCode(), request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP_OR_EMAIL));

        if (resetToken.getUsed()) {
            throw new AppException(ErrorCode.OTP_ALREADY_USED);
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true); // danh dau da su dung
        passwordResetTokenRepository.save(resetToken);

        return ApiResponse.success("Đặt lại mật khẩu thành công");
    }

    @Override
    @Transactional
    public ApiResponse<String> uploadAvatar(MultipartFile file) {
        // 1. Lấy thông tin người dùng đang đăng nhập
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Upload ảnh lên S3
        String avatarUrl = s3UploadService.uploadFile(file);

        // 3. Cập nhật vào Database
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return ApiResponse.success("Cập nhật ảnh đại diện thành công", s3UploadService.resolveFileUrl(avatarUrl));
    }

    private void resolveAvatar(List<UserResponse> userResponses) {
        if (userResponses == null || userResponses.isEmpty()) {
            return;
        }
        userResponses.forEach(this::resolveAvatar);
    }

    private void resolveAvatar(UserResponse userResponse) {
        if (userResponse == null) {
            return;
        }
        userResponse.setAvatarUrl(s3UploadService.resolveFileUrl(userResponse.getAvatarUrl()));
    }

    private void resolveAvatar(StudentResponse studentResponse) {
        if (studentResponse == null) {
            return;
        }
        studentResponse.setAvatarUrl(s3UploadService.resolveFileUrl(studentResponse.getAvatarUrl()));
    }

    private void resolveAvatar(TeacherResponse teacherResponse) {
        if (teacherResponse == null) {
            return;
        }
        teacherResponse.setAvatarUrl(s3UploadService.resolveFileUrl(teacherResponse.getAvatarUrl()));
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
