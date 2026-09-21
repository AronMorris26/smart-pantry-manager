package com.richfield.smartpantry.logic;

import androidx.annotation.NonNull;

import com.richfield.smartpantry.logic.UnitConverter.Dimension;
import com.richfield.smartpantry.logic.UnitConverter.Measure;
import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Decides which recipes the user can cook right now.
 *
 * <p>This is the core rule of the app. A recipe is only suggested when <em>every single
 * ingredient it requires is present in the pantry, in at least the required quantity</em>. A
 * recipe needing five ingredients where the pantry has four does not appear, no matter how close
 * it is. There is no partial credit and no "almost" in this list.
 *
 * <p>Two things stop that strictness from becoming brittle. Names are compared through
 * {@link IngredientNormalizer}, so "Tomatoes" in the pantry satisfies "tomato" in a recipe. And
 * quantities are compared through {@link UnitConverter}, so 1 kg of flour satisfies a recipe
 * asking for 500 g. The rule is strict about whether the user has the ingredients; it is not
 * strict about how they wrote them down.
 */
public final class RecipeMatcher {

    /**
     * Guards against floating-point error. Quantities are stored as REAL, so a value that should
     * be exactly 250 can come back as 249.99999999999997 and make a recipe the user can clearly
     * cook disappear.
     */
    private static final double TOLERANCE = 0.000001;

    private RecipeMatcher() {
        // Utility class - not meant to be instantiated.
    }

    /**
     * Tests one recipe against the pantry.
     *
     * @param recipe       the recipe to test, with its ingredients loaded
     * @param pantryByKey  the pantry keyed by normalised ingredient name, as returned by
     *                     {@code PantryDao.getAllByNameKey()}
     * @return the result, including which ingredients fell short
     */
    @NonNull
    public static MatchResult evaluate(@NonNull Recipe recipe,
                                       @NonNull Map<String, PantryItem> pantryByKey) {
        List<String> missing = new ArrayList<>();

        for (RecipeIngredient required : recipe.getIngredients()) {
            PantryItem available = pantryByKey.get(required.getNameKey());

            if (available == null || !isEnough(available, required)) {
                // The display name, not the key, because this reaches the user.
                missing.add(required.getName());
            }
        }

        return new MatchResult(missing);
    }

    /**
     * Returns only the recipes the user can make right now, in the order given.
     *
     * <p>A recipe with no ingredients at all would trivially satisfy the rule, so it is excluded
     * rather than being offered as something the user can always cook.
     */
    @NonNull
    public static List<Recipe> suggest(@NonNull List<Recipe> recipes,
                                       @NonNull Map<String, PantryItem> pantryByKey) {
        List<Recipe> canCook = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            if (evaluate(recipe, pantryByKey).isSatisfied()) {
                canCook.add(recipe);
            }
        }
        return canCook;
    }

    /**
     * Whether the pantry holds enough of one ingredient.
     *
     * <p>When both amounts measure the same kind of thing, this is a straight comparison in base
     * units. When they do not - a recipe wanting 2 cloves of garlic against a pantry recording
     * 50 g of it - there is no honest conversion between them without knowing the density and
     * size of a clove. Rather than invent a number, the ingredient counts as available on the
     * strength of being present at all.
     *
     * <p>That is a deliberate trade-off, and it errs towards suggesting a recipe the user might
     * be slightly short of rather than hiding one they can certainly cook. It only applies when
     * the two units genuinely disagree, which the fixed unit list on the Add Ingredient screen
     * makes uncommon.
     */
    private static boolean isEnough(@NonNull PantryItem available,
                                    @NonNull RecipeIngredient required) {
        Measure have = UnitConverter.toBase(available.getQuantity(), available.getUnit());
        Measure need = UnitConverter.toBase(required.getQuantity(), required.getUnit());

        if (have.getDimension() != need.getDimension()
                || have.getDimension() == Dimension.UNKNOWN) {
            return true;
        }

        return have.getAmount() + TOLERANCE >= need.getAmount();
    }
}
