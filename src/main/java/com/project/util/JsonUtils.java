package com.project.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T readJson(File file, Class<T> clazz) throws IOException {
        return mapper.readValue(file, clazz);
    }

    public static void writeJson(File file, Object obj) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
    }
}