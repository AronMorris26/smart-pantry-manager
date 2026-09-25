package com.richfield.smartpantry.model;

import androidx.annotation.NonNull;

/** A recipe the pantry covers except for one ingredient. */
public class AlmostThereRecipe {

    private final Recipe recipe;
    private final String missingIngredient;

    public AlmostThereRecipe(@NonNull Recipe recipe, @NonNull String missingIngredient) {
        this.recipe = recipe;
        this.missingIngredient = missingIngredient;
    }

    @NonNull
    public Recipe getRecipe() {
        return recipe;
    }

    @NonNull
    public String getMissingIngredient() {
        return missingIngredient;
    }
}
