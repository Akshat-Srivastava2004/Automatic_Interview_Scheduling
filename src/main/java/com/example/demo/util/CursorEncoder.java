package com.example.demo.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class CursorEncoder {
    
    private static final String SEPARATOR = "::";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public static String encode(LocalDateTime dateTime, Long id) {
        String data = dateTime.format(FORMATTER) + SEPARATOR + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes());
    }
    
    public static Cursor decode(String cursor) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = decoded.split(SEPARATOR);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid cursor format");
            }
            LocalDateTime dateTime = LocalDateTime.parse(parts[0], FORMATTER);
            Long id = Long.parseLong(parts[1]);
            return new Cursor(dateTime, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor: " + cursor, e);
        }
    }
    
    public static class Cursor {
        private final LocalDateTime dateTime;
        private final Long id;
        
        public Cursor(LocalDateTime dateTime, Long id) {
            this.dateTime = dateTime;
            this.id = id;
        }
        
        public LocalDateTime getDateTime() {
            return dateTime;
        }
        
        public Long getId() {
            return id;
        }
    }
}

