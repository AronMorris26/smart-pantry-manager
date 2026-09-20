package com.richfield.smartpantry.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.richfield.smartpantry.data.PantryContract.RecipeIngredients;
import com.richfield.smartpantry.data.PantryContract.Recipes;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads recipes and the ingredients they require.
 *
 * <p>Read-only: the recipe collection is seeded when the database is created and the user never
 * edits it, so there is no insert or update here. Writing the seed data is
 * {@link PantryDbHelper}'s job, because it has to happen on the database handed to onCreate.
 */
public class RecipeDao {

    private final PantryDbHelper helper;

    public RecipeDao(@NonNull Context context) {
        this.helper = PantryDbHelper.getInstance(context);
    }

    /**
     * Returns every recipe with its ingredient list attached, sorted by name.
     *
     * <p>Two queries in total, not one per recipe. Reading the ingredients for each recipe in a
     * loop would issue nineteen queries for eighteen recipes, and the suggestions screen reloads
     * this on every visit. Instead the ingredients are read in one pass and matched to their
     * recipe through a map.
     */
    @NonNull
    public List<Recipe> getAll() {
        Map<Long, Recipe> recipesById = readAllRecipes();
        attachIngredients(recipesById);
        return new ArrayList<>(recipesById.values());
    }

    /** Returns one recipe with its ingredients, or null if the id is unknown. */
    @Nullable
    public Recipe findById(long recipeId) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Recipe recipe = null;
        try (Cursor cursor = db.query(
                Recipes.TABLE_NAME,
                null,
                Recipes._ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                recipe = recipeFromCursor(cursor);
            }
        }

        if (recipe == null) {
            return null;
        }

        try (Cursor cursor = db.query(
                RecipeIngredients.TABLE_NAME,
                null,
                RecipeIngredients.COLUMN_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null, null,
                RecipeIngredients._ID + " ASC")) {
            while (cursor.moveToNext()) {
                recipe.getIngredients().add(ingredientFromCursor(cursor));
            }
        }
        return recipe;
    }

    /** Number of seeded recipes. Used to check the seed actually ran. */
    public int count() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + Recipes.TABLE_NAME, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /** Reads every recipe into a map keyed by id, preserving the sort order. */
    private Map<Long, Recipe> readAllRecipes() {
        Map<Long, Recipe> recipesById = new LinkedHashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.query(
                Recipes.TABLE_NAME,
                null, null, null, null, null,
                Recipes.COLUMN_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                Recipe recipe = recipeFromCursor(cursor);
                recipesById.put(recipe.getId(), recipe);
            }
        }
        return recipesById;
    }

    /** Reads every ingredient row once and files each one under the recipe it belongs to. */
    private void attachIngredients(@NonNull Map<Long, Recipe> recipesById) {
        if (recipesById.isEmpty()) {
            return;
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(
                RecipeIngredients.TABLE_NAME,
                null, null, null, null, null,
                RecipeIngredients.COLUMN_RECIPE_ID + " ASC, " + RecipeIngredients._ID + " ASC")) {

            int recipeIdIndex = cursor.getColumnIndexOrThrow(RecipeIngredients.COLUMN_RECIPE_ID);
            while (cursor.moveToNext()) {
                Recipe owner = recipesById.get(cursor.getLong(recipeIdIndex));
                if (owner != null) {
                    owner.getIngredients().add(ingredientFromCursor(cursor));
                }
            }
        }
    }

    private Recipe recipeFromCursor(@NonNull Cursor cursor) {
        return new Recipe(
                cursor.getLong(cursor.getColumnIndexOrThrow(Recipes._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(Recipes.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(Recipes.COLUMN_STEPS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Recipes.COLUMN_SERVINGS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Recipes.COLUMN_MINUTES)));
    }

    private RecipeIngredient ingredientFromCursor(@NonNull Cursor cursor) {
        return new RecipeIngredient(
                cursor.getLong(cursor.getColumnIndexOrThrow(RecipeIngredients._ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(RecipeIngredients.COLUMN_RECIPE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(RecipeIngredients.COLUMN_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(RecipeIngredients.COLUMN_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(RecipeIngredients.COLUMN_UNIT)));
    }
}
