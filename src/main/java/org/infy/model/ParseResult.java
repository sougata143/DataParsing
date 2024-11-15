package org.infy.model;

import java.util.List;

public record ParseResult<T>(
    List<T> parsedData,
    List<ParseError> errors,
    ParsingStats stats
) {} 