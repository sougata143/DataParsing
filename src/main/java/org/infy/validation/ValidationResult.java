package org.infy.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ValidationResult(
    boolean valid,
    List<String> errors
) {
    public ValidationResult {
        errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }

    public static ValidationResult createValid() {
        return new ValidationResult(true, Collections.emptyList());
    }

    public static ValidationResult createInvalid(List<String> errors) {
        return new ValidationResult(false, errors);
    }

    public static ValidationResult createInvalid(String error) {
        return new ValidationResult(false, List.of(error));
    }
} 