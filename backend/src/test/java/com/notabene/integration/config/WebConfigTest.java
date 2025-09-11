package com.notabene.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.notabene.config.WebConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WebConfig.class)
@ActiveProfiles("test")
@DisplayName("WebConfig Integration Tests")
class WebConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create ObjectMapper bean")
    void shouldCreateObjectMapperBean() {
        // Then
        assertNotNull(objectMapper);
    }

    @Test
    @DisplayName("Should register JavaTimeModule")
    void shouldRegisterJavaTimeModule() {
        // Test by trying to serialize/deserialize a LocalDateTime
        // This will fail if JavaTimeModule is not registered
        try {
            LocalDateTime dateTime = LocalDateTime.now();
            String json = objectMapper.writeValueAsString(dateTime);
            LocalDateTime restored = objectMapper.readValue(json, LocalDateTime.class);
            assertNotNull(restored);
        } catch (Exception e) {
            fail("JavaTimeModule not properly registered: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should disable writing dates as timestamps")
    void shouldDisableWritingDatesAsTimestamps() {
        // Then
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    @DisplayName("Should serialize LocalDateTime correctly")
    void shouldSerializeLocalDateTimeCorrectly() throws Exception {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 15, 30, 45);

        // When
        String json = objectMapper.writeValueAsString(dateTime);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("2023-12-25"));
        assertTrue(json.contains("15:30:45"));
        // Should be ISO format, not timestamp
        assertFalse(json.matches("\\d+"));
    }

    @Test
    @DisplayName("Should deserialize LocalDateTime correctly")
    void shouldDeserializeLocalDateTimeCorrectly() throws Exception {
        // Given
        String json = "\"2023-12-25T15:30:45\"";

        // When
        LocalDateTime dateTime = objectMapper.readValue(json, LocalDateTime.class);

        // Then
        assertNotNull(dateTime);
        assertEquals(2023, dateTime.getYear());
        assertEquals(12, dateTime.getMonthValue());
        assertEquals(25, dateTime.getDayOfMonth());
        assertEquals(15, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
        assertEquals(45, dateTime.getSecond());
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() throws Exception {
        // When
        String json = objectMapper.writeValueAsString(null);

        // Then
        assertEquals("null", json);
    }
}
