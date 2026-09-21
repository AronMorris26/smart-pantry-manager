package com.richfield.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.richfield.smartpantry.data.PantryContract.PantryItems;
import com.richfield.smartpantry.data.PantryContract.RecipeIngredients;
import com.richfield.smartpantry.data.PantryContract.Recipes;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

/**
 * Creates and upgrades the local SQLite database.
 *
 * <p>SQLite was chosen over a cloud database because a pantry is inherently local, single-user
 * data: there is nothing to sync and no second user to share with, and the app stays useful with
 * no signal. {@link SQLiteOpenHelper} handles opening the database file, calling
 * {@link #onCreate} the first time it is created, and {@link #onUpgrade} when the schema version
 * changes.
 *
 * <p>A single shared instance is used across the app. Multiple helper instances would each hold
 * their own connection to the same file, which risks lock contention on writes.
 */
public class PantryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "smart_pantry.db";

    /**
     * Bump this whenever the schema, the seeded recipe collection, or the normalisation rules
     * change, so onUpgrade runs on existing installs. Editing SeedData without bumping this
     * leaves old devices on the old recipes, because onCreate only ever runs once.
     *
     * <p>Version 3 exists because IngredientNormalizer's rules changed. Objects recalculate their
     * key whenever their name is set, so matching was unaffected - but the name_key column was
     * written by the old rules, and both the duplicate check and the UNIQUE constraint query it.
     * A pantry row stored as "eggs" would no longer be found by a search for the new key "egg",
     * letting the same ingredient be added twice.
     */
    public static final int DATABASE_VERSION = 3;

    private static PantryDbHelper instance;

    /**
     * Ingredients the user has. name_key is UNIQUE so an ingredient appears at most once:
     * adding an ingredient that is already present updates the existing row rather than
     * creating a duplicate, which keeps the matching logic from having to sum across rows.
     */
    private static final String SQL_CREATE_PANTRY_ITEMS =
            "CREATE TABLE " + PantryItems.TABLE_NAME + " (" +
                    PantryItems._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PantryItems.COLUMN_NAME + " TEXT NOT NULL, " +
                    PantryItems.COLUMN_NAME_KEY + " TEXT NOT NULL UNIQUE, " +
                    PantryItems.COLUMN_QUANTITY + " REAL NOT NULL, " +
                    PantryItems.COLUMN_UNIT + " TEXT NOT NULL, " +
                    PantryItems.COLUMN_EXPIRY_DATE + " INTEGER" +
                    ")";

    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE " + Recipes.TABLE_NAME + " (" +
                    Recipes._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Recipes.COLUMN_NAME + " TEXT NOT NULL, " +
                    Recipes.COLUMN_STEPS + " TEXT NOT NULL, " +
                    Recipes.COLUMN_SERVINGS + " INTEGER NOT NULL DEFAULT 1, " +
                    Recipes.COLUMN_MINUTES + " INTEGER NOT NULL DEFAULT 0" +
                    ")";

    /**
     * One row per ingredient a recipe needs. ON DELETE CASCADE means removing a recipe also
     * removes its ingredient rows, so the table cannot be left with orphans.
     */
    private static final String SQL_CREATE_RECIPE_INGREDIENTS =
            "CREATE TABLE " + RecipeIngredients.TABLE_NAME + " (" +
                    RecipeIngredients._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RecipeIngredients.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                    RecipeIngredients.COLUMN_NAME + " TEXT NOT NULL, " +
                    RecipeIngredients.COLUMN_NAME_KEY + " TEXT NOT NULL, " +
                    RecipeIngredients.COLUMN_QUANTITY + " REAL NOT NULL, " +
                    RecipeIngredients.COLUMN_UNIT + " TEXT NOT NULL, " +
                    "FOREIGN KEY (" + RecipeIngredients.COLUMN_RECIPE_ID + ") REFERENCES " +
                    Recipes.TABLE_NAME + "(" + Recipes._ID + ") ON DELETE CASCADE" +
                    ")";

    /** Speeds up loading a recipe's ingredient list. */
    private static final String SQL_INDEX_INGREDIENTS_BY_RECIPE =
            "CREATE INDEX idx_recipe_ingredients_recipe_id ON " +
                    RecipeIngredients.TABLE_NAME + "(" + RecipeIngredients.COLUMN_RECIPE_ID + ")";

    /** Speeds up the lookups the strict-matching rule performs on every suggestion refresh. */
    private static final String SQL_INDEX_INGREDIENTS_BY_NAME_KEY =
            "CREATE INDEX idx_recipe_ingredients_name_key ON " +
                    RecipeIngredients.TABLE_NAME + "(" + RecipeIngredients.COLUMN_NAME_KEY + ")";

    private PantryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Returns the shared helper. The application context is used deliberately: holding an
     * Activity context in a static field would leak that Activity when it is destroyed.
     */
    public static synchronized PantryDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PantryDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onConfigure(@NonNull SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite ignores foreign keys unless they are switched on for each connection,
        // so ON DELETE CASCADE above would silently do nothing without this line.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PANTRY_ITEMS);
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);
        db.execSQL(SQL_INDEX_INGREDIENTS_BY_RECIPE);
        db.execSQL(SQL_INDEX_INGREDIENTS_BY_NAME_KEY);

        seedRecipes(db);
    }

    /**
     * Writes the starter recipe collection.
     *
     * <p>Note that this writes through the {@code db} handed to {@link #onCreate}, and must not
     * go via RecipeDao or {@link #getWritableDatabase()}. The database is still being created at
     * this point, so asking the helper for it again re-enters creation and throws.
     *
     * <p>Wrapped in a single transaction: eighteen recipes and their ingredients are roughly a
     * hundred inserts, and committing them one at a time would make first launch noticeably slow.
     */
    private void seedRecipes(@NonNull SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (Recipe recipe : SeedData.getRecipes()) {
                ContentValues recipeValues = new ContentValues();
                recipeValues.put(Recipes.COLUMN_NAME, recipe.getName());
                recipeValues.put(Recipes.COLUMN_STEPS, recipe.getSteps());
                recipeValues.put(Recipes.COLUMN_SERVINGS, recipe.getServings());
                recipeValues.put(Recipes.COLUMN_MINUTES, recipe.getMinutes());

                long recipeId = db.insert(Recipes.TABLE_NAME, null, recipeValues);
                if (recipeId == -1) {
                    continue;
                }

                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put(RecipeIngredients.COLUMN_RECIPE_ID, recipeId);
                    ingredientValues.put(RecipeIngredients.COLUMN_NAME, ingredient.getName());
                    ingredientValues.put(
                            RecipeIngredients.COLUMN_NAME_KEY, ingredient.getNameKey());
                    ingredientValues.put(
                            RecipeIngredients.COLUMN_QUANTITY, ingredient.getQuantity());
                    ingredientValues.put(RecipeIngredients.COLUMN_UNIT, ingredient.getUnit());
                    db.insert(RecipeIngredients.TABLE_NAME, null, ingredientValues);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Rebuilds the database from scratch.
     *
     * <p>Dropping and recreating is appropriate here because the recipe collection is seeded
     * data that can always be regenerated, and the app has no released version whose user data
     * would need preserving. A production app would migrate the existing rows instead.
     */
    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecipeIngredients.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Recipes.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PantryItems.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
