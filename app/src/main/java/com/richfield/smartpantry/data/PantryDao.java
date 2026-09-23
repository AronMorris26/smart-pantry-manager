package com.richfield.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.richfield.smartpantry.data.PantryContract.PantryItems;
import com.richfield.smartpantry.logic.IngredientNormalizer;
import com.richfield.smartpantry.model.PantryItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All reads and writes against the pantry_items table.
 *
 * <p>Keeping SQL in one class means the Activities and Fragments never touch a Cursor or a
 * ContentValues directly - they ask for {@link PantryItem} objects and hand back
 * {@link PantryItem} objects. That keeps the UI code readable and means the storage choice
 * could change without rewriting any screen.
 */
public class PantryDao {

    /** Returned by {@link #insert} when the insert failed. */
    public static final long INSERT_FAILED = -1L;

    private final PantryDbHelper helper;

    public PantryDao(@NonNull Context context) {
        this.helper = PantryDbHelper.getInstance(context);
    }

    /**
     * Saves a new pantry item.
     *
     * @return the new row id, or {@link #INSERT_FAILED} if an item with the same normalised
     *         name already exists (the name_key column is UNIQUE)
     */
    public long insert(@NonNull PantryItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(PantryItems.TABLE_NAME, null, toContentValues(item));
        if (id != INSERT_FAILED) {
            item.setId(id);
        }
        return id;
    }

    /**
     * Updates an existing pantry item.
     *
     * @return the number of rows changed, which is 1 on success and 0 if the id was not found
     */
    public int update(@NonNull PantryItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(
                PantryItems.TABLE_NAME,
                toContentValues(item),
                PantryItems._ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /**
     * Deletes a pantry item.
     *
     * @return the number of rows removed
     */
    public int delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(
                PantryItems.TABLE_NAME,
                PantryItems._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** Returns the item with this id, or null if there is none. */
    @Nullable
    public PantryItem findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(
                PantryItems.TABLE_NAME,
                null,
                PantryItems._ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    /**
     * Returns the item whose normalised name matches this one, or null if there is none.
     *
     * <p>Used to stop the user adding the same ingredient twice under a slightly different
     * spelling, and to offer to update the existing row instead.
     */
    @Nullable
    public PantryItem findByName(@NonNull String rawName) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(
                PantryItems.TABLE_NAME,
                null,
                PantryItems.COLUMN_NAME_KEY + " = ?",
                new String[]{IngredientNormalizer.key(rawName)},
                null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    /** Returns every pantry item, sorted by name, ignoring case. */
    @NonNull
    public List<PantryItem> getAll() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(
                PantryItems.TABLE_NAME,
                null, null, null, null, null,
                PantryItems.COLUMN_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        }
        return items;
    }

    /**
     * Returns every pantry item, soonest expiry date first.
     *
     * <p>Items with no expiry date sort to the end rather than to the front, which is what
     * NULLs would otherwise do.
     */
    @NonNull
    public List<PantryItem> getAllByExpiry() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(
                PantryItems.TABLE_NAME,
                null, null, null, null, null,
                PantryItems.COLUMN_EXPIRY_DATE + " IS NULL, "
                        + PantryItems.COLUMN_EXPIRY_DATE + " ASC, "
                        + PantryItems.COLUMN_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        }
        return items;
    }

    /**
     * Returns every pantry item keyed by its normalised name.
     *
     * <p>This is the shape the strict-matching rule needs: checking whether a recipe ingredient
     * is available becomes a single map lookup per ingredient instead of a scan of the list.
     */
    @NonNull
    public Map<String, PantryItem> getAllByNameKey() {
        Map<String, PantryItem> byKey = new LinkedHashMap<>();
        for (PantryItem item : getAll()) {
            byKey.put(item.getNameKey(), item);
        }
        return byKey;
    }

    /** Number of items currently in the pantry. Used to decide whether to show the empty state. */
    public int count() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + PantryItems.TABLE_NAME, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /** Maps a PantryItem onto the column values the database expects. */
    private ContentValues toContentValues(@NonNull PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(PantryItems.COLUMN_NAME, item.getName());
        values.put(PantryItems.COLUMN_NAME_KEY, item.getNameKey());
        values.put(PantryItems.COLUMN_QUANTITY, item.getQuantity());
        values.put(PantryItems.COLUMN_UNIT, item.getUnit());
        if (item.hasExpiryDate()) {
            values.put(PantryItems.COLUMN_EXPIRY_DATE, item.getExpiryDate());
        } else {
            // Explicitly null rather than absent, so clearing a date on an edit actually clears it.
            values.putNull(PantryItems.COLUMN_EXPIRY_DATE);
        }
        return values;
    }

    /** Reads the row the cursor is currently positioned on into a PantryItem. */
    private PantryItem fromCursor(@NonNull Cursor cursor) {
        int expiryIndex = cursor.getColumnIndexOrThrow(PantryItems.COLUMN_EXPIRY_DATE);
        Long expiryDate = cursor.isNull(expiryIndex) ? null : cursor.getLong(expiryIndex);

        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(PantryItems._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryItems.COLUMN_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(PantryItems.COLUMN_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryItems.COLUMN_UNIT)),
                expiryDate);
    }
}
