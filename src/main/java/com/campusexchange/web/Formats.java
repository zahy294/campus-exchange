package com.campusexchange.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * View helpers exposed to Thymeleaf as {@code @fmt}, e.g. {@code ${@fmt.inr(book.price)}}.
 */
@Component("fmt")
public class Formats {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    /**
     * Rupees with Indian digit grouping: 1500 -> "₹1,500", 125000.5 -> "₹1,25,000.50".
     * (java.text.DecimalFormat can't do the 3-then-2 grouping, so it's done by hand.)
     */
    public String inr(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        BigDecimal value = amount.setScale(2, RoundingMode.HALF_UP);
        boolean negative = value.signum() < 0;
        String plain = value.abs().toPlainString();
        String whole = plain.substring(0, plain.indexOf('.'));
        String paise = plain.substring(plain.indexOf('.') + 1);

        StringBuilder grouped = new StringBuilder();
        int len = whole.length();
        if (len <= 3) {
            grouped.append(whole);
        } else {
            String head = whole.substring(0, len - 3);
            String tail = whole.substring(len - 3);
            for (int i = 0; i < head.length(); i++) {
                if (i > 0 && (head.length() - i) % 2 == 0) {
                    grouped.append(',');
                }
                grouped.append(head.charAt(i));
            }
            grouped.append(',').append(tail);
        }
        String result = "₹" + grouped + ("00".equals(paise) ? "" : "." + paise);
        return negative ? "-" + result : result;
    }

    public String date(LocalDateTime time) {
        return time == null ? "" : DATE.format(time);
    }

    /** "today", "yesterday", "5 days ago", or the date once it's older than four weeks. */
    public String ago(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        long days = Duration.between(time.toLocalDate().atStartOfDay(),
                LocalDateTime.now().toLocalDate().atStartOfDay()).toDays();
        if (days <= 0) {
            return "today";
        }
        if (days == 1) {
            return "yesterday";
        }
        if (days < 28) {
            return days + " days ago";
        }
        return "on " + date(time);
    }

    /** Initials for the seller avatar: "Aarav Patel" -> "AP". */
    public String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.ROOT);
    }
}
