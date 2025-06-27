package resources.Icons;

import java.awt.*;
import javax.swing.*;

public class AppIconLoader {
    public static Image getAppIcon() {
        // Try to load icon from /Icons/app_icon.png (for PNG) and fallback to /Icons/app_icon.ico (for ICO)
        try {
            java.net.URL iconUrl = AppIconLoader.class.getResource("/Icons/app_icon.ico");
            if (iconUrl != null) {
                return new ImageIcon(iconUrl).getImage();
            } else {
                iconUrl = AppIconLoader.class.getResource("/Icons/app_icon.ico");
                if (iconUrl != null) {
                    return new ImageIcon(iconUrl).getImage();
                }
            }
        } catch (Exception e) {
            // Optionally log the error
        }
        return null;
    }
}
