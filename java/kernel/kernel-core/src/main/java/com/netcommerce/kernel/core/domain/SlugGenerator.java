package com.netcommerce.kernel.core.domain;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

import com.netcommerce.kernel.core.ArgumentException;
import com.netcommerce.kernel.core.ArgumentNullException;

/** URL-friendly slugs, character for character as the .NET SlugGenerator makes them. */
public final class SlugGenerator {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9-]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-+");

    private SlugGenerator() {
    }

    public static String generate(String text) {
        if (text == null) {
            throw new ArgumentNullException("text");
        }
        if (text.isBlank()) {
            throw new ArgumentException("The value cannot be an empty string or composed entirely of whitespace.", "text");
        }
        String slug = text.toLowerCase(Locale.ROOT)
                .replace("&", "and")
                .replace("'", "")
                .replace("\"", "")
                .replace(" ", "-")
                .replace("_", "-");
        slug = NON_ALPHANUMERIC.matcher(slug).replaceAll("");
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");
        return trimHyphens(slug);
    }

    /** A slug the given ones do not already hold: "-1", "-2", ... appended as needed. */
    public static String generateUnique(String text, Collection<String> existingSlugs) {
        String baseSlug = generate(text);
        if (!existingSlugs.contains(baseSlug)) {
            return baseSlug;
        }
        int counter = 1;
        String unique;
        do {
            unique = baseSlug + "-" + counter++;
        } while (existingSlugs.contains(unique));
        return unique;
    }

    private static String trimHyphens(String slug) {
        int start = 0;
        int end = slug.length();
        while (start < end && slug.charAt(start) == '-') {
            start++;
        }
        while (end > start && slug.charAt(end - 1) == '-') {
            end--;
        }
        return slug.substring(start, end);
    }
}
