package org.infy.parser.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.infy.model.*;
import org.infy.parser.DataParser;
import org.infy.validation.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonParser<T> implements DataParser<T> {
    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
    private final ParserConfig config;
    private final JsonFactory jsonFactory;
    private final ObjectMapper objectMapper;

    public JsonParser(ParserConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.jsonFactory = new JsonFactory()
            .enable(Feature.ALLOW_COMMENTS)
            .disable(Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .disable(Feature.ALLOW_SINGLE_QUOTES);
    }

    @Override
    public ParseResult<T> parse(InputStream input, Class<T> targetClass) {
        Instant start = Instant.now();
        List<T> parsedData = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();
        int lineNumber = 1;

        try (com.fasterxml.jackson.core.JsonParser parser = jsonFactory.createParser(input)) {
            // Check if input starts with an array
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected content to be an array");
            }

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                try {
                    T item = objectMapper.readValue(parser, targetClass);
                    parsedData.add(item);
                } catch (Exception e) {
                    errors.add(new ParseError(
                        "Failed to parse JSON object",
                        lineNumber,
                        parser.getCurrentLocation().toString(),
                        ErrorSeverity.ERROR,
                        e
                    ));
                }
                lineNumber++;
            }
        } catch (Exception e) {
            errors.add(new ParseError(
                "Failed to parse JSON",
                lineNumber,
                "",
                ErrorSeverity.FATAL,
                e
            ));
        }

        ParsingStats stats = new ParsingStats(
            parsedData.size() + errors.size(),
            parsedData.size(),
            errors.size(),
            Duration.between(start, Instant.now()),
            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        );

        return new ParseResult<>(parsedData, errors, stats);
    }

    @Override
    public Stream<T> parseStream(InputStream input, Class<T> targetClass) {
        try {
            com.fasterxml.jackson.core.JsonParser parser = jsonFactory.createParser(input);
            return StreamSupport.stream(
                new JsonSpliterator<>(parser, objectMapper, targetClass),
                false
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JSON stream", e);
        }
    }

    @Override
    public void validate(InputStream input, Schema schema) {
        throw new UnsupportedOperationException("JSON schema validation not implemented yet");
    }
} 