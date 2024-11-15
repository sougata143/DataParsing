package org.infy.parser;

import org.infy.model.ParseResult;
import org.infy.validation.Schema;

import java.io.InputStream;
import java.util.stream.Stream;

public interface DataParser<T> {
    ParseResult<T> parse(InputStream input, Class<T> targetClass);
    Stream<T> parseStream(InputStream input, Class<T> targetClass);
    void validate(InputStream input, Schema schema);
} 