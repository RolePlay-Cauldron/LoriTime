package com.jannik_kuehn.common.service;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Validation rules for tracking tag keys and values.
 */
public final class TrackingTagRules {
    /**
     * Maximum length of a tracking tag key.
     */
    public static final int MAX_KEY_LENGTH = 100;

    /**
     * Maximum length of a tracking tag value.
     */
    public static final int MAX_VALUE_LENGTH = 191;

    /**
     * Maximum number of tags per player.
     */
    public static final int MAX_TAGS = 32;

    /**
     * Pattern of a namespaced tracking tag key.
     */
    private static final Pattern KEY_PATTERN = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private TrackingTagRules() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Tells whether a key is a valid namespaced tag key.
     *
     * @param key the key to check
     * @return {@code true} if valid
     */
    public static boolean isValidKey(final String key) {
        return key != null && key.length() <= MAX_KEY_LENGTH && KEY_PATTERN.matcher(key).matches();
    }

    /**
     * Tells whether a value is a valid tag value.
     *
     * @param value the value to check
     * @return {@code true} if valid
     */
    public static boolean isValidValue(final String value) {
        return value != null && !value.isBlank() && value.length() <= MAX_VALUE_LENGTH;
    }

    /**
     * Validates a tag key.
     *
     * @param key the key to validate
     * @throws IllegalArgumentException if the key is invalid
     */
    public static void requireValidKey(final String key) {
        Objects.requireNonNull(key, "key");
        if (!isValidKey(key)) {
            throw new IllegalArgumentException("key must be a namespaced lower case key such as "
                    + "'plugin:name' with at most " + MAX_KEY_LENGTH + " characters");
        }
    }

    /**
     * Validates a tag value.
     *
     * @param value the value to validate
     * @throws IllegalArgumentException if the value is invalid
     */
    public static void requireValidValue(final String value) {
        Objects.requireNonNull(value, "value");
        if (!isValidValue(value)) {
            throw new IllegalArgumentException("value must not be blank and at most "
                    + MAX_VALUE_LENGTH + " characters");
        }
    }
}
