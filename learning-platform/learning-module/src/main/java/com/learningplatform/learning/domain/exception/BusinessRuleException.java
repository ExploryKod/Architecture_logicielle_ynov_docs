package com.learningplatform.learning.domain.exception;

/**
 * Exception levée lorsqu'une règle métier est violée.
 */
public class BusinessRuleException extends RuntimeException {
    
    public BusinessRuleException(String message) {
        super(message);
    }
    
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
