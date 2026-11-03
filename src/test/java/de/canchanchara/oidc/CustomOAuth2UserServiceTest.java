package de.canchanchara.oidc;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomOAuth2UserServiceTest {

    @Test
    void copiesDataFieldsToTopLevel() {
        Map<String, Object> result = CustomOAuth2UserService.flatten(Map.of(
                "email", "max@example.org",
                "data", Map.of("userName", "max", "firstName", "Max", "email", "other@example.org")));

        assertEquals("max", result.get("userName"));
        assertEquals("Max", result.get("firstName"));
        // oberste Ebene gewinnt
        assertEquals("max@example.org", result.get("email"));
    }

    @Test
    void worksWithoutData() {
        Map<String, Object> result = CustomOAuth2UserService.flatten(Map.of("id", 42, "userName", "max"));

        assertEquals(Map.of("id", 42, "userName", "max"), result);
    }
}
