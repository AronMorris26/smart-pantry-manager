package com.richfield.smartpantry.logic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts a quantity and unit into a comparable amount.
 *
 * <p>A recipe asking for 500 g of flour has to be satisfied by a pantry holding 1 kg, and one
 * asking for 2 tbsp of oil by a pantry holding 250 ml. Comparing the numbers directly would
 * answer both wrongly, so every amount is first converted to a base unit for its kind of
 * measurement: grams for mass, millilitres for volume, and a plain count for things counted
 * individually.
 *
 * <p>Amounts of different kinds are deliberately not converted between. Turning 2 cloves of
 * garlic into grams would need the density and size of a clove, which the app has no way of
 * knowing; {@link RecipeMatcher} handles that case explicitly instead of guessing here.
 */
public final class UnitConverter {

    /** The kind of measurement a unit expresses. Only like may be compared with like. */
    public enum Dimension {
        MASS,
        VOLUME,
        COUNT,

        /** A unit this app does not recognise. */
        UNKNOWN
    }

    /** A quantity expressed in the base unit of its dimension. */
    public static final class Measure {

        private final double amount;
        private final Dimension dimension;

        Measure(double amount, @NonNull Dimension dimension) {
            this.amount = amount;
            this.dimension = dimension;
        }

        /** Grams for MASS, millilitres for VOLUME, a plain count for COUNT. */
        public double getAmount() {
            return amount;
        }

        @NonNull
        public Dimension getDimension() {
            return dimension;
        }
    }

    /** How many base units one of each known unit is worth. */
    private static final Map<String, Double> FACTORS = new HashMap<>();

    private static final Map<String, Dimension> DIMENSIONS = new HashMap<>();

    static {
        // Mass, in grams.
        defineUnit("g", 1.0, Dimension.MASS);
        defineUnit("gram", 1.0, Dimension.MASS);
        defineUnit("grams", 1.0, Dimension.MASS);
        defineUnit("kg", 1000.0, Dimension.MASS);
        defineUnit("kilogram", 1000.0, Dimension.MASS);
        defineUnit("oz", 28.3495, Dimension.MASS);
        defineUnit("lb", 453.592, Dimension.MASS);

        // Volume, in millilitres.
        defineUnit("ml", 1.0, Dimension.VOLUME);
        defineUnit("l", 1000.0, Dimension.VOLUME);
        defineUnit("litre", 1000.0, Dimension.VOLUME);
        defineUnit("tsp", 5.0, Dimension.VOLUME);
        defineUnit("tbsp", 15.0, Dimension.VOLUME);
        defineUnit("cup", 240.0, Dimension.VOLUME);

        // Counted individually. All worth one, because they are ways of saying "one of these"
        // rather than genuinely different sizes - a recipe and a pantry describing the same
        // ingredient will use the same word for it.
        defineUnit("item", 1.0, Dimension.COUNT);
        defineUnit("piece", 1.0, Dimension.COUNT);
        defineUnit("clove", 1.0, Dimension.COUNT);
        defineUnit("slice", 1.0, Dimension.COUNT);
        defineUnit("whole", 1.0, Dimension.COUNT);
        defineUnit("unit", 1.0, Dimension.COUNT);
    }

    private static void defineUnit(String unit, double factor, Dimension dimension) {
        FACTORS.put(unit, factor);
        DIMENSIONS.put(unit, dimension);
    }

    private UnitConverter() {
        // Utility class - not meant to be instantiated.
    }

    /**
     * Converts a quantity into the base unit of its dimension.
     *
     * <p>An unrecognised unit returns the quantity untouched with {@link Dimension#UNKNOWN}, so
     * a unit the app has never heard of degrades into "cannot be compared" rather than being
     * silently treated as grams.
     */
    @NonNull
    public static Measure toBase(double quantity, @Nullable String unit) {
        String key = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);

        Dimension dimension = DIMENSIONS.get(key);
        Double factor = FACTORS.get(key);
        if (dimension == null || factor == null) {
            return new Measure(quantity, Dimension.UNKNOWN);
        }
        return new Measure(quantity * factor, dimension);
    }
}
