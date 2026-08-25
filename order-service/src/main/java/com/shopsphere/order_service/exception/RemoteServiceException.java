package com.shopsphere.order_service.exception;

public class RemoteServiceException extends RuntimeException {

    public RemoteServiceException(String message) {
        super(message);
    }

    public RemoteServiceException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}