package org.infy.parser.impl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class XmlSpliterator<T> implements Spliterator<T> {
    private final XMLStreamReader reader;
    private final XmlMapper<T> mapper;
    private final Class<T> targetClass;
    private String currentTag = "";

    public XmlSpliterator(XMLStreamReader reader, XmlMapper<T> mapper, Class<T> targetClass) {
        this.reader = reader;
        this.mapper = mapper;
        this.targetClass = targetClass;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        try {
            Map<String, String> currentElement = new HashMap<>();
            
            while (reader.hasNext()) {
                int event = reader.next();
                
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        currentTag = reader.getLocalName();
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
                        if (reader.getLocalName().equals(mapper.getRootElement())) {
                            T mapped = mapper.mapToObject(currentElement, targetClass);
                            action.accept(mapped);
                            return true;
                        }
                        break;
                }
            }
            return false;
        } catch (XMLStreamException e) {
            throw new RuntimeException("Error reading XML stream", e);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null; // XML parsing is inherently sequential
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