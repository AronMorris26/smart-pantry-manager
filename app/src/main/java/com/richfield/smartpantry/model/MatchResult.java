package com.richfield.smartpantry.model;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * The outcome of testing one recipe against the pantry.
 *
 * <p>Carries more than a yes or no: the names of the ingredients that fell short are kept, which
 * is what lets the app tell a user they are one ingredient away from a recipe rather than only
 * that they cannot make it.
 */
public class MatchResult {

    private final List<String> missingIngredients;

    public MatchResult(@NonNull List<String> missingIngredients) {
        this.missingIngredients = Collections.unmodifiableList(missingIngredients);
    }

    /** True when every required ingredient is present in at least the required quantity. */
    public boolean isSatisfied() {
        return missingIngredients.isEmpty();
    }

    /**
     * Display names of the ingredients the pantry could not cover, either because they are
     * absent or because there is not enough of them.
     */
    @NonNull
    public List<String> getMissingIngredients() {
        return missingIngredients;
    }

    /** How many ingredients fell short. One means the recipe is a single item away. */
    public int getMissingCount() {
        return missingIngredients.size();
    }
}
