package org.infy.parser.impl;

import org.infy.model.*;
import org.infy.parser.DataParser;
import org.infy.validation.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class CsvParser implements DataParser<Map<String, String>> {
    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);
    private final ParserConfig config;
    private Map<String, Integer> blankFieldCounts;
    private int totalRows;
    
    public CsvParser(ParserConfig config) {
        this.config = config;
        this.blankFieldCounts = new HashMap<>();
        this.totalRows = 0;
    }

    @Override
    public ParseResult<Map<String, String>> parse(InputStream input, Class<Map<String, String>> targetClass) {
        Instant start = Instant.now();
        List<Map<String, String>> parsedData = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();
        blankFieldCounts.clear();
        totalRows = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, config.encoding()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            String[] headers = headerLine.split(",");
            Arrays.stream(headers).forEach(header -> blankFieldCounts.put(header.trim(), 0));
            logger.debug("CSV Headers: {}", Arrays.toString(headers));
            
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                totalRows++;
                if (config.skipEmptyLines() && line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Map<String, String> record = parseLine(line, headers);
                    updateBlankFieldCounts(record);
                    logger.debug("Parsed line {}: {}", lineNumber, record);
                    parsedData.add(record);
                } catch (Exception e) {
                    logger.error("Error parsing line {}: {}", lineNumber, line, e);
                    errors.add(new ParseError(
                        e.getMessage(),
                        lineNumber,
                        line,
                        ErrorSeverity.ERROR,
                        e
                    ));
                }
            }
        } catch (IOException e) {
            errors.add(new ParseError(
                "Failed to read CSV file",
                0,
                "",
                ErrorSeverity.FATAL,
                e
            ));
        }
        
        Duration processingTime = Duration.between(start, Instant.now());
        CsvStats csvStats = CsvStats.create(totalRows, blankFieldCounts.size(), blankFieldCounts);
        logCsvStats(csvStats, processingTime);
        
        ParsingStats stats = new ParsingStats(
            parsedData.size() + errors.size(),
            parsedData.size(),
            errors.size(),
            processingTime,
            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        );
        
        return new ParseResult<>(parsedData, errors, stats);
    }

    private void updateBlankFieldCounts(Map<String, String> record) {
        record.forEach((key, value) -> {
            if (value == null || value.trim().isEmpty()) {
                blankFieldCounts.merge(key, 1, Integer::sum);
            }
        });
    }

    private void logCsvStats(CsvStats stats, Duration processingTime) {
        StringBuilder report = new StringBuilder();
        report.append("\n=== CSV Parsing Report ===\n");
        report.append(String.format("Total Rows: %d\n", stats.totalRows()));
        report.append(String.format("Total Columns: %d\n", stats.totalColumns()));
        report.append(String.format("Processing Time: %dms\n", processingTime.toMillis()));
        report.append("\nBlank Field Analysis:\n");
        
        stats.blankFieldCounts().forEach((field, count) -> {
            double percentage = stats.blankFieldPercentages().get(field);
            report.append(String.format("- %s: %d blanks (%.2f%%)\n", field, count, percentage));
        });
        
        logger.info(report.toString());
    }

    @Override
    public Stream<Map<String, String>> parseStream(InputStream input, Class<Map<String, String>> targetClass) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, config.encoding()));
        String[] headers;
        try {
            headers = reader.readLine().split(",");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV headers", e);
        }
        
        return reader.lines()
            .filter(line -> !config.skipEmptyLines() || !line.trim().isEmpty())
            .map(line -> {
                try {
                    return parseLine(line, headers);
                } catch (Exception e) {
                    logger.error("Error parsing line: {}", line, e);
                    return null;
                }
            })
            .filter(Objects::nonNull);
    }

    @Override
    public void validate(InputStream input, Schema schema) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, config.encoding()))) {
            String[] headers = reader.readLine().split(",");
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Map<String, String> record = parseLine(line, headers);
                    List<String> errors = schema.validate(record);
                    if (!errors.isEmpty()) {
                        logger.warn("Validation errors at line {}: {}", lineNumber, errors);
                    }
                } catch (Exception e) {
                    logger.error("Error validating line {}: {}", lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to validate CSV file", e);
        }
    }

    private Map<String, String> parseLine(String line, String[] headers) {
        String[] values = line.split(",", -1);
        Map<String, String> record = new HashMap<>();
        
        int maxLength = Math.max(headers.length, values.length);
        
        for (int i = 0; i < maxLength; i++) {
            String header = i < headers.length ? headers[i].trim() : "Column" + (i + 1);
            String value = i < values.length ? values[i].trim() : "";
            record.put(header, value);
            
            if (i >= headers.length) {
                logger.warn("Extra value found at position {}: {}", i, value);
            } else if (i >= values.length) {
                logger.warn("Missing value for header: {}", header);
            }
        }
        
        return record;
    }
} 