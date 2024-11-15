package org.infy.transformer;

import java.util.List;

public interface DataTransformer<T, R> {
    R transform(T input);
    List<R> transformBatch(List<T> input);
} 