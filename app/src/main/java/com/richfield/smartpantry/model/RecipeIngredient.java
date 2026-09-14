package com.richfield.smartpantry.model;

import com.richfield.smartpantry.logic.IngredientNormalizer;

/**
 * One ingredient required by a recipe, with the amount that recipe needs.
 *
 * <p>Deliberately a separate table row rather than part of a delimited string on the recipe,
 * so the matcher can compare quantities per ingredient.
 */
public class RecipeIngredient {

    public static final long NO_ID = -1L;

    private long id;
    private long recipeId;
    private String name;
    private String nameKey;
    private double quantity;
    private String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this(NO_ID, NO_ID, name, quantity, unit);
    }

    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        setName(name);
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    /** Sets the display name and recalculates the matching key from it. */
    public void setName(String name) {
        this.name = name;
        this.nameKey = IngredientNormalizer.key(name);
    }

    /** The canonical form of {@link #getName()}, used for all matching and lookups. */
    public String getNameKey() {
        return nameKey;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return quantity + " " + unit + " " + name;
    }
}
