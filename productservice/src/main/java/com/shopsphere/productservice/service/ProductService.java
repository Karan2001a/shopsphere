package com.shopsphere.productservice.service;

import com.shopsphere.productservice.dto.ProductRequest;
import com.shopsphere.productservice.dto.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(
            Long id,
            ProductRequest request
    );

    ProductResponse uploadProductImage(
            Long id,
            MultipartFile file
    );

    ProductResponse reduceStock(
            Long productId,
            Integer quantity
    );

    void deleteProduct(Long id);
}