package ojt.aws.educare.service.Impl;

import lombok.RequiredArgsConstructor;
import ojt.aws.educare.service.S3UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadServiceImpl implements S3UploadService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiration-minutes:60}")
    private long presignedUrlExpirationMinutes;

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File không được để trống");
        }

        try {
            // Tạo tên file ngẫu nhiên để không bị trùng lặp
            String originalName = file.getOriginalFilename();
            String extension = ".jpg";
            if (StringUtils.hasText(originalName) && originalName.lastIndexOf('.') >= 0) {
                extension = originalName.substring(originalName.lastIndexOf('.'));
            }
            String fileName = "avatars/" + UUID.randomUUID() + extension;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Lưu object key vào DB để không phụ thuộc bucket public/private
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload file lên S3: " + e.getMessage());
        }
    }

    @Override
    public String resolveFileUrl(String fileReference) {
        if (!StringUtils.hasText(fileReference)) {
            return null;
        }

        String fileKey = extractFileKey(fileReference);
        if (!StringUtils.hasText(fileKey)) {
            return fileReference;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(Math.max(1, presignedUrlExpirationMinutes)))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void deleteFileFromUrl(String fileUrl) {
        if (StringUtils.hasText(fileUrl)) {
            try {
                String fileKey = extractFileKey(fileUrl);
                if (!StringUtils.hasText(fileKey)) {
                    return;
                }

                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileKey)
                        .build();

                s3Client.deleteObject(deleteObjectRequest);
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa file trên S3: " + e.getMessage());
            }
        }
    }

    private String extractFileKey(String fileReference) {
        if (!StringUtils.hasText(fileReference)) {
            return null;
        }

        String value = fileReference.trim();
        if (!value.contains("amazonaws.com")) {
            return value;
        }

        int keyStartIndex = value.indexOf(".amazonaws.com/");
        if (keyStartIndex < 0) {
            return null;
        }

        String keyWithQuery = value.substring(keyStartIndex + ".amazonaws.com/".length());
        int queryIndex = keyWithQuery.indexOf('?');
        return queryIndex >= 0 ? keyWithQuery.substring(0, queryIndex) : keyWithQuery;
    }
}
