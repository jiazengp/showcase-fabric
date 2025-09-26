package com.showcase.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and formatting time durations
 */
public class TimeUtils {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s?)?$", Pattern.CASE_INSENSITIVE);

    /**
     * Parse a time string into seconds
     * Supports formats like: "1h", "30m", "1h30m", "90s", "1h30m45s", or just numbers
     * @param timeString the time string to parse
     * @return the duration in seconds
     * @throws IllegalArgumentException if the format is invalid
     */
    public static int parseTimeToSeconds(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new IllegalArgumentException("Time string cannot be null or empty");
        }

        timeString = timeString.trim().toLowerCase();

        // Handle plain numbers (backwards compatibility)
        try {
            return Integer.parseInt(timeString);
        } catch (NumberFormatException ignored) {
            // Not a plain number, continue with pattern matching
        }

        Matcher matcher = TIME_PATTERN.matcher(timeString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time format: " + timeString +
                    ". Use format like '1h', '30m', '1h30m', or plain seconds");
        }

        int totalSeconds = 0;

        // Hours
        String hours = matcher.group(1);
        if (hours != null) {
            totalSeconds += Integer.parseInt(hours) * 3600;
        }

        // Minutes
        String minutes = matcher.group(2);
        if (minutes != null) {
            totalSeconds += Integer.parseInt(minutes) * 60;
        }

        // Seconds
        String seconds = matcher.group(3);
        if (seconds != null) {
            totalSeconds += Integer.parseInt(seconds);
        }

        if (totalSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0");
        }

        return totalSeconds;
    }

    /**
     * Format seconds into a human-readable time string
     * @param seconds the duration in seconds
     * @return formatted time string like "1h 30m" or "45s"
     */
    public static String formatTime(int seconds) {
        if (seconds <= 0) {
            return "0s";
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append("h");
        }

        if (minutes > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(minutes).append("m");
        }

        if (remainingSeconds > 0 || result.length() == 0) {
            if (result.length() > 0) result.append(" ");
            result.append(remainingSeconds).append("s");
        }

        return result.toString();
    }
}