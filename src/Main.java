import ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            try {
                java.net.URL icoUrl = Main.class.getResource("/Icons/appIcons.png");
                if (icoUrl != null) {
                    frame.setIconImage(new ImageIcon(icoUrl).getImage());
                }
            } catch (Exception e) {
                // Ignore if icon not found
            }
            frame.setVisible(true);
        });
    }
}
