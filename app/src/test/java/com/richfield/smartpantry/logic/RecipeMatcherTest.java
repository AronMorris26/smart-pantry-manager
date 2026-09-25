package com.richfield.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the strict-matching rule: a recipe qualifies only when every ingredient it needs is in
 * the pantry in at least the required amount.
 */
public class RecipeMatcherTest {

    @Test
    public void suggestsARecipeWhenEveryIngredientIsPresent() {
        Recipe omelette = recipe("Cheese Omelette",
                needs("Eggs", 3, "item"),
                needs("Cheddar cheese", 60, "g"),
                needs("Butter", 15, "g"));

        Map<String, PantryItem> pantry = pantry(
                have("Eggs", 6, "item"),
                have("Cheddar cheese", 200, "g"),
                have("Butter", 200, "g"));

        assertTrue(RecipeMatcher.evaluate(omelette, pantry).isSatisfied());
        assertEquals(1, RecipeMatcher.suggest(Collections.singletonList(omelette), pantry).size());
    }

    /** The exact case the brief calls out: five needed, four held, must not appear. */
    @Test
    public void excludesARecipeMissingOneOfFiveIngredients() {
        Recipe pasta = recipe("Garlic Pasta",
                needs("Pasta", 200, "g"),
                needs("Garlic", 3, "clove"),
                needs("Olive oil", 30, "ml"),
                needs("Salt", 5, "g"),
                needs("Cheddar cheese", 50, "g"));

        Map<String, PantryItem> pantry = pantry(
                have("Pasta", 500, "g"),
                have("Garlic", 4, "clove"),
                have("Olive oil", 250, "ml"),
                have("Salt", 100, "g"));

        MatchResult result = RecipeMatcher.evaluate(pasta, pantry);

        assertFalse(result.isSatisfied());
        assertTrue(RecipeMatcher.suggest(Collections.singletonList(pasta), pantry).isEmpty());
    }

    @Test
    public void namesTheIngredientsThatFellShort() {
        Recipe toastie = recipe("Cheese Toastie",
                needs("Bread", 2, "slice"),
                needs("Cheddar cheese", 60, "g"));

        MatchResult result = RecipeMatcher.evaluate(toastie,
                pantry(have("Cheddar cheese", 200, "g")));

        assertEquals(1, result.getMissingCount());
        assertEquals("Bread", result.getMissingIngredients().get(0));
    }

    @Test
    public void matchesAPluralPantryNameAgainstASingularRecipeName() {
        Recipe soup = recipe("Tomato Soup", needs("tomato", 4, "item"));

        assertTrue(RecipeMatcher.evaluate(soup, pantry(have("Tomatoes", 5, "item")))
                .isSatisfied());
    }

    @Test
    public void ignoresDescriptiveWordsInPantryNames() {
        Recipe soup = recipe("Tomato Soup", needs("tomato", 4, "item"));

        assertTrue(RecipeMatcher.evaluate(soup, pantry(have("Fresh chopped tomatoes", 5, "item")))
                .isSatisfied());
    }

    /** Every ingredient present, but not enough of one of them. */
    @Test
    public void excludesARecipeWhenAnIngredientIsPresentButInsufficient() {
        Recipe pudding = recipe("Rice Pudding",
                needs("Rice", 150, "g"),
                needs("Milk", 700, "ml"));

        MatchResult result = RecipeMatcher.evaluate(pudding,
                pantry(have("Rice", 1, "kg"), have("Milk", 500, "ml")));

        assertFalse(result.isSatisfied());
        assertEquals(Collections.singletonList("Milk"), result.getMissingIngredients());
    }

    @Test
    public void convertsBetweenUnitsOfTheSameKind() {
        Recipe shortbread = recipe("Shortbread", needs("Flour", 500, "g"));

        // 1 kg satisfies a recipe written in grams.
        assertTrue(RecipeMatcher.evaluate(shortbread, pantry(have("Flour", 1, "kg")))
                .isSatisfied());

        Recipe dressing = recipe("Dressing", needs("Olive oil", 2, "tbsp"));

        // 250 ml satisfies a recipe written in tablespoons.
        assertTrue(RecipeMatcher.evaluate(dressing, pantry(have("Olive oil", 250, "ml")))
                .isSatisfied());
    }

    @Test
    public void treatsAnExactlyEqualQuantityAsEnough() {
        Recipe macCheese = recipe("Macaroni Cheese", needs("Cheddar cheese", 200, "g"));

        assertTrue(RecipeMatcher.evaluate(macCheese, pantry(have("Cheddar cheese", 200, "g")))
                .isSatisfied());
    }

