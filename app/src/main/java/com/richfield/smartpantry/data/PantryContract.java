package com.richfield.smartpantry.data;

import android.provider.BaseColumns;

/**
 * Single source of truth for the database table and column names.
 *
 * <p>Every table name and column name in the app is referenced through this class rather than
 * as a string literal, so a rename cannot silently break one query while leaving others working.
 *
 * <p>Each inner class implements {@link BaseColumns}, which supplies the standard {@code _id}
 * primary key column name that Android's adapters and cursors expect.
 */
public final class PantryContract {

    private PantryContract() {
        // Contract class - not meant to be instantiated.
    }

    /** Ingredients the user currently has at home. */
    public static final class PantryItems implements BaseColumns {
        public static final String TABLE_NAME = "pantry_items";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NAME_KEY = "name_key";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_EXPIRY_DATE = "expiry_date";

        private PantryItems() {
        }
    }

    /** The seeded recipe collection. */
    public static final class Recipes implements BaseColumns {
        public static final String TABLE_NAME = "recipes";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_STEPS = "steps";
        public static final String COLUMN_SERVINGS = "servings";
        public static final String COLUMN_MINUTES = "minutes";

        private Recipes() {
        }
    }

    /** The ingredients each recipe requires, one row per ingredient. */
    public static final class RecipeIngredients implements BaseColumns {
        public static final String TABLE_NAME = "recipe_ingredients";
        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NAME_KEY = "name_key";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_UNIT = "unit";

        private RecipeIngredients() {
        }
    }
}
