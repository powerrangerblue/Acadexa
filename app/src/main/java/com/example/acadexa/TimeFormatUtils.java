package com.example.acadexa;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TimeFormatUtils {
    private static final String[] INPUT_PATTERNS = {"h:mm a", "hh:mm a", "H:mm", "HH:mm"};

    private TimeFormatUtils() {
    }

    public static String formatForDisplay(String timeText) {
        int[] timeParts = parseToHourMinute(timeText);
        if (timeParts == null) {
            return timeText == null ? "" : timeText.trim();
        }

        return formatForDisplay(timeParts[0], timeParts[1]);
    }

    public static String formatForDisplay(int hourOfDay, int minute) {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.getDefault());
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return formatter.format(calendar.getTime());
    }

    public static int[] parseToHourMinute(String timeText) {
        if (timeText == null) {
            return null;
        }

        String trimmed = timeText.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        for (String pattern : INPUT_PATTERNS) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.getDefault());
            parser.setLenient(false);
            try {
                Date parsed = parser.parse(trimmed);
                if (parsed != null) {
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.setTime(parsed);
                    return new int[]{calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE)};
                }
            } catch (ParseException ignored) {
                // Try the next supported format.
            }
        }

        return null;
    }
}