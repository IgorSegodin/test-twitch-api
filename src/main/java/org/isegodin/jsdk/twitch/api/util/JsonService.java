package org.isegodin.jsdk.twitch.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author isegodin
 */
public class JsonService {

    protected ObjectMapper objectMapper;

    {
        objectMapper = new ObjectMapper();
    }

    public <T> T fromJson(String jsonString, Class<T> type) {
        try {
            return objectMapper.readValue(jsonString, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson(Object object) {
        try {
            StringWriter stringWriter = new StringWriter();
            objectMapper.writeValue(stringWriter, object);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
