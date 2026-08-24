package com.siva.shopsphere.products.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {
    private SlugUtils() {
    }

    public static String toSlug(String input) {
        if (input == null) {
            return null;
        }
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "")
            .replaceAll("-{2,}", "-");
        return normalized;
    }
}
