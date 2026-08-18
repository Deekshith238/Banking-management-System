package banking.util;

import java.util.regex.Pattern;

/**
 * Utility class to validate CLI input data.
 */
public class InputValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");

    /**
     * Validates email format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates phone number format (7 to 15 digits, optional + prefix).
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates non-empty string input.
     */
    public static boolean isNonEmptyString(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Validates positive monetary amount.
     */
    public static boolean isPositiveAmount(double amount) {
        return amount > 0.0;
    }

    /**
     * Validates account type input ("SAVINGS" or "CURRENT").
     */
    public static boolean isValidAccountType(String accountType) {
        if (accountType == null) return false;
        String typeUpper = accountType.trim().toUpperCase();
        return typeUpper.equals("SAVINGS") || typeUpper.equals("CURRENT");
    }
}
