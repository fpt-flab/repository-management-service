package vn.edu.fpt.repository.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.repository.constant.ResponseStatusEnum;
import vn.edu.fpt.repository.dto.common.CreateFileRequest;
import vn.edu.fpt.repository.entity._File;
import vn.edu.fpt.repository.exception.BusinessException;
import vn.edu.fpt.repository.service.S3BucketStorageService;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * @author : Hoang Lam
 * @product : Charity Management System
 * @project : Charity System
 * @created : 26/10/2022 - 02:03
 * @contact : 0834481768 - hoang.harley.work@gmail.com
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class S3BucketStorageServiceImpl implements S3BucketStorageService {

    private final AmazonS3 amazonS3;

    @Value("${application.bucket}")
    private String bucketName;

    @Value("${application.cloudfront.repository}")
    private String cloudfront;
    @Override
    public String uploadFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(getContentType(fileName));
            File convFile = new File(fileName);

            try (OutputStream os = Files.newOutputStream(Path.of(convFile.getPath()))) {
                os.write(file.getBytes());
            }
            String fileKey = UUID.randomUUID().toString();
            PutObjectRequest request = new PutObjectRequest(bucketName, fileKey, convFile);
            request.setCannedAcl(CannedAccessControlList.PublicRead);
            request.setMetadata(metadata);
            amazonS3.putObject(request);
            URL url = amazonS3.getUrl(bucketName, fileKey);
            return url.toString();
        } catch (Exception ex) {
            throw new BusinessException(ResponseStatusEnum.INTERNAL_SERVER_ERROR, "Can't put object file to AWS S3: " + ex.getMessage());
        } finally {
            try {
                Files.delete(Paths.get(fileName));
            } catch (Exception ex) {
                log.error("Can't delete converted file: " + ex.getMessage());
            }
        }
    }

    @Override
    public void uploadFile(CreateFileRequest request, String fileKey) {
        String base64 = request.getBase64().split(",")[1];
        byte[] decodedFile = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
        InputStream is = new ByteArrayInputStream(decodedFile);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(request.getMimeType());
        metadata.setContentLength(request.getSize());

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileKey, is, metadata);
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        try {
            amazonS3.putObject(putObjectRequest);
        }catch (Exception ex){
            throw new BusinessException("Can't push object to s3 bucket: "+ ex.getMessage());
        }
    }

    private String getContentType(String fileName) {
        String fileType = fileName.split("\\.")[1];
        switch ((fileType)) {
            case "png":
                return MediaType.IMAGE_PNG_VALUE;
            case "jpeg":
                return MediaType.IMAGE_JPEG_VALUE;
            case "gif":
                return MediaType.IMAGE_GIF_VALUE;
            case "pdf":
                return MediaType.APPLICATION_PDF_VALUE;
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    @Override
    public void downloadFile(_File file, HttpServletResponse response) {
        response.setContentType(file.getMimeType());
        response.setContentLengthLong(file.getLength());
        try {
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, file.getFileKey()));
            try (InputStream is = s3Object.getObjectContent()) {
                int len;
                byte[] buffer = new byte[4096];
                while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                    response.getOutputStream().write(buffer, 0, len);
                }
                response.flushBuffer();
                log.info("Download file success");
            }
        } catch (Exception ex) {
            throw new BusinessException(ResponseStatusEnum.INTERNAL_SERVER_ERROR, "Can't download file from AWS S3: " + ex.getMessage());
        }
    }

    @Override
    public File downloadFile(String fileKey) {
        try {
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, fileKey));
            try (InputStream is = s3Object.getObjectContent()) {
                File file = new File(fileKey);
                Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return file;
            }
        } catch (Exception ex) {
            throw new BusinessException(ResponseStatusEnum.INTERNAL_SERVER_ERROR, "Can't download file from AWS S3: " + ex.getMessage());
        }
    }

    @Override
    public String sharingUsingPresignedURL(String fileKey) {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 24 * 7 * 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        // Generate the presigned URL.
        System.out.println("Generating pre-signed URL.");
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();

    }

    @Override
    public String getPublicURL(String fileKey) {
        return cloudfront+ fileKey;
    }
}
