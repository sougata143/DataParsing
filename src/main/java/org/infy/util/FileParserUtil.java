package org.infy.util;

import org.infy.model.ParseResult;
import org.infy.model.ParserConfig;
import org.infy.model.Person;
import org.infy.parser.DataParser;
import org.infy.parser.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileParserUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileParserUtil.class);
    
    @SuppressWarnings("unchecked")
    public static <T> ParseResult<T> parseFile(String filePath, ParserConfig config, Class<T> targetClass) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase();
        
        try (InputStream inputStream = new FileInputStream(filePath)) {
            if (extension.equals("csv") && targetClass == Person.class) {
                // Special handling for CSV to Person mapping
                CsvParser csvParser = new CsvParser(config);
                ParseResult<Map<String, String>> csvResult = csvParser.parse(
                    inputStream, 
                    (Class<Map<String, String>>) (Class<?>) Map.class
                );
                
                // Convert the CSV result to Person objects
                List<Person> persons = csvResult.parsedData().stream()
                    .map(CsvPersonMapper::mapToPerson)
                    .collect(Collectors.toList());
                
                // Create new ParseResult with Person objects
                return (ParseResult<T>) new ParseResult<>(
                    persons,
                    csvResult.errors(),
                    csvResult.stats()
                );
            } else {
                DataParser<T> parser = getParser(extension, config);
                ParseResult<T> result = parser.parse(inputStream, targetClass);
                writeResultToFile(result, fileName);
                return result;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + filePath, e);
        }
    }
    
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        throw new IllegalArgumentException("File has no extension: " + fileName);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> DataParser<T> getParser(String extension, ParserConfig config) {
        return switch (extension) {
            case "csv" -> (DataParser<T>) new CsvParser(config);
            case "json" -> (DataParser<T>) new JsonParser<>(config);
            case "xml" -> (DataParser<T>) new XmlParser<>(config, new PersonXmlMapper());
            default -> throw new IllegalArgumentException("Unsupported file extension: " + extension);
        };
    }
    
    private static <T> void writeResultToFile(ParseResult<T> result, String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String outputFileName = "parsed_" + originalFileName + "_" + timestamp + ".txt";
        Path outputPath = Paths.get("output", outputFileName);
        
        try {
            Files.createDirectories(outputPath.getParent());
            
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath))) {
                // Write parsing statistics
                writer.println("=== Parsing Statistics ===");
                writer.println("Total Records: " + result.stats().totalRecords());
                writer.println("Valid Records: " + result.stats().validRecords());
                writer.println("Invalid Records: " + result.stats().invalidRecords());
                writer.println("Processing Time: " + result.stats().processingTime().toMillis() + "ms");
                writer.println("Memory Used: " + result.stats().memoryUsed() + " bytes");
                writer.println();
                
                // Write CSV-specific stats if available
                if (result instanceof ParseResult<?> && result.parsedData() instanceof List<?> && 
                    !result.parsedData().isEmpty() && result.parsedData().get(0) instanceof Map) {
                    writer.println("=== CSV Field Analysis ===");
                    Map<String, String> firstRecord = (Map<String, String>) result.parsedData().get(0);
                    writer.println("Fields: " + String.join(", ", firstRecord.keySet()));
                    writer.println("Total Rows: " + result.parsedData().size());
                    writer.println();
                }
                
                // Write parsed data
                writer.println("=== Parsed Data ===");
                result.parsedData().forEach(item -> writer.println(item));
                
                // Write errors if any
                if (!result.errors().isEmpty()) {
                    writer.println("\n=== Parsing Errors ===");
                    result.errors().forEach(error -> {
                        writer.println("Line " + error.lineNumber() + ": " + error.message());
                        writer.println("Content: " + error.rawContent());
                        writer.println("Severity: " + error.severity());
                        if (error.cause() != null) {
                            writer.println("Cause: " + error.cause().getMessage());
                        }
                        writer.println();
                    });
                }
            }
            
            logger.info("Results written to file: {}", outputPath);
        } catch (IOException e) {
            logger.error("Error writing results to file", e);
        }
    }
} 