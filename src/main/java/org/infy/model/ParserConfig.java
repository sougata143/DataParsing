package org.infy.model;

import java.nio.charset.Charset;
import java.util.Map;

public record ParserConfig(
    int batchSize,
    boolean validateSchema,
    Charset encoding,
    boolean skipEmptyLines,
    Map<String, String> customOptions
) {} 