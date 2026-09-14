package com.richfield.smartpantry.logic;

import java.util.Locale;

/**
 * Converts a free-text ingredient name into a canonical "key" used for matching.
 *
 * <p>Every comparison between a pantry item and a recipe ingredient goes through this class,
 * so that matching never depends on how the user happened to type a name. Keeping the rule in
 * one place means the pantry table, the recipe table and the matcher can never disagree about
 * what counts as the same ingredient.
 *
 * <p>This is currently a basic implementation (case and whitespace only). Plural handling,
 * descriptor stripping and synonyms are added later, once the matching logic is written.
 */
public final class IngredientNormalizer {

    private IngredientNormalizer() {
        // Utility class - not meant to be instantiated.
    }

    /**
     * Returns the canonical key for an ingredient name.
     *
     * @param rawName the name as typed by the user, or read from the recipe data
     * @return a lowercase, single-spaced, trimmed key; empty string if {@code rawName} is null
     */
    public static String key(String rawName) {
        if (rawName == null) {
            return "";
        }
        return rawName.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }
}
