package ojt.aws.educare.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3UploadService {
    String uploadFile(MultipartFile file);
    String resolveFileUrl(String fileReference);
    void deleteFileFromUrl(String fileUrl);
}
