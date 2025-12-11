package org.example.bookaroo.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s nie znaleziono z %s : '%s'", resourceName, fieldName, fieldValue));
    }
}