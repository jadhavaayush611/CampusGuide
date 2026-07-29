package com.campusguide.personal.ai.atlas.context.query;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

/**
 * Normalizes conversational temporal expressions (e.g. "tomorrow", "next week", "after lunch")
 * into structured temporal ranges.
 */
@Component
public class TemporalExpressionResolver {

    /**
     * Resolves temporal expressions from text using current local time.
     */
    public TemporalInformation resolve(String text) {
        return resolve(text, LocalDateTime.now());
    }

    /**
     * Resolves temporal expressions from text using a specific reference time.
     *
     * @param text input query or text fragment
     * @param referenceTime reference timestamp for relative calculation
     * @return TemporalInformation object
     */
    public TemporalInformation resolve(String text, LocalDateTime referenceTime) {
        if (text == null || text.isBlank()) {
            return TemporalInformation.builder().resolved(false).build();
        }

        LocalDateTime ref = referenceTime != null ? referenceTime : LocalDateTime.now();
        LocalDate today = ref.toLocalDate();
        String lower = text.toLowerCase(Locale.ROOT);

        if (lower.contains("after lunch") || lower.contains("afternoon")) {
            LocalDateTime start = LocalDateTime.of(today, LocalTime.of(13, 0));
            LocalDateTime end = LocalDateTime.of(today, LocalTime.of(17, 0));
            return TemporalInformation.builder()
                    .rawExpression(extractMatchedPhrase(lower, "after lunch", "afternoon"))
                    .startTime(start)
                    .endTime(end)
                    .resolutionType("TIME_OF_DAY")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("this morning") || lower.contains("morning")) {
            LocalDateTime start = LocalDateTime.of(today, LocalTime.of(6, 0));
            LocalDateTime end = LocalDateTime.of(today, LocalTime.of(12, 0));
            return TemporalInformation.builder()
                    .rawExpression(extractMatchedPhrase(lower, "this morning", "morning"))
                    .startTime(start)
                    .endTime(end)
                    .resolutionType("TIME_OF_DAY")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("tonight") || lower.contains("this evening") || lower.contains("evening")) {
            LocalDateTime start = LocalDateTime.of(today, LocalTime.of(18, 0));
            LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);
            return TemporalInformation.builder()
                    .rawExpression(extractMatchedPhrase(lower, "tonight", "this evening", "evening"))
                    .startTime(start)
                    .endTime(end)
                    .resolutionType("TIME_OF_DAY")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("tomorrow")) {
            LocalDate tomorrow = today.plusDays(1);
            return TemporalInformation.builder()
                    .rawExpression("tomorrow")
                    .startTime(LocalDateTime.of(tomorrow, LocalTime.MIN))
                    .endTime(LocalDateTime.of(tomorrow, LocalTime.MAX))
                    .resolutionType("DAY")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("yesterday")) {
            LocalDate yesterday = today.minusDays(1);
            return TemporalInformation.builder()
                    .rawExpression("yesterday")
                    .startTime(LocalDateTime.of(yesterday, LocalTime.MIN))
                    .endTime(LocalDateTime.of(yesterday, LocalTime.MAX))
                    .resolutionType("DAY")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("today")) {
            return TemporalInformation.builder()
                    .rawExpression("today")
                    .startTime(LocalDateTime.of(today, LocalTime.MIN))
                    .endTime(LocalDateTime.of(today, LocalTime.MAX))
                    .resolutionType("DAY")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("next week")) {
            LocalDate nextMon = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            LocalDate nextSun = nextMon.plusDays(6);
            return TemporalInformation.builder()
                    .rawExpression("next week")
                    .startTime(LocalDateTime.of(nextMon, LocalTime.MIN))
                    .endTime(LocalDateTime.of(nextSun, LocalTime.MAX))
                    .resolutionType("WEEK")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("this week")) {
            LocalDate mon = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sun = mon.plusDays(6);
            return TemporalInformation.builder()
                    .rawExpression("this week")
                    .startTime(LocalDateTime.of(mon, LocalTime.MIN))
                    .endTime(LocalDateTime.of(sun, LocalTime.MAX))
                    .resolutionType("WEEK")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("this weekend") || lower.contains("weekend")) {
            LocalDate sat = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            LocalDate sun = sat.plusDays(1);
            return TemporalInformation.builder()
                    .rawExpression(extractMatchedPhrase(lower, "this weekend", "weekend"))
                    .startTime(LocalDateTime.of(sat, LocalTime.MIN))
                    .endTime(LocalDateTime.of(sun, LocalTime.MAX))
                    .resolutionType("WEEKEND")
                    .resolved(true)
                    .build();
        }

        if (lower.contains("next month")) {
            LocalDate firstNextMonth = today.with(TemporalAdjusters.firstDayOfNextMonth());
            LocalDate lastNextMonth = firstNextMonth.with(TemporalAdjusters.lastDayOfMonth());
            return TemporalInformation.builder()
                    .rawExpression("next month")
                    .startTime(LocalDateTime.of(firstNextMonth, LocalTime.MIN))
                    .endTime(LocalDateTime.of(lastNextMonth, LocalTime.MAX))
                    .resolutionType("MONTH")
                    .resolved(true)
                    .build();
        }

        return TemporalInformation.builder().resolved(false).build();
    }

    private String extractMatchedPhrase(String text, String... candidates) {
        for (String c : candidates) {
            if (text.contains(c)) return c;
        }
        return candidates[0];
    }
}
