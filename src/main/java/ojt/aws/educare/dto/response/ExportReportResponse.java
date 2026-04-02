package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExportReportResponse {
    String fileName;
    String format;
    List<String> dataTypes;
    String timeRange;
    /** PENDING = generation queued; READY = file available for download */
    String status;
    String message;
    /** Null until file generation is implemented */
    String downloadUrl;
}
