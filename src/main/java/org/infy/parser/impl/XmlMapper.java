package org.infy.parser.impl;

import java.util.Map;

public interface XmlMapper<T> {
    T mapToObject(Map<String, String> elements, Class<T> targetClass);
    String getRootElement();
} 