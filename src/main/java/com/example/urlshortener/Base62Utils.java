package com.example.urlshortener;

public class Base62Utils {

    // This is the string containing all possible characters for our Base62 encoding.
    // 0-9 (10), a-z (26), A-Z (26) = 62 characters total.
    private static final String BASE62_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARACTERS.length(); // This will be 62

    // This method takes a number (long) and converts it into a Base62 string.
    public static String toBase62(long number) {
        // If the number is 0, we just return "0" (the first character in our BASE62_CHARACTERS string).
        if (number == 0) {
            return String.valueOf(BASE62_CHARACTERS.charAt(0));
        }

        // StringBuilder is efficient for building strings piece by piece.
        StringBuilder sb = new StringBuilder();

        // We keep dividing the number by 62 and taking the remainder
        // until the number becomes 0.
        while (number > 0) {
            // The remainder gives us the index for our BASE62_CHARACTERS string.
            sb.append(BASE62_CHARACTERS.charAt((int) (number % BASE)));
            // Divide the number by 62 for the next iteration.
            number /= BASE;
        }

        // The characters were added in reverse order (least significant digit first),
        // so we need to reverse the string before returning it.
        return sb.reverse().toString();
    }

    // This method takes a Base62 string and converts it back to a number (long).
    // We might not use this one immediately, but it's good to have.
    public static long toBase10(String base62String) {
        long number = 0;
        long power = 1; // Represents 62^0, 62^1, 62^2, ...

        // We iterate through the string from right to left.
        for (int i = base62String.length() - 1; i >= 0; i--) {
            char character = base62String.charAt(i);
            int digitValue = BASE62_CHARACTERS.indexOf(character);

            // If the character is not in our BASE62_CHARACTERS string, it's an error.
            if (digitValue == -1) {
                throw new IllegalArgumentException("Invalid character in Base62 string: " + character);
            }

            number += (long)digitValue * power;
            power *= BASE;
        }
        return number;
    }
}