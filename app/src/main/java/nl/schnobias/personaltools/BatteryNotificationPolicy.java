package nl.schnobias.personaltools;

final class BatteryNotificationPolicy {
    static int threshold(int value) { return Math.max(0, Math.min(100, value)); }
    static boolean shouldNotify(boolean onlyLow, int level, int threshold) {
        return !onlyLow || (BatteryReading.valid(level) && level <= threshold(threshold));
    }
}
