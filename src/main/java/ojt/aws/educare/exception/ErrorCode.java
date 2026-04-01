package ojt.aws.educare.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(401, "Bạn chưa được xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(403, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),

    //USER ERRORS
    USER_EXISTED(1002, "Username đã được sử dụng, hãy sử dụng username khác!", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1003, "Email đã được sử dụng, hãy sử dụng email khác", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1004, "Tên người dùng phải có ít nhất 3 ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1005, "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ cái và số", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1006, "Tên đăng nhập hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1007, "Tên đăng nhập hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    USERNAME_REQUIRED(1008, "Username không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1009, "Password không được để trống", HttpStatus.BAD_REQUEST),
    FULLNAME_REQUIRED(1010, "Họ tên không được để trống", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1011, "Email không được để trống", HttpStatus.BAD_REQUEST),
    PHONE_REQUIRED(1012, "Số điện thoại không được để trống", HttpStatus.BAD_REQUEST),
    ROLE_ID_REQUIRED(1013, "Role ID không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_PHONE(1014, "Số điện thoại phải có 10-11 chữ số", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(1015, "Email không đúng định dạng (ví dụ: 112@gmail.com)", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1016, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_FULLNAME(1017, "Họ tên không được vượt quá 255 ký tự", HttpStatus.BAD_REQUEST),
    USER_LIST_EMPTY(1018, "Không có tài khoản nào trong hệ thống!!!", HttpStatus.NOT_FOUND),

    // ROLE ERRORS
    ROLE_NOT_FOUND(1019, "Role không tồn tại", HttpStatus.NOT_FOUND),
    DEFAULT_ROLE_NOT_FOUND(1020, "Không tìm thấy role mặc định", HttpStatus.NOT_FOUND),
    USER_INACTIVE(1021, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ giáo viên hoặc trung tâm để được mở khóa.", HttpStatus.FORBIDDEN),

    DELETE_ADMIN_INVALID(1022, "Không thể xóa tài khoản Quản trị viên hệ thống", HttpStatus.BAD_REQUEST),
    DELETE_SELF_INVALID(1023, "Bạn không thể tự khóa tài khoản của chính mình!", HttpStatus.BAD_REQUEST),
    DELETE_OTHER_ADMIN_INVALID(1024, "Bạn không thể xóa tài khoản Quản trị viên khác!", HttpStatus.BAD_REQUEST),
    CLASSNAME_REQUIRED(1025, "Tên lớp học không được để trống", HttpStatus.BAD_REQUEST),
    SUBJECT_ID_REQUIRED(1026, "Vui lòng chọn môn học", HttpStatus.BAD_REQUEST),
    SEMESTER_REQUIRED(1027, "Học kỳ không được để trống", HttpStatus.BAD_REQUEST),
    ACADEMIC_YEAR_REQUIRED(1028, "Năm học không được để trống", HttpStatus.BAD_REQUEST),
    MAX_STUDENTS_REQUIRED(1029, "Sĩ số tối đa không được để trống", HttpStatus.BAD_REQUEST),

    SUBJECT_NOT_FOUND(1030, "Không tìm thấy môn học", HttpStatus.NOT_FOUND),
    CLASSROOM_NOT_FOUND(1031, "Không tìm thấy lớp học", HttpStatus.NOT_FOUND),
    TEACHER_NOT_FOUND(1032, "Không tìm thấy giáo viên", HttpStatus.NOT_FOUND),
    STUDENT_NOT_FOUND(1033, "Không tìm thấy học sinh", HttpStatus.NOT_FOUND),
    CLASSROOM_FULL(1034, "Lớp học đã đạt sĩ số tối đa", HttpStatus.BAD_REQUEST),
    TEACHER_ALREADY_ASSIGNED_THIS_CLASS(1035, "Giáo viên này đang phụ trách lớp học này rồi!", HttpStatus.BAD_REQUEST),
    STUDENT_NOT_IN_CLASS(1036, "Học sinh này không thuộc lớp học này", HttpStatus.BAD_REQUEST),

    TIMETABLE_NOT_FOUND(1037, "Không tìm thấy buổi học", HttpStatus.NOT_FOUND),
    STUDENT_ID_REQUIRED(1038, "Mã học sinh không được để trống", HttpStatus.BAD_REQUEST),
    CLASS_ID_REQUIRED(1039, "Mã lớp học không được để trống", HttpStatus.BAD_REQUEST),
    STATUS_REQUIRED(1040, "Trạng thái không được để trống", HttpStatus.BAD_REQUEST),
    START_TIME_REQUIRED(1041, "Thời gian bắt đầu không được để trống", HttpStatus.BAD_REQUEST),
    END_TIME_REQUIRED(1042, "Thời gian kết thúc không được để trống", HttpStatus.BAD_REQUEST),
    START_DATE_REQUIRED(1043, "Ngày bắt đầu không được để trống", HttpStatus.BAD_REQUEST),
    END_DATE_REQUIRED(1044, "Ngày kết thúc không được để trống", HttpStatus.BAD_REQUEST),
    DAYS_OF_WEEK_REQUIRED(1045, "Vui lòng chọn ít nhất một ngày trong tuần để lặp lại", HttpStatus.BAD_REQUEST),
    TIMETABLE_TIME_INVALID(1046, "Giờ kết thúc phải sau giờ bắt đầu", HttpStatus.BAD_REQUEST),
    TIMETABLE_DATE_INVALID(1047, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu", HttpStatus.BAD_REQUEST),
    CLASSROOM_NO_SUBJECT(1048, "Lớp học này chưa có môn học nào được gán", HttpStatus.BAD_REQUEST),
    USER_IS_NOT_TEACHER(1049, "Tài khoản này không phải là Giáo viên!", HttpStatus.BAD_REQUEST ),
    NO_PERMISSION_UPDATE_TIMETABLE_LINK(1050, "Bạn không có quyền cập nhật link cho buổi học này!", HttpStatus.BAD_REQUEST),

    //reset password
    OTP_REQUIRED(1058, "Mã OTP không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1059, "Mã OTP phải có 6 chữ số", HttpStatus.BAD_REQUEST),
    OTP_NOT_FOUND(1060, "Mã OTP không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1061, "Mã OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_ALREADY_USED(1062, "Mã OTP đã được sử dụng", HttpStatus.BAD_REQUEST),
    INVALID_OTP_OR_EMAIL(1063, "Mã OTP hoặc email không chính xác", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND(1064, "Token không hợp lệ", HttpStatus.BAD_REQUEST),
    PASSWORDS_NOT_MATCH(1065, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_REQUIRED(1066, "Vui lòng xác nhận mật khẩu", HttpStatus.BAD_REQUEST),

    CHAT_SESSION_ID_REQUIRED(1067, "Session chat không được để trống", HttpStatus.BAD_REQUEST),
    CHAT_SESSION_PROCESSING_ERROR(1068, "Không thể xử lý dữ liệu lịch sử chat", HttpStatus.INTERNAL_SERVER_ERROR),
    CHAT_SESSION_TITLE_REQUIRED(1069, "Tiêu đề phiên chat không được để trống", HttpStatus.BAD_REQUEST),

    PASSWORD_INCORRECT(1070, "Mật khẩu hiện tại không chính xác", HttpStatus.BAD_REQUEST),
    PASSWORD_CHANGE_LIMIT(1071, "Bạn chỉ được phép đổi mật khẩu 1 lần trong vòng 7 ngày", HttpStatus.BAD_REQUEST),

    DOCUMENT_TYPE_INVALID(1072, "Loại tài liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    DOCUMENT_NOT_FOUND(1073, "Không tìm thấy tài liệu", HttpStatus.NOT_FOUND),
    NO_PERMISSION_DISTRIBUTE_DOCUMENT(1074, "Bạn không có quyền phân phối tài liệu cho lớp này", HttpStatus.FORBIDDEN),
    STUDENT_MATERIAL_ACCESS_DENIED(1075, "Bạn không có quyền xem tài liệu này", HttpStatus.FORBIDDEN),

    // ASSIGNMENT ERRORS
    ASSIGNMENT_NOT_FOUND(1400, "Không tìm thấy đề kiểm tra", HttpStatus.NOT_FOUND),
    QUESTION_NOT_FOUND(1401, "Không tìm thấy câu hỏi", HttpStatus.NOT_FOUND),
    FORBIDDEN(1403, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),
    ASSIGNMENT_ALREADY_PUBLISHED(1404, "Đề kiểm tra đã phát hành, không thể chỉnh sửa", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_HAS_NO_QUESTIONS(1405, "Đề kiểm tra chưa có câu hỏi", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_DEADLINE_PASSED(1406, "Deadline đã qua", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ACTIVE_ASSIGNMENT(1407, "Không thể xóa đề đang hoạt động", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_NOT_ACTIVE(1408, "Đề kiểm tra chưa được phát hành hoặc đã đóng", HttpStatus.BAD_REQUEST),
    ALREADY_SUBMITTED(1409, "Bạn đã nộp bài cho đề kiểm tra này", HttpStatus.BAD_REQUEST),
    QUESTION_BANK_NOT_FOUND(1410, "Không tìm thấy ngân hàng câu hỏi", HttpStatus.NOT_FOUND),
    ASSIGNMENT_TYPE_INVALID(1411, "Loại bài không hợp lệ. Chỉ chấp nhận TEST hoặc ASSIGNMENT", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_FORMAT_INVALID(1412, "Hình thức không hợp lệ. Chỉ chấp nhận MULTIPLE_CHOICE hoặc ESSAY", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_TEST_TIME_REQUIRED(1413, "Bài kiểm tra yêu cầu startTime và endTime", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_TEST_TIME_INVALID(1414, "endTime phải sau startTime", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_DEADLINE_REQUIRED(1415, "Bài tập yêu cầu deadline", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_DURATION_REQUIRED(1416, "Bài kiểm tra/bài tập yêu cầu thời gian làm bài", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_DURATION_INVALID(1417, "Thời gian làm bài phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_DEADLINE_INVALID(1418, "Deadline phải ở tương lai", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_TEST_NOT_STARTED(1419, "Bài kiểm tra chưa đến thời gian bắt đầu", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_TEST_ENDED(1420, "Bài kiểm tra đã kết thúc", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_ATTEMPT_NOT_STARTED(1421, "Bạn chưa bắt đầu làm bài", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_ATTEMPT_EXPIRED(1422, "Đã hết thời gian làm bài", HttpStatus.BAD_REQUEST);

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
