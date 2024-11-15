package org.infy;

import org.infy.model.ParserConfig;
import org.infy.model.Person;
import org.infy.util.FileParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String DATA_DIR = "src/main/resources/data";

    public static void main(String[] args) {
        logger.info("Starting Data Parsing Application");

        // Configure parser
        ParserConfig config = new ParserConfig(
            1000,
            false,
            StandardCharsets.UTF_8,
            true,
            new HashMap<>()
        );

        // Create sample files for testing only if directory is empty
        createSampleFilesIfNeeded();

        // Process each sample file
        try {
            processFile(DATA_DIR + "/sample.csv", config);
            processFile(DATA_DIR + "/sample.json", config);
            processFile(DATA_DIR + "/sample.xml", config);
        } catch (Exception e) {
            logger.error("Error processing files", e);
        }

        logger.info("Data Parsing Application completed");
    }

    private static void processFile(String filePath, ParserConfig config) {
        try {
            if (!Files.exists(Path.of(filePath))) {
                logger.warn("File not found: {}", filePath);
                return;
            }

            logger.info("Processing file: {}", filePath);
            var result = FileParserUtil.parseFile(filePath, config, Person.class);

            // Log results to consolė
            logger.info("Parsing completed for {}. Stats: {}", filePath, result.stats());
            result.parsedData().forEach(person ->
                logger.info("Parsed Person: {}", person)
            );

            if (!result.errors().isEmpty()) {
                logger.warn("Found {} errors during parsing", result.errors().size());
                result.errors().forEach(error ->
                    logger.error("Error at line {}: {}", error.lineNumber(), error.message())
                );
            }
        } catch (Exception e) {
            logger.error("Failed to process file: {}", filePath, e);
        }
    }

    private static void createSampleFilesIfNeeded() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            
            // Create directory if it doesn't exist
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            
            // Only create sample files if directory is empty
            if (Files.list(dataDir).findFirst().isEmpty()) {
//                logger.info("Creating sample files in empty directory");
//
//                // Create CSV sample
//                Path csvPath = dataDir.resolve("sample.csv");
//                String csvContent = """
//                    name,age,city
//                    John,30,New York
//                    Jane,,London
//                    Bob,45,
//                    Mary,
//                    ,35,Chicago
//                    """;
//                Files.writeString(csvPath, csvContent);
//
//                // Create JSON sample
//                Path jsonPath = dataDir.resolve("sample.json");
//                String jsonContent = """
//                    [
//                        {"name": "John", "age": 30, "city": "New York"},
//                        {"name": "Jane", "age": 25, "city": "London"}
//                    ]
//                    """;
//                Files.writeString(jsonPath, jsonContent);
//
//                // Create XML sample
//                Path xmlPath = dataDir.resolve("sample.xml");
//                String xmlContent = """
//                    <?xml version="1.0" encoding="UTF-8"?>
//                    <people>
//                        <person>
//                            <name>John</name>
//                            <age>30</age>
//                            <city>New York</city>
//                        </person>
//                        <person>
//                            <name>Jane</name>
//                            <age>25</age>
//                            <city>London</city>
//                        </person>
//                    </people>
//                    """;
//                Files.writeString(xmlPath, xmlContent);
//
//                logger.info("Sample files created successfully");
            } else {
                logger.info("Using existing files in {}", DATA_DIR);
            }
        } catch (IOException e) {
            logger.error("Error handling sample files", e);
        }
    }
}