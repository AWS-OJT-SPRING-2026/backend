package ojt.aws.educare.service;

import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.StudentResponse;

import java.util.List;

public interface StudentService {
    ApiResponse<List<StudentResponse>> getAllStudents();
    ApiResponse<StudentResponse> getStudentByID(Integer studentID);
}
