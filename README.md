# Data Parsing Library

A robust Java library for parsing CSV, XML, and JSON files with support for streaming, validation, and transformation.

## Features 

- Support for multiple file formats:
  - CSV
  - XML
  - JSON
- Streaming and batch processing
- Schema validation
- Data transformation
- Custom filtering
- Extensive error handling
- Detailed parsing statistics
- Blank field analysis for CSV files
- Output generation in text format

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Dependencies

- Jackson (JSON processing)
- StAX (XML processing)
- SLF4J (Logging)
- Logback (Logging implementation)
- JUnit 5 (Testing)
- AssertJ (Testing assertions)

## Installation

1. Clone the repository: 
git clone https://github.com/yourusername/DataParsing.git


2. Build the project:
bash
mvn clean install


## Usage

### Basic Usage

java
// Configure parser
ParserConfig config = new ParserConfig(
1000, // batchSize
false, // validateSchema
StandardCharsets.UTF_8, // encoding
true, // skipEmptyLines
new HashMap<>() // customOptions
);
// Process CSV file
String csvPath = "path/to/file.csv";
ParseResult<Person> result = FileParserUtil.parseFile(csvPath, config, Person.class);
// Access parsed data
result.parsedData().forEach(person ->
System.out.println("Person: " + person)
);
// Check parsing statistics
System.out.println("Total Records: " + result.stats().totalRecords());
System.out.println("Valid Records: " + result.stats().validRecords());
System.out.println("Processing Time: " + result.stats().processingTime().toMillis() + "ms");


### Supported File Formats

#### CSV
- Handles header row
- Supports custom delimiters
- Tracks blank fields
- Provides detailed statistics

#### XML
- Streaming parser using StAX
- Custom mapping support
- Attribute handling
- Security features (external entity processing disabled)

#### JSON
- Streaming support for large files
- Object mapping using Jackson
- Comments support
- Strict parsing mode

### Output Format

The parser generates detailed output files containing:
- Parsing statistics
- Data analysis
- Parsed records
- Error reports

Output files are created in the `output` directory with timestamps.

## Error Handling

The library provides comprehensive error handling:
- Detailed error messages
- Line number tracking
- Error severity levels (FATAL, ERROR, WARNING)
- Original content preservation
- Exception cause tracking

## Configuration Options

### Parser Configuration

java
ParserConfig config = new ParserConfig(
batchSize, // Number of records to process in batch
validateSchema, // Enable/disable schema validation
encoding, // Character encoding
skipEmptyLines, // Skip empty lines in input
customOptions // Additional parser-specific options
);


## Project Structure

src/
├── main/
│ ├── java/
│ │ └── org/infy/
│ │ ├── model/ # Data models
│ │ ├── parser/ # Parser implementations
│ │ ├── transformer/ # Data transformers
│ │ ├── validation/ # Validation logic
│ │ └── util/ # Utility classes
│ └── resources/
│ └── data/ # Sample data files
└── test/
└── java/ # Test classes


## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Jackson library for JSON processing
- StAX for XML processing
- SLF4J and Logback for logging

## Contact

Your Name - your.email@example.com
Project Link: https://github.com/yourusername/DataParsing
