package org.infy.validation;

import java.util.List;
import java.util.Map;

public interface Schema {
    /**
     * Validates if the data structure matches the schema definition
     * @param data The data to validate
     * @return List of validation errors, empty if valid
     */
    List<String> validate(Object data);
    
    /**
     * Gets the schema constraints
     * @return Map of field names to their constraints
     */
    Map<String, SchemaConstraint> getConstraints();
} 