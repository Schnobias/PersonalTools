package nl.schnobias.personaltools;
import org.junit.Test;
import static org.junit.Assert.*;
public class BatteryReadingTest {
    @Test public void preservesEmptyBatteryAndRejectsDisconnectSentinel() {
        assertTrue(BatteryReading.valid(0)); assertTrue(BatteryReading.valid(100));
        assertFalse(BatteryReading.valid(-1)); assertFalse(BatteryReading.valid(101));
    }
    @Test public void labelsHistoricalReadingWithItsAge() {
        assertEquals("Last known battery: 42% · reported 120 min ago", BatteryReading.message(42, 1000, 7201000));
    }
    @Test public void missingReadingDoesNotInventPercentage() {
        assertTrue(BatteryReading.message(-1, 0, 1000).startsWith("Battery unavailable"));
        assertTrue(BatteryReading.message(50, 0, 1000).startsWith("Battery unavailable"));
    }
    @Test public void clockRollbackNeverDisplaysNegativeAge() {
        assertTrue(BatteryReading.message(0, 2000, 1000).endsWith("reported just now"));
    }
}
