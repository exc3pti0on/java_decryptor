package org.example.formatters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class JSONOutputFormat extends OutputFormat {
    private static final ObjectMapper objectMapper = new ObjectMapper( );

    public JSONOutputFormat (List<Map<String, String>> pwstore, String cmdargs) {
        super(pwstore, cmdargs);
    }

    @Override
    public void output ( ) throws JsonProcessingException {
        System.out.println(objectMapper.writeValueAsString(objectMapper));
    }

    @Override
    public OutputFormat init (List<Map<String, String>> pwstore, String cmdargs) {
        return new JSONOutputFormat(pwstore, cmdargs);
    }
}
