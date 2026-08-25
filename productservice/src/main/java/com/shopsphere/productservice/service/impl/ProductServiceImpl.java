package com.shopsphere.productservice.service.impl;

import com.shopsphere.productservice.dto.ProductRequest;
import com.shopsphere.productservice.dto.ProductResponse;
import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.exception.ProductNotFoundException;
import com.shopsphere.productservice.repository.ProductRepository;
import com.shopsphere.productservice.service.ProductService;
import com.shopsphere.productservice.service.S3StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final S3StorageService s3StorageService;
    private final String bucketName;
    private final String awsRegion;

    public ProductServiceImpl(
            ProductRepository productRepository,
            S3StorageService s3StorageService,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.region}") String awsRegion) {

        this.productRepository = productRepository;
        this.s3StorageService = s3StorageService;
        this.bucketName = bucketName;
        this.awsRegion = awsRegion;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Override
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {

        return mapToResponse(findProductById(id));
    }

    @Override
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request) {

        Product product = findProductById(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse uploadProductImage(
            Long id,
            MultipartFile file) {

        Product product = findProductById(id);

        if (product.getImageKey() != null) {
            s3StorageService.deleteFile(product.getImageKey());
        }

        String imageKey =
                s3StorageService.uploadProductImage(id, file);

        String imageUrl = String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                awsRegion,
                imageKey
        );

        product.setImageKey(imageKey);
        product.setImageUrl(imageUrl);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct =
                productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {

        Product product = findProductById(id);

        if (product.getImageKey() != null) {
            s3StorageService.deleteFile(product.getImageKey());
        }

        productRepository.delete(product);
    }

    private Product findProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with ID: " + id
                        )
                );
    }

    private ProductResponse mapToResponse(Product product) {

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .imageKey(product.getImageKey())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    @Override
    public ProductResponse reduceStock(
            Long productId,
            Integer quantity) {

        Product product = findProductById(productId);

        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock for product "
                            + product.getName()
                            + ". Available: "
                            + product.getStockQuantity()
                            + ", requested: "
                            + quantity
            );
        }

        product.setStockQuantity(
                product.getStockQuantity() - quantity
        );

        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct =
                productRepository.save(product);

        return mapToResponse(updatedProduct);
    }
}