package com.richfield.smartpantry.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/** Reads and writes the user's display preferences. */
public class AppPreferences {

    /** How the pantry list is ordered. */
    public enum SortOrder {
        NAME,
        EXPIRY
    }

    public static final int DEFAULT_EXPIRY_WINDOW_DAYS = 3;

    private static final String FILE_NAME = "smart_pantry_prefs";
    private static final String KEY_HIGHLIGHT_EXPIRING = "highlight_expiring";
    private static final String KEY_EXPIRY_WINDOW_DAYS = "expiry_window_days";
    private static final String KEY_SORT_ORDER = "sort_order";

    private final SharedPreferences preferences;

    public AppPreferences(@NonNull Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isHighlightExpiringEnabled() {
        return preferences.getBoolean(KEY_HIGHLIGHT_EXPIRING, true);
    }

    public void setHighlightExpiringEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_HIGHLIGHT_EXPIRING, enabled).apply();
    }

    public int getExpiryWindowDays() {
        return preferences.getInt(KEY_EXPIRY_WINDOW_DAYS, DEFAULT_EXPIRY_WINDOW_DAYS);
    }

    public void setExpiryWindowDays(int days) {
        preferences.edit().putInt(KEY_EXPIRY_WINDOW_DAYS, days).apply();
    }

    @NonNull
    public SortOrder getSortOrder() {
        String stored = preferences.getString(KEY_SORT_ORDER, SortOrder.NAME.name());
        try {
            return SortOrder.valueOf(stored);
        } catch (IllegalArgumentException unknownValue) {
            return SortOrder.NAME;
        }
    }

    public void setSortOrder(@NonNull SortOrder order) {
        preferences.edit().putString(KEY_SORT_ORDER, order.name()).apply();
    }
}
