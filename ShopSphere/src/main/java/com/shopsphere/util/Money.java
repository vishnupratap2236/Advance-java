package com.shopsphere.util;

import com.shopsphere.config.AppConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Formatting helpers for money and timestamps.
 *
 * <p>All timestamps are stored in UTC. They are rendered in the configured display
 * zone (see {@code SHOPSPHERE_TIMEZONE}), which keeps the stored data independent
 * of the server locale.
 */
public final class Money {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private Money() {
    }

    public static BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Formats an amount with the configured currency symbol, e.g. {@code ₹1,299.00}. */
    public static String format(BigDecimal value) {
        BigDecimal amount = scale(value);
        String symbol = AppConfig.get().currencySymbol();
        String plain = String.format("%,.2f", amount);
        return symbol + plain;
    }

    public static String formatDateTime(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return "";
        }
        return utcDateTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(AppConfig.get().displayZone())
                .format(DATE_TIME);
    }

    public static String formatDate(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return "";
        }
        return utcDateTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(AppConfig.get().displayZone())
                .format(DATE);
    }
}
