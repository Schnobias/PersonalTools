package nl.schnobias.personaltools;

final class BatteryReading {
    static boolean valid(int percentage) { return percentage >= 0 && percentage <= 100; }
    static String message(int percentage, long recordedAt, long now) {
        if (!valid(percentage) || recordedAt <= 0) return "Battery unavailable — this device has not reported a battery level.";
        long minutes = Math.max(0, now - recordedAt) / 60_000;
        return "Last known battery: " + percentage + "% · "
                + (minutes == 0 ? "reported just now" : "reported " + minutes + " min ago");
    }
}
