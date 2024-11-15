package org.infy.parser.impl;

import org.infy.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class CsvPersonMapper {
    private static final Logger logger = LoggerFactory.getLogger(CsvPersonMapper.class);
    
    public static Person mapToPerson(Map<String, String> data) {
        String name = data.getOrDefault("name", "");
        String city = data.getOrDefault("city", "");
        int age = parseAge(data.get("age"));
        
        return new Person(name, age, city);
    }
    
    private static int parseAge(String ageStr) {
        if (ageStr == null || ageStr.trim().isEmpty()) {
            logger.debug("Age is blank or null, defaulting to 0");
            return 0;
        }
        try {
            return Integer.parseInt(ageStr.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid age format: {}, defaulting to 0", ageStr);
            return 0;
        }
    }
} 