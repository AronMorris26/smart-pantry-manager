package com.richfield.smartpantry.data;

import androidx.annotation.NonNull;

import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The recipe collection the app ships with, written into the database on first run.
 *
 * <p>Held as plain Java rather than a JSON or CSV asset: there is no parsing step that can fail
 * at runtime, and the compiler catches a malformed entry rather than the user finding it.
 *
 * <p>Units are drawn from the same short list the Add Ingredient screen offers, so the matcher
 * can convert between a recipe's amount and a pantry amount without guessing what a unit means.
 * Ingredient names are written the way a recipe would naturally phrase them; making those match
 * whatever the user typed is the normaliser's job, not the seed data's.
 */
public final class SeedData {

    private SeedData() {
        // Utility class - not meant to be instantiated.
    }

    /** Builds the full starter collection. */
    @NonNull
    public static List<Recipe> getRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        recipes.add(recipe("Scrambled Eggs", 2, 10,
                "Beat the eggs with a pinch of salt.\n"
                        + "Melt the butter in a pan over low heat.\n"
                        + "Add the eggs and stir gently until just set.",
                ing("Eggs", 3, "item"),
                ing("Butter", 15, "g"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Boiled Rice", 4, 20,
                "Rinse the rice until the water runs clear.\n"
                        + "Add twice its volume of salted water and bring to the boil.\n"
                        + "Cover and simmer for 12 minutes, then rest off the heat.",
                ing("Rice", 200, "g"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Garlic Pasta", 2, 20,
                "Boil the pasta in salted water until al dente.\n"
                        + "Warm the sliced garlic gently in the olive oil.\n"
                        + "Toss the drained pasta through the oil and finish with cheese.",
                ing("Pasta", 200, "g"),
                ing("Garlic", 3, "clove"),
                ing("Olive oil", 30, "ml"),
                ing("Salt", 5, "g"),
                ing("Cheddar cheese", 50, "g")));

        recipes.add(recipe("Tomato Pasta", 4, 30,
                "Soften the chopped onion and garlic in the olive oil.\n"
                        + "Add the chopped tomatoes and simmer for 20 minutes.\n"
                        + "Season, then stir through the cooked pasta.",
                ing("Pasta", 250, "g"),
                ing("Tomatoes", 4, "item"),
                ing("Garlic", 2, "clove"),
                ing("Onion", 1, "item"),
                ing("Olive oil", 2, "tbsp"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Cheese Omelette", 1, 10,
                "Beat the eggs with salt.\n"
                        + "Pour into a buttered pan and cook until almost set.\n"
                        + "Scatter over the cheese, fold, and serve.",
                ing("Eggs", 3, "item"),
                ing("Cheddar cheese", 60, "g"),
                ing("Butter", 15, "g"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Pancakes", 4, 25,
                "Whisk the flour, sugar and salt together.\n"
                        + "Beat in the eggs and milk until smooth, then rest for 10 minutes.\n"
                        + "Fry spoonfuls in butter until golden on both sides.",
                ing("Flour", 200, "g"),
                ing("Milk", 300, "ml"),
                ing("Eggs", 2, "item"),
                ing("Sugar", 30, "g"),
                ing("Butter", 20, "g"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Egg Fried Rice", 3, 20,
                "Fry the chopped onion and garlic in the oil until soft.\n"
                        + "Push aside, scramble the eggs in the same pan.\n"
                        + "Add cold cooked rice and soy sauce, and toss over high heat.",
                ing("Rice", 300, "g"),
                ing("Eggs", 2, "item"),
                ing("Onion", 1, "item"),
                ing("Garlic", 2, "clove"),
                ing("Olive oil", 2, "tbsp"),
                ing("Salt", 5, "g"),
                ing("Soy sauce", 30, "ml")));

        recipes.add(recipe("Pan-Roasted Chicken", 2, 30,
                "Season the chicken well on both sides.\n"
                        + "Sear in hot oil with the crushed garlic until golden.\n"
                        + "Lower the heat and cook through, basting as it goes.",
                ing("Chicken breast", 400, "g"),
                ing("Olive oil", 2, "tbsp"),
                ing("Garlic", 2, "clove"),
                ing("Salt", 5, "g"),
                ing("Black pepper", 2, "g")));

        recipes.add(recipe("Chicken and Rice", 4, 45,
                "Brown the chicken pieces in the oil and set aside.\n"
                        + "Soften the onion and garlic, then stir in the rice.\n"
                        + "Return the chicken, pour in the stock, cover and simmer until absorbed.",
                ing("Chicken breast", 300, "g"),
                ing("Rice", 250, "g"),
                ing("Onion", 1, "item"),
                ing("Garlic", 2, "clove"),
                ing("Olive oil", 2, "tbsp"),
                ing("Salt", 5, "g"),
                ing("Chicken stock", 500, "ml")));

        recipes.add(recipe("Tomato Soup", 4, 40,
                "Sweat the onion and garlic in the olive oil.\n"
                        + "Add the tomatoes and simmer for half an hour.\n"
                        + "Blend until smooth, then stir in the cream and season.",
                ing("Tomatoes", 4, "item"),
                ing("Onion", 1, "item"),
                ing("Garlic", 2, "clove"),
                ing("Olive oil", 2, "tbsp"),
                ing("Salt", 5, "g"),
                ing("Cream", 100, "ml")));

        recipes.add(recipe("Macaroni Cheese", 4, 35,
                "Boil the pasta until just short of done.\n"
                        + "Melt the butter, stir in the flour, then whisk in the milk to a sauce.\n"
                        + "Add most of the cheese, fold through the pasta, top and bake.",
                ing("Pasta", 250, "g"),
                ing("Cheddar cheese", 200, "g"),
                ing("Milk", 400, "ml"),
                ing("Butter", 30, "g"),
                ing("Flour", 30, "g"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("French Toast", 2, 15,
                "Beat the eggs with the milk, sugar and salt.\n"
                        + "Soak each slice of bread until saturated.\n"
                        + "Fry in butter until golden on both sides.",
                ing("Bread", 4, "slice"),
                ing("Eggs", 2, "item"),
                ing("Milk", 100, "ml"),
                ing("Sugar", 20, "g"),
                ing("Butter", 20, "g"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Shortbread Biscuits", 12, 40,
                "Cream the butter and sugar together.\n"
                        + "Work in the flour and salt until it comes together as a dough.\n"
                        + "Roll, cut, and bake at 160C for 20 minutes until pale gold.",
                ing("Flour", 250, "g"),
                ing("Butter", 170, "g"),
                ing("Sugar", 80, "g"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Cheese Toastie", 1, 10,
                "Butter the outside of both slices of bread.\n"
                        + "Fill with the grated cheese.\n"
                        + "Fry gently until the outside is crisp and the middle has melted.",
                ing("Bread", 2, "slice"),
                ing("Cheddar cheese", 60, "g"),
                ing("Butter", 20, "g")));

        recipes.add(recipe("Spanish Omelette", 4, 35,
                "Fry the sliced potatoes and onion slowly in plenty of oil until tender.\n"
                        + "Beat the eggs with salt and combine with the drained potatoes.\n"
                        + "Cook gently in the pan, then flip to set the other side.",
                ing("Potatoes", 3, "item"),
                ing("Eggs", 4, "item"),
                ing("Onion", 1, "item"),
                ing("Olive oil", 3, "tbsp"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Creamy Garlic Chicken", 2, 30,
                "Season and sear the chicken in the butter until browned.\n"
                        + "Soften the onion and garlic in the same pan.\n"
                        + "Pour in the cream, reduce to a sauce, and return the chicken.",
                ing("Chicken breast", 400, "g"),
                ing("Cream", 200, "ml"),
                ing("Garlic", 4, "clove"),
                ing("Butter", 20, "g"),
                ing("Onion", 1, "item"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Rice Pudding", 4, 50,
                "Stir the rice, milk and sugar together in an oven dish.\n"
                        + "Dot the butter over the surface and add a pinch of salt.\n"
                        + "Bake slowly at 150C for 90 minutes until thick and skinned.",
                ing("Rice", 150, "g"),
                ing("Milk", 700, "ml"),
                ing("Sugar", 60, "g"),
                ing("Butter", 10, "g"),
                ing("Salt", 5, "g")));

        recipes.add(recipe("Simple Flatbread", 4, 25,
                "Mix the flour and salt, then work in the milk and oil.\n"
                        + "Knead briefly and rest the dough for 15 minutes.\n"
                        + "Roll thin and cook in a dry hot pan until blistered.",
                ing("Flour", 300, "g"),
                ing("Olive oil", 2, "tbsp"),
                ing("Salt", 5, "g"),
                ing("Milk", 150, "ml")));

        recipes.add(recipe("Beef Lasagne", 6, 75,
                "Brown the mince with the onion and garlic, add tomatoes and simmer.\n"
                        + "Make a white sauce from the butter, flour and milk.\n"
                        + "Layer pasta, ragu and sauce, top with cheese and bake for 40 minutes.",
                ing("Beef mince", 500, "g"),
                ing("Pasta", 250, "g"),
                ing("Tomatoes", 6, "item"),
                ing("Onion", 1, "item"),
                ing("Garlic", 2, "clove"),
                ing("Milk", 400, "ml"),
                ing("Flour", 30, "g"),
                ing("Butter", 30, "g"),
                ing("Cheddar cheese", 150, "g")));

        recipes.add(recipe("Vegetable Curry", 4, 45,
                "Fry the onion, garlic and curry powder until fragrant.\n"
                        + "Add the diced potatoes and carrots with the coconut milk.\n"
                        + "Simmer until tender and serve over rice.",
                ing("Potatoes", 3, "item"),
                ing("Carrots", 2, "item"),
                ing("Onion", 1, "item"),
                ing("Garlic", 2, "clove"),
                ing("Curry powder", 15, "g"),
                ing("Coconut milk", 400, "ml"),
                ing("Rice", 200, "g"),
                ing("Salt", 5, "g")));

        return recipes;
    }

    private static Recipe recipe(String name, int servings, int minutes, String steps,
                                 RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(Recipe.NO_ID, name, steps, servings, minutes);
        recipe.setIngredients(new ArrayList<>(Arrays.asList(ingredients)));
        return recipe;
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }
}
