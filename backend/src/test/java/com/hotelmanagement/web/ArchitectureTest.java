package com.hotelmanagement.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureTest {
    @Test
    void webBackendHasAnExplicitApplicationBoundary() {
        assertTrue(WebHotelMisApplication.class.getPackageName().startsWith("com.hotelmanagement.web"));
    }
}
