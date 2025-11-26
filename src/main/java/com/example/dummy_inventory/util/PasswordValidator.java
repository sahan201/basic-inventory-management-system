package com.example.dummy_inventory.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Password validation utility class
 * Enforces password strength requirements
 */
public class PasswordValidator {

    // Password requirements
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    // Regex patterns for password validation
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    /**
     * Validates password strength
     *
     * @param password The password to validate
     * @return ValidationResult containing isValid and error messages
     */
    public static ValidationResult validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        // Null or empty check
        if (password == null || password.isEmpty()) {
            errors.add("Password is required");
            return new ValidationResult(false, errors);
        }

        // Length check
        if (password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters");
        }

        if (password.length() > MAX_LENGTH) {
            errors.add("Password must not exceed " + MAX_LENGTH + " characters");
        }

        // Uppercase check
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter");
        }

        // Lowercase check
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter");
        }

        // Digit check
        if (!DIGIT_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one number");
        }

        // Optional: Special character check (uncomment to require)
        // if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
        //     errors.add("Password must contain at least one special character");
        // }

        // Check for common weak passwords
        if (isCommonPassword(password)) {
            errors.add("Password is too common. Please choose a stronger password");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Quick password strength check (returns true if password meets minimum requirements)
     *
     * @param password The password to check
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return validatePassword(password).isValid();
    }

    /**
     * Calculates password strength score (0-100)
     *
     * @param password The password to evaluate
     * @return Strength score from 0 (weakest) to 100 (strongest)
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score (up to 30 points)
        if (password.length() >= MIN_LENGTH) {
            score += Math.min(30, password.length() * 2);
        }

        // Uppercase letters (10 points)
        if (UPPERCASE_PATTERN.matcher(password).find()) {
            score += 10;
        }

        // Lowercase letters (10 points)
        if (LOWERCASE_PATTERN.matcher(password).find()) {
            score += 10;
        }

        // Numbers (15 points)
        if (DIGIT_PATTERN.matcher(password).find()) {
            score += 15;
        }

        // Special characters (20 points)
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            score += 20;
        }

        // Variety bonus (15 points for using all character types)
        if (UPPERCASE_PATTERN.matcher(password).find() &&
            LOWERCASE_PATTERN.matcher(password).find() &&
            DIGIT_PATTERN.matcher(password).find() &&
            SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            score += 15;
        }

        // Penalize common passwords
        if (isCommonPassword(password)) {
            score = Math.max(0, score - 40);
        }

        return Math.min(100, score);
    }

    /**
     * Gets password strength label based on score
     *
     * @param password The password to evaluate
     * @return Strength label: "Weak", "Fair", "Good", "Strong", or "Very Strong"
     */
    public static String getPasswordStrengthLabel(String password) {
        int strength = calculatePasswordStrength(password);

        if (strength < 30) return "Weak";
        if (strength < 50) return "Fair";
        if (strength < 70) return "Good";
        if (strength < 90) return "Strong";
        return "Very Strong";
    }

    /**
     * Checks if password is commonly used (weak)
     *
     * @param password The password to check
     * @return true if password is common, false otherwise
     */
    private static boolean isCommonPassword(String password) {
        // List of commonly used weak passwords
        String[] commonPasswords = {
            "password", "password123", "123456", "12345678", "qwerty",
            "abc123", "monkey", "1234567", "letmein", "trustno1",
            "dragon", "baseball", "111111", "iloveyou", "master",
            "sunshine", "ashley", "bailey", "passw0rd", "shadow",
            "123123", "654321", "superman", "qazwsx", "michael",
            "football", "admin", "welcome", "login", "test"
        };

        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.equals(common)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Result class for password validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join("\n", errors);
        }
    }
}
