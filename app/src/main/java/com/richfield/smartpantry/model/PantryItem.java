package com.richfield.smartpantry.model;

import com.richfield.smartpantry.logic.IngredientNormalizer;

/**
 * A single ingredient the user currently has at home.
 *
 * <p>The {@code nameKey} is always derived from {@code name} rather than being set
 * independently, so the two can never drift apart.
 */
public class PantryItem {

    /** Id used for an item that has not been saved to the database yet. */
    public static final long NO_ID = -1L;

    private long id;
    private String name;
    private String nameKey;
    private double quantity;
    private String unit;

    /** Expiry date as epoch milliseconds, or null if the user did not supply one. */
    private Long expiryDate;

    /** Creates a new, unsaved pantry item. */
    public PantryItem(String name, double quantity, String unit, Long expiryDate) {
        this(NO_ID, name, quantity, unit, expiryDate);
    }

    /** Creates a pantry item that already exists in the database. */
    public PantryItem(long id, String name, double quantity, String unit, Long expiryDate) {
        this.id = id;
        setName(name);
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
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

    public Long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean hasExpiryDate() {
        return expiryDate != null;
    }

    /** True if this item has not been saved to the database yet. */
    public boolean isNew() {
        return id == NO_ID;
    }

    @Override
    public String toString() {
        return name + " (" + quantity + " " + unit + ")";
    }
}
