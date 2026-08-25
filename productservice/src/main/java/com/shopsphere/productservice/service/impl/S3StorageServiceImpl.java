package com.shopsphere.productservice.service.impl;

import com.shopsphere.productservice.exception.InvalidImageException;
import com.shopsphere.productservice.exception.S3UploadException;
import com.shopsphere.productservice.service.S3StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class S3StorageServiceImpl implements S3StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageServiceImpl(
            S3Client s3Client,
            @Value("${aws.s3.bucket-name}") String bucketName) {

        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadProductImage(
            Long productId,
            MultipartFile file) {

        validateImage(file);

        String objectKey = buildObjectKey(productId, file);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            return objectKey;

        } catch (IOException exception) {
            throw new S3UploadException(
                    "Unable to read the uploaded image",
                    exception
            );

        } catch (S3Exception exception) {
            throw new S3UploadException(
                    "Unable to upload image to AWS S3: "
                            + exception.awsErrorDetails().errorMessage(),
                    exception
            );
        }
    }

    @Override
    public void deleteFile(String objectKey) {

        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(request);

        } catch (S3Exception exception) {
            throw new S3UploadException(
                    "Unable to delete image from AWS S3: "
                            + exception.awsErrorDetails().errorMessage(),
                    exception
            );
        }
    }

    private void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidImageException(
                    "Please select an image to upload"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new InvalidImageException(
                    "Only JPEG, PNG, and WEBP images are allowed"
            );
        }

        long maximumSize = 10L * 1024 * 1024;

        if (file.getSize() > maximumSize) {
            throw new InvalidImageException(
                    "Image size cannot exceed 10 MB"
            );
        }
    }

    private String buildObjectKey(
            Long productId,
            MultipartFile file) {

        String originalName = file.getOriginalFilename();

        String safeFileName =
                originalName == null
                        ? "product-image"
                        : originalName.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );

        return "products/"
                + productId
                + "/"
                + UUID.randomUUID()
                + "-"
                + safeFileName;
    }
}