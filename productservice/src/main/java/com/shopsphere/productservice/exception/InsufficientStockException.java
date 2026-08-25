package com.shopsphere.productservice.exception;
import com.shopsphere.productservice.exception.InsufficientStockException;

public class InsufficientStockException
        extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}