    /**
     * Cloves against grams cannot be compared without knowing what a clove weighs, so the
     * ingredient counts as available on the strength of being present.
     */
    @Test
    public void fallsBackToPresenceWhenUnitsMeasureDifferentThings() {
        Recipe pasta = recipe("Garlic Pasta", needs("Garlic", 3, "clove"));

        assertTrue(RecipeMatcher.evaluate(pasta, pantry(have("Garlic", 50, "g")))
                .isSatisfied());
    }

    @Test
    public void suggestsNothingFromAnEmptyPantry() {
        Recipe eggs = recipe("Scrambled Eggs", needs("Eggs", 3, "item"));

        List<Recipe> suggestions = RecipeMatcher.suggest(
                Collections.singletonList(eggs), new LinkedHashMap<>());

        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void doesNotSuggestARecipeWithNoIngredients() {
        Recipe empty = recipe("Mystery Dish");

        assertTrue(RecipeMatcher.suggest(Collections.singletonList(empty),
                pantry(have("Eggs", 6, "item"))).isEmpty());
    }

    @Test
    public void keepsOnlyTheCookableRecipesOutOfSeveral() {
        Recipe canCook = recipe("Boiled Rice", needs("Rice", 200, "g"));
        Recipe cannotCook = recipe("Cheese Toastie", needs("Bread", 2, "slice"));

        List<Recipe> suggestions = RecipeMatcher.suggest(
                Arrays.asList(canCook, cannotCook), pantry(have("Rice", 1, "kg")));

        assertEquals(1, suggestions.size());
        assertEquals("Boiled Rice", suggestions.get(0).getName());
    }

    @Test
    public void listsARecipeMissingExactlyOneIngredientAsAlmostThere() {
        Recipe toastie = recipe("Cheese Toastie",
                needs("Bread", 2, "slice"),
                needs("Cheddar cheese", 60, "g"));

        RecipeMatcher.Suggestions result = RecipeMatcher.partition(
                Collections.singletonList(toastie), pantry(have("Cheddar cheese", 200, "g")));

        assertTrue(result.getReadyToCook().isEmpty());
        assertEquals(1, result.getAlmostThere().size());
        assertEquals("Cheese Toastie", result.getAlmostThere().get(0).getRecipe().getName());
        assertEquals("Bread", result.getAlmostThere().get(0).getMissingIngredient());
    }

    @Test
    public void keepsAlmostThereRecipesOutOfTheCookableList() {
        Recipe canCook = recipe("Boiled Rice", needs("Rice", 200, "g"));
        Recipe oneShort = recipe("Cheese Toastie",
                needs("Bread", 2, "slice"), needs("Cheddar cheese", 60, "g"));
        Recipe wayOff = recipe("Curry",
                needs("Potatoes", 3, "item"), needs("Curry powder", 15, "g"));

        RecipeMatcher.Suggestions result = RecipeMatcher.partition(
                Arrays.asList(canCook, oneShort, wayOff),
                pantry(have("Rice", 1, "kg"), have("Cheddar cheese", 200, "g")));

        assertEquals(1, result.getReadyToCook().size());
        assertEquals("Boiled Rice", result.getReadyToCook().get(0).getName());
        assertEquals(1, result.getAlmostThere().size());
        assertEquals("Cheese Toastie", result.getAlmostThere().get(0).getRecipe().getName());
    }

    @Test
    public void countsAnInsufficientQuantityAsTheOneMissingIngredient() {
        Recipe pudding = recipe("Rice Pudding",
                needs("Rice", 150, "g"), needs("Milk", 700, "ml"));

        RecipeMatcher.Suggestions result = RecipeMatcher.partition(
                Collections.singletonList(pudding),
                pantry(have("Rice", 1, "kg"), have("Milk", 500, "ml")));

        assertTrue(result.getReadyToCook().isEmpty());
        assertEquals("Milk", result.getAlmostThere().get(0).getMissingIngredient());
    }

    // --- helpers -------------------------------------------------------------------------

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(1, name, "Step one.", 2, 15);
        recipe.setIngredients(new ArrayList<>(Arrays.asList(ingredients)));
        return recipe;
    }

    private static RecipeIngredient needs(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static PantryItem have(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit, null);
    }

    /** Builds the pantry in the shape the matcher expects: keyed by normalised name. */
    private static Map<String, PantryItem> pantry(PantryItem... items) {
        Map<String, PantryItem> byKey = new LinkedHashMap<>();
        for (PantryItem item : items) {
            byKey.put(item.getNameKey(), item);
        }
        return byKey;
    }
}
