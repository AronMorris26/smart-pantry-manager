package com.richfield.smartpantry.model;

import static org.junit.Assert.assertEquals;

import com.richfield.smartpantry.model.PantryItem.ExpiryStatus;

import org.junit.Test;

/** Tests how an item is classified against its expiry date. */
public class PantryItemTest {

    private static final long NOW = 1_800_000_000_000L;
    private static final long ONE_DAY = 24L * 60L * 60L * 1000L;

    @Test
    public void reportsNoStatusWhenNoExpiryDateIsSet() {
        assertEquals(ExpiryStatus.NONE, item(null).getExpiryStatus(NOW, 3));
    }

    @Test
    public void reportsExpiredWhenTheDateHasPassed() {
        assertEquals(ExpiryStatus.EXPIRED, item(NOW - ONE_DAY).getExpiryStatus(NOW, 3));
    }

    @Test
    public void reportsExpiringInsideTheWarningWindow() {
        assertEquals(ExpiryStatus.EXPIRING, item(NOW + 2 * ONE_DAY).getExpiryStatus(NOW, 3));
    }

    @Test
    public void reportsFreshBeyondTheWarningWindow() {
        assertEquals(ExpiryStatus.FRESH, item(NOW + 10 * ONE_DAY).getExpiryStatus(NOW, 3));
    }

    @Test
    public void widensTheWarningWindowWhenAsked() {
        PantryItem item = item(NOW + 10 * ONE_DAY);

        assertEquals(ExpiryStatus.FRESH, item.getExpiryStatus(NOW, 3));
        assertEquals(ExpiryStatus.EXPIRING, item.getExpiryStatus(NOW, 14));
    }

    private static PantryItem item(Long expiryDate) {
        return new PantryItem("Milk", 500, "ml", expiryDate);
    }
}
