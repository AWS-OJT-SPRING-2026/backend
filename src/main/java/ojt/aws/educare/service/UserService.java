package ojt.aws.educare.service;


import ojt.aws.educare.dto.request.*;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.PageResponse;
import ojt.aws.educare.dto.response.StudentResponse;
import ojt.aws.educare.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    ApiResponse<UserResponse> registerUser(UserRegisterRequest request);
    ApiResponse<List<UserResponse>> getAllUsers();
    ApiResponse<Object> getUserByID(Integer userID);
    ApiResponse<UserResponse> createTeacher(TeacherCreateRequest request);
    ApiResponse<StudentResponse> createStudent(StudentCreateRequest request);
    ApiResponse<PageResponse<UserResponse>> getUsersWithPaginationAndFilter(int page, int size, String roleName, String keyword);
    ApiResponse<UserResponse> updateTeacher(Integer userId, TeacherUpdateRequest request);
    ApiResponse<StudentResponse> updateStudent(Integer userId, StudentUpdateRequest request);
    ApiResponse<Void> toggleUserStatus(Integer userID);
    ApiResponse<Void> deleteUser(Integer userID);

    ApiResponse<Void> updateLastActive();

    ApiResponse<String> forgotPassword(ForgotPasswordRequest request);
    ApiResponse<String> verifyOtp(VerifyOtpRequest request);
    ApiResponse<String> resetPassword(ResetPasswordRequest request);

}
