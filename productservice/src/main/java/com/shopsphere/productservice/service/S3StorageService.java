package com.shopsphere.productservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3StorageService {

    String uploadProductImage(Long productId, MultipartFile file);

    void deleteFile(String objectKey);
}