package com.richfield.smartpantry.logic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Converts a free-text ingredient name into a canonical "key" used for matching.
 *
 * <p>Every comparison between a pantry item and a recipe ingredient goes through this class, so
 * matching never depends on how the user happened to type a name. A recipe asking for "tomatoes"
 * has to be satisfied by a pantry holding "Tomato", and one asking for "ground beef" by a pantry
 * holding "beef mince".
 *
 * <p>The brief does not call for a full natural-language solution, and this is not one. It is a
 * deliberately small set of rules that covers the differences people actually type:
 *
 * <ol>
 *   <li>case, surrounding space and punctuation are removed</li>
 *   <li>leading descriptors are dropped, so "fresh chopped tomatoes" becomes "tomatoes"</li>
 *   <li>the head noun is made singular, so "tomatoes" becomes "tomato"</li>
 *   <li>known synonyms are folded together, so "aubergine" and "eggplant" agree</li>
 * </ol>
 *
 * <p>Note that the key is always derived, never stored and reused: {@code PantryItem} and
 * {@code RecipeIngredient} recalculate it whenever their name is set, including when they are
 * read back out of the database. Changing the rules here therefore changes matching immediately,
 * without needing the rows themselves to be rewritten.
 */
public final class IngredientNormalizer {

    /**
     * Words that describe an ingredient rather than identify it. Only stripped from the front,
     * and never all of them - whatever is left has to still name something.
     */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "dried", "frozen", "tinned", "canned", "raw", "cooked",
            "chopped", "diced", "sliced", "minced", "grated", "crushed", "ground",
            "whole", "large", "small", "medium", "ripe", "lean",
            "organic", "free-range", "boneless", "skinless",
            "plain", "unsalted", "salted", "extra", "virgin"));

    /** Words that end in "s" but are not plurals, or that no rule below handles correctly. */
    private static final Set<String> NEVER_SINGULARISED = new HashSet<>(Arrays.asList(
            "molasses", "watercress", "oats", "greens", "grits", "swiss", "hummus",
            "couscous", "asparagus"));

    /** Plurals the general rules would get wrong. */
    private static final Map<String, String> IRREGULAR_PLURALS = new HashMap<>();

    /**
     * Different names for the same ingredient, folded onto one of them.
     *
     * <p>Keys are written in the form they reach this map in - already singular and already
     * stripped of descriptors - and no value is itself a key, so a lookup never needs repeating.
     */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR_PLURALS.put("chillies", "chilli");
        IRREGULAR_PLURALS.put("chilies", "chili");
        IRREGULAR_PLURALS.put("leaves", "leaf");
        IRREGULAR_PLURALS.put("loaves", "loaf");

        SYNONYMS.put("beef mince", "beef");
        SYNONYMS.put("mince", "beef");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("brinjal", "eggplant");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("coriander", "cilantro");
        SYNONYMS.put("spring onion", "green onion");
        SYNONYMS.put("rocket", "arugula");
        SYNONYMS.put("soya sauce", "soy sauce");
        SYNONYMS.put("maize meal", "cornmeal");
        SYNONYMS.put("mealie meal", "cornmeal");
        SYNONYMS.put("bicarbonate of soda", "baking soda");
    }

    private IngredientNormalizer() {
        // Utility class - not meant to be instantiated.
    }

    /**
     * Returns the canonical key for an ingredient name.
     *
     * @param rawName the name as typed by the user, or as written in the recipe data
     * @return the key to match on; empty string if {@code rawName} is null or has no letters
     */
    @NonNull
    public static String key(@Nullable String rawName) {
        if (rawName == null) {
            return "";
        }

        String cleaned = rawName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 -]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) {
            return "";
        }

        cleaned = stripLeadingDescriptors(cleaned);
        cleaned = singulariseHeadNoun(cleaned);

        String synonym = SYNONYMS.get(cleaned);
        return synonym != null ? synonym : cleaned;
    }

    /**
     * Drops descriptive words from the front of the name.
     *
     * <p>Stops before the final word whatever happens, so an ingredient that is only descriptors
     * still normalises to something rather than to an empty key.
     */
    private static String stripLeadingDescriptors(@NonNull String phrase) {
        String[] words = phrase.split(" ");
        int first = 0;
        while (first < words.length - 1 && DESCRIPTORS.contains(words[first])) {
            first++;
        }

        StringBuilder result = new StringBuilder(words[first]);
        for (int i = first + 1; i < words.length; i++) {
            result.append(' ').append(words[i]);
        }
        return result.toString();
    }

    /**
     * Makes the last word of the phrase singular.
     *
     * <p>Only the last word, because that is the noun being counted: "chicken breasts" is many
     * breasts, not many chickens, so it should normalise to "chicken breast".
     */
    private static String singulariseHeadNoun(@NonNull String phrase) {
        int lastSpace = phrase.lastIndexOf(' ');
        if (lastSpace == -1) {
            return singularise(phrase);
        }
        return phrase.substring(0, lastSpace + 1) + singularise(phrase.substring(lastSpace + 1));
    }

    private static String singularise(@NonNull String word) {
        String irregular = IRREGULAR_PLURALS.get(word);
        if (irregular != null) {
            return irregular;
        }

        // Too short to have a plural ending worth trimming, or not a plural at all.
        if (word.length() < 4 || !word.endsWith("s") || NEVER_SINGULARISED.contains(word)) {
            return word;
        }

        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("ves")) {
            return word.substring(0, word.length() - 3) + "f";
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("sses") || word.endsWith("ches")
                || word.endsWith("shes") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);
        }

        // Words like "hummus" or "cress" end in s without being plural.
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;
        }

        return word.substring(0, word.length() - 1);
    }
}
