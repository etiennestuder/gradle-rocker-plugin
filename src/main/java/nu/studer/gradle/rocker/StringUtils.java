package nu.studer.gradle.rocker;

public abstract class StringUtils {
    public static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
