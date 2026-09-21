package com.richfield.smartpantry.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the rules that decide when two differently written names mean the same ingredient.
 */
public class IngredientNormalizerTest {

    @Test
    public void ignoresCaseAndSurroundingSpace() {
        assertEquals("tomato", IngredientNormalizer.key("  Tomato  "));
        assertEquals("olive oil", IngredientNormalizer.key("Olive Oil"));
    }

    @Test
    public void collapsesRepeatedSpacesAndDropsPunctuation() {
        assertEquals("cheddar cheese", IngredientNormalizer.key("Cheddar    cheese"));
        assertEquals("tomato", IngredientNormalizer.key("Tomato."));
    }

    @Test
    public void makesSimplePluralsSingular() {
        assertEquals("egg", IngredientNormalizer.key("Eggs"));
        assertEquals("onion", IngredientNormalizer.key("onions"));
    }

    @Test
    public void handlesPluralsEndingInOes() {
        assertEquals("tomato", IngredientNormalizer.key("Tomatoes"));
        assertEquals("potato", IngredientNormalizer.key("potatoes"));
    }

    @Test
    public void handlesPluralsEndingInIes() {
        assertEquals("berry", IngredientNormalizer.key("berries"));
    }

    @Test
    public void leavesWordsThatOnlyLookPluralAlone() {
        assertEquals("rice", IngredientNormalizer.key("Rice"));
        assertEquals("hummus", IngredientNormalizer.key("hummus"));
        assertEquals("molasses", IngredientNormalizer.key("Molasses"));
        assertEquals("asparagus", IngredientNormalizer.key("asparagus"));
    }

    @Test
    public void onlyMakesTheLastWordSingular() {
        // Many breasts, not many chickens.
        assertEquals("chicken breast", IngredientNormalizer.key("Chicken breasts"));
    }

    @Test
    public void dropsLeadingDescriptors() {
        assertEquals("tomato", IngredientNormalizer.key("Fresh chopped tomatoes"));
        assertEquals("chicken breast", IngredientNormalizer.key("boneless skinless chicken breast"));
    }

    @Test
    public void neverStripsAwayTheWholeName() {
        assertEquals("fresh", IngredientNormalizer.key("Fresh"));
    }

    @Test
    public void foldsSynonymsOntoOneName() {
        assertEquals(IngredientNormalizer.key("ground beef"),
                IngredientNormalizer.key("Beef mince"));
        assertEquals(IngredientNormalizer.key("eggplant"),
                IngredientNormalizer.key("Aubergine"));
        assertEquals(IngredientNormalizer.key("green onion"),
                IngredientNormalizer.key("Spring onions"));
    }

    @Test
    public void handlesNullAndEmptyInput() {
        assertEquals("", IngredientNormalizer.key(null));
        assertEquals("", IngredientNormalizer.key("   "));
        assertEquals("", IngredientNormalizer.key("!!!"));
    }
}
