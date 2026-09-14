package com.richfield.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe: a name, its required ingredients, and the method.
 *
 * <p>{@code ingredients} is loaded separately from the recipe_ingredients table, so a recipe
 * read without its ingredients has an empty list rather than null.
 */
public class Recipe {

    public static final long NO_ID = -1L;

    private long id;
    private String name;

    /** Preparation steps, one step per line. */
    private String steps;

    private int servings;
    private int minutes;

    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String steps, int servings, int minutes) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.servings = servings;
        this.minutes = minutes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients == null ? new ArrayList<RecipeIngredient>() : ingredients;
    }

    /** Steps split into a list, for displaying as numbered lines on the detail screen. */
    public List<String> getStepList() {
        List<String> result = new ArrayList<>();
        if (steps == null) {
            return result;
        }
        for (String line : steps.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
