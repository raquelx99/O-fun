package app;

import javax.swing.SwingUtilities;
import ui.AppFonts;
import ui.UiTheme;
import ui.MainWindow;

public class Main {
    public static void main(String[] args) {
        AppFonts.load();
        UiTheme.applyFonts();

        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}
