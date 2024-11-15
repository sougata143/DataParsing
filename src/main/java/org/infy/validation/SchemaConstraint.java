package org.infy.validation;

import java.util.function.Predicate;

public record SchemaConstraint(
    Class<?> type,
    boolean required,
    int maxLength,
    Predicate<Object> customValidation
) {
    public SchemaConstraint {
        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength must be non-negative");
        }
    }
    
    public static SchemaConstraint of(Class<?> type) {
        return new SchemaConstraint(type, false, Integer.MAX_VALUE, null);
    }
    
    public static SchemaConstraint required(Class<?> type) {
        return new SchemaConstraint(type, true, Integer.MAX_VALUE, null);
    }
    
    public static SchemaConstraint withMaxLength(Class<?> type, int maxLength) {
        return new SchemaConstraint(type, false, maxLength, null);
    }
    
    public static SchemaConstraint withCustomValidation(Class<?> type, Predicate<Object> validation) {
        return new SchemaConstraint(type, false, Integer.MAX_VALUE, validation);
    }
} 