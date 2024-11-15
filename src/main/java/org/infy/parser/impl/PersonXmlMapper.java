package org.infy.parser.impl;

import org.infy.model.Person;
import java.util.Map;

public class PersonXmlMapper implements XmlMapper<Person> {
    @Override
    public Person mapToObject(Map<String, String> elements, Class<Person> targetClass) {
        return new Person(
            elements.get("name"),
            Integer.parseInt(elements.get("age")),
            elements.get("city")
        );
    }

    @Override
    public String getRootElement() {
        return "person";
    }
} 