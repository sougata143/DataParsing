package org.infy.model;

public record ParseError(
    String message,
    int lineNumber,
    String rawContent,
    ErrorSeverity severity,
    Exception cause
) {} 