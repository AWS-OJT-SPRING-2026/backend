package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExportReportRequest {

    @NotBlank(message = "Định dạng xuất không được để trống")
    String format; // EXCEL | PDF | CSV

    @NotEmpty(message = "Vui lòng chọn ít nhất một loại dữ liệu để xuất")
    List<@NotBlank(message = "Tên loại dữ liệu không được để trống") String> dataTypes; // GRADEBOOK | ATTENDANCE | PROGRESS

    @NotBlank(message = "Khoảng thời gian không được để trống")
    String timeRange; // THIS_WEEK | THIS_MONTH | CURRENT_SEMESTER
}
