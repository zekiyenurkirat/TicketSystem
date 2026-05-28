package com.ticketsystem.core.exception;

/** Kaynak mevcut olsa da iş kuralı nedeniyle işlem gerçekleştirilemediğinde fırlatılır. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
