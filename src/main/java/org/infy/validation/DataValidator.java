package org.infy.validation;

import java.util.List;

public interface DataValidator<T> {
    ValidationResult validate(T data);
    List<ValidationResult> validateBatch(List<T> data);
} 