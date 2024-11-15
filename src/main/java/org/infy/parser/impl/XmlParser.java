package org.infy.parser.impl;

import org.infy.model.*;
import org.infy.parser.DataParser;
import org.infy.validation.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class XmlParser<T> implements DataParser<T> {
    private static final Logger logger = LoggerFactory.getLogger(XmlParser.class);
    private final ParserConfig config;
    private final XMLInputFactory xmlInputFactory;
    private final XmlMapper<T> xmlMapper;

    public XmlParser(ParserConfig config, XmlMapper<T> xmlMapper) {
        this.config = config;
        this.xmlMapper = xmlMapper;
        this.xmlInputFactory = XMLInputFactory.newInstance();
        // Disable external entity processing for security
        this.xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        this.xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    @Override
    public ParseResult<T> parse(InputStream input, Class<T> targetClass) {
        Instant start = Instant.now();
        List<T> parsedData = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();
        int lineNumber = 1;

        try {
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(input);
            Map<String, String> currentElement = new HashMap<>();
            String currentTag = "";

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        currentTag = reader.getLocalName();
                        // Handle attributes
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            currentElement.put(
                                currentTag + "@" + reader.getAttributeLocalName(i),
                                reader.getAttributeValue(i)
                            );
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        if (!reader.isWhiteSpace() && !currentTag.isEmpty()) {
                            currentElement.put(currentTag, reader.getText().trim());
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (reader.getLocalName().equals(xmlMapper.getRootElement())) {
                            try {
                                T mapped = xmlMapper.mapToObject(currentElement, targetClass);
                                parsedData.add(mapped);
                                currentElement = new HashMap<>();
                            } catch (Exception e) {
                                errors.add(new ParseError(
                                    "Failed to map XML element to object",
                                    lineNumber,
                                    currentElement.toString(),
                                    ErrorSeverity.ERROR,
                                    e
                                ));
                            }
                        }
                        break;
                }
                lineNumber++;
            }
            reader.close();
        } catch (XMLStreamException e) {
            errors.add(new ParseError(
                "Failed to parse XML",
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
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(input);
            return StreamSupport.stream(
                new XmlSpliterator<>(reader, xmlMapper, targetClass),
                false
            );
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XML stream", e);
        }
    }

    @Override
    public void validate(InputStream input, Schema schema) {
        throw new UnsupportedOperationException("XML schema validation not implemented yet");
    }
} 