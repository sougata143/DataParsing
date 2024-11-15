package org.infy.parser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Spliterator;
import java.util.function.Consumer;

public class JsonSpliterator<T> implements Spliterator<T> {
    private static final Logger logger = LoggerFactory.getLogger(JsonSpliterator.class);
    private final JsonParser parser;
    private final ObjectMapper objectMapper;
    private final Class<T> targetClass;
    private boolean started = false;

    public JsonSpliterator(JsonParser parser, ObjectMapper objectMapper, Class<T> targetClass) {
        this.parser = parser;
        this.objectMapper = objectMapper;
        this.targetClass = targetClass;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        try {
            if (!started) {
                JsonToken token = parser.nextToken();
                if (token != JsonToken.START_ARRAY) {
                    throw new IllegalStateException("Expected content to be an array");
                }
                started = true;
            }

            JsonToken token = parser.nextToken();
            if (token == JsonToken.END_ARRAY) {
                return false;
            }

            T item = objectMapper.readValue(parser, targetClass);
            action.accept(item);
            return true;
        } catch (Exception e) {
            logger.error("Error reading JSON stream", e);
            return false;
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null; // JSON parsing is sequential in this implementation
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL;
    }
} 