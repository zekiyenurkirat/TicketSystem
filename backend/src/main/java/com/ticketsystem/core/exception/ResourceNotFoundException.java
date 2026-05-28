package com.ticketsystem.core.exception;

/** İstenen kaynağın veritabanında bulunamadığı durumlarda fırlatılır. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
