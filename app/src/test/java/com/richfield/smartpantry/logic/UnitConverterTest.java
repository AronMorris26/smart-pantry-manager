package com.richfield.smartpantry.logic;

import static org.junit.Assert.assertEquals;

import com.richfield.smartpantry.logic.UnitConverter.Dimension;
import com.richfield.smartpantry.logic.UnitConverter.Measure;

import org.junit.Test;

/** Tests that amounts written in different units become comparable numbers. */
public class UnitConverterTest {

    private static final double DELTA = 0.0001;

    @Test
    public void convertsMassToGrams() {
        Measure measure = UnitConverter.toBase(1, "kg");
        assertEquals(1000.0, measure.getAmount(), DELTA);
        assertEquals(Dimension.MASS, measure.getDimension());
    }

    @Test
    public void convertsVolumeToMillilitres() {
        assertEquals(30.0, UnitConverter.toBase(2, "tbsp").getAmount(), DELTA);
        assertEquals(240.0, UnitConverter.toBase(1, "cup").getAmount(), DELTA);
        assertEquals(1000.0, UnitConverter.toBase(1, "l").getAmount(), DELTA);
    }

    @Test
    public void treatsCountUnitsAsPlainNumbers() {
        assertEquals(3.0, UnitConverter.toBase(3, "clove").getAmount(), DELTA);
        assertEquals(Dimension.COUNT, UnitConverter.toBase(3, "clove").getDimension());
        assertEquals(Dimension.COUNT, UnitConverter.toBase(1, "slice").getDimension());
    }

    @Test
    public void ignoresCaseAndSurroundingSpaceInUnits() {
        assertEquals(1000.0, UnitConverter.toBase(1, " KG ").getAmount(), DELTA);
    }

    @Test
    public void flagsUnknownUnitsInsteadOfGuessing() {
        Measure measure = UnitConverter.toBase(5, "handfuls");
        assertEquals(Dimension.UNKNOWN, measure.getDimension());
        assertEquals(5.0, measure.getAmount(), DELTA);
    }

    @Test
    public void treatsMissingUnitAsUnknown() {
        assertEquals(Dimension.UNKNOWN, UnitConverter.toBase(1, null).getDimension());
        assertEquals(Dimension.UNKNOWN, UnitConverter.toBase(1, "").getDimension());
    }
}
