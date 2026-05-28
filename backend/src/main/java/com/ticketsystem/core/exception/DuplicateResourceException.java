package com.ticketsystem.core.exception;

/** Benzersizlik kısıtı ihlal edildiğinde fırlatılır. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
