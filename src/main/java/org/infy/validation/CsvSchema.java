package org.infy.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvSchema implements Schema {
    private final Map<String, SchemaConstraint> constraints;
    
    public CsvSchema(Map<String, SchemaConstraint> constraints) {
        this.constraints = new HashMap<>(constraints);
    }
    
    @Override
    public List<String> validate(Object data) {
        List<String> errors = new ArrayList<>();
        if (!(data instanceof Map<?, ?> record)) {
            errors.add("Data must be a Map");
            return errors;
        }
        
        for (Map.Entry<String, SchemaConstraint> entry : constraints.entrySet()) {
            String field = entry.getKey();
            SchemaConstraint constraint = entry.getValue();
            Object value = record.get(field);
            
            // Check required fields
            if (constraint.required() && value == null) {
                errors.add("Required field missing: " + field);
                continue;
            }
            
            if (value != null) {
                // Type validation
                if (!constraint.type().isInstance(value)) {
                    errors.add("Invalid type for field " + field + ": expected " + 
                             constraint.type().getSimpleName());
                }
                
                // Length validation for strings
                if (value instanceof String str && str.length() > constraint.maxLength()) {
                    errors.add("Field " + field + " exceeds maximum length of " + 
                             constraint.maxLength());
                }
                
                // Custom validation
                if (constraint.customValidation() != null && 
                    !constraint.customValidation().test(value)) {
                    errors.add("Custom validation failed for field " + field);
                }
            }
        }
        
        return errors;
    }
    
    @Override
    public Map<String, SchemaConstraint> getConstraints() {
        return new HashMap<>(constraints);
    }
    
    public static class Builder {
        private final Map<String, SchemaConstraint> constraints = new HashMap<>();
        
        public Builder addField(String name, SchemaConstraint constraint) {
            constraints.put(name, constraint);
            return this;
        }
        
        public CsvSchema build() {
            return new CsvSchema(constraints);
        }
    }
} 