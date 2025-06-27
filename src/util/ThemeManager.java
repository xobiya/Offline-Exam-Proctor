package util;

import java.awt.Color;

public class ThemeManager {
    public enum Theme { LIGHT, DARK }
    private static Theme currentTheme = Theme.LIGHT;

    // Define color schemes
    public static Color getPrimaryBackground() {
        return currentTheme == Theme.LIGHT ? Color.WHITE : new Color(34, 34, 34);
    }
    public static Color getPrimaryForeground() {
        return currentTheme == Theme.LIGHT ? Color.BLACK : Color.WHITE;
    }
    public static Color getAccent() {
        return currentTheme == Theme.LIGHT ? new Color(33, 150, 243) : new Color(100, 181, 246);
    }
    public static Color getShadow() {
        return currentTheme == Theme.LIGHT ? new Color(220, 220, 220) : new Color(44, 44, 44);
    }
    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }
    public static Theme getTheme() {
        return currentTheme;
    }
}
