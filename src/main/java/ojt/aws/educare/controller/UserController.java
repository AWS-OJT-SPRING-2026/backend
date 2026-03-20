package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.*;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.PageResponse;
import ojt.aws.educare.dto.response.StudentResponse;
import ojt.aws.educare.dto.response.UserResponse;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getUserByID(@PathVariable Integer userID) {
        return userService.getUserByID(userID);
    }

    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createTeacher(@Valid @RequestBody TeacherCreateRequest request) {
        return userService.createTeacher(request);
    }

    @PostMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        return userService.createStudent(request);
    }

    @GetMapping("/paging")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String keyword
    ) {
        return userService.getUsersWithPaginationAndFilter(page, size, roleName, keyword);
    }


    @PutMapping("/teachers/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateTeacher(
            @PathVariable Integer userId,
            @Valid @RequestBody TeacherUpdateRequest request) {
        return userService.updateTeacher(userId, request);
    }

    @PutMapping("/students/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StudentResponse> updateStudent(
            @PathVariable Integer userId,
            @Valid @RequestBody StudentUpdateRequest request) {
        return userService.updateStudent(userId, request);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Integer userId) {
        return userService.toggleUserStatus(userId);
    }

    @DeleteMapping("/{userId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Integer userId) {
        return userService.deleteUser(userId);
    }

    @PutMapping("/ping")
    public ApiResponse<Void> updateLastActive() {
        return userService.updateLastActive();
    }

    //reset password
    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return userService.forgotPassword(request);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return userService.verifyOtp(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request);
    }
}