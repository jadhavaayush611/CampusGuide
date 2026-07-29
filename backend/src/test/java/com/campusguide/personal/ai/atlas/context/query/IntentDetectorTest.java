package com.campusguide.personal.ai.atlas.context.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntentDetectorTest {

    private IntentDetector intentDetector;

    @BeforeEach
    void setUp() {
        intentDetector = new IntentDetector();
    }

    @Test
    @DisplayName("Should detect ACADEMIC_INQUIRY intent and domain")
    void testDetect_Academic() {
        IntentDetector.IntentResult result = intentDetector.detectIntent("What is my current GPA and course grade?", List.of());

        assertEquals(QueryIntent.ACADEMIC_INQUIRY, result.intent());
        assertEquals(QueryDomain.ACADEMIC, result.domain());
        assertTrue(result.confidence() >= 0.70);
    }

    @Test
    @DisplayName("Should detect PLANNER_LOOKUP intent and domain")
    void testDetect_Planner() {
        IntentDetector.IntentResult result = intentDetector.detectIntent("Show my pending tasks and todo list deadlines", List.of());

        assertEquals(QueryIntent.PLANNER_LOOKUP, result.intent());
        assertEquals(QueryDomain.PLANNER, result.domain());
        assertTrue(result.confidence() >= 0.70);
    }

    @Test
    @DisplayName("Should detect CALENDAR_EVENT intent and domain")
    void testDetect_Calendar() {
        IntentDetector.IntentResult result = intentDetector.detectIntent("What is on my schedule for today?", List.of());

        assertEquals(QueryIntent.CALENDAR_EVENT, result.intent());
        assertEquals(QueryDomain.CALENDAR, result.domain());
        assertTrue(result.confidence() >= 0.70);
    }

    @Test
    @DisplayName("Should detect CAMPUS_NAVIGATION intent and domain")
    void testDetect_Campus() {
        IntentDetector.IntentResult result = intentDetector.detectIntent("Where is the science hall library located?", List.of());

        assertEquals(QueryIntent.CAMPUS_NAVIGATION, result.intent());
        assertEquals(QueryDomain.CAMPUS, result.domain());
        assertTrue(result.confidence() >= 0.70);
    }

    @Test
    @DisplayName("Should detect USER_PROFILE intent and domain")
    void testDetect_UserProfile() {
        IntentDetector.IntentResult result = intentDetector.detectIntent("What is my student name and profile info?", List.of());

        assertEquals(QueryIntent.USER_PROFILE, result.intent());
        assertEquals(QueryDomain.USER, result.domain());
        assertTrue(result.confidence() >= 0.70);
    }

    @Test
    @DisplayName("Should detect GENERAL_CONVERSATION intent and domain for greetings")
    void testDetect_GeneralConversation() {
        IntentDetector.IntentResult result = intentDetector.detectIntent("Hello, how are you?", List.of());

        assertEquals(QueryIntent.GENERAL_CONVERSATION, result.intent());
        assertEquals(QueryDomain.GENERAL, result.domain());
    }
}
