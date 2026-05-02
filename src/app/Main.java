package app;

import javax.swing.SwingUtilities;
import ui.AppFonts;
import ui.UiTheme;
import ui.MainWindow;

public class Main {
    public static void main(String[] args) {
        // 1. Carrega Poppins antes de qualquer componente Swing ser criado
        AppFonts.load();
        UiTheme.applyFonts();

        // 2. Inicia a janela na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}
