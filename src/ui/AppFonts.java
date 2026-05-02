package ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

public class AppFonts {

    private static Font regular;
    private static Font bold;
    private static Font medium;
    private static Font light;
    private static boolean loaded = false;

    private static final float TITLE_SIZE    = 38f;
    private static final float SUBTITLE_SIZE = 20f;
    private static final float BODY_SIZE     = 14f;
    private static final float SMALL_SIZE    = 12f;
    private static final float BUTTON_SIZE   = 14f;
    private static final float MONO_SIZE     = 13f;

    public static void load() {
        if (loaded) return;

        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            regular = loadTtf("fonts/Poppins-Regular.ttf");
            bold    = loadTtf("fonts/Poppins-Bold.ttf");
            medium  = loadTtf("fonts/Poppins-Medium.ttf");
            light   = loadTtf("fonts/Poppins-Light.ttf");

            ge.registerFont(regular);
            ge.registerFont(bold);
            ge.registerFont(medium);
            ge.registerFont(light);

            loaded = true;
            System.out.println("[AppFonts] Poppins carregada com sucesso.");

        } catch (Exception e) {
            System.out.println("[AppFonts] Poppins nao encontrada em fonts/ — usando fallback. " + e.getMessage());
            loaded = false;
        }
    }

    private static Font loadTtf(String path) throws IOException, FontFormatException {
        File f = new File(path);
        if (!f.exists()) throw new IOException("Arquivo nao encontrado: " + path);
        return Font.createFont(Font.TRUETYPE_FONT, f);
    }

    public static Font title()    { return derive(bold,    TITLE_SIZE,    Font.BOLD);  }
    public static Font subtitle() { return derive(bold,    SUBTITLE_SIZE, Font.BOLD);  }
    public static Font body()     { return derive(regular, BODY_SIZE,     Font.PLAIN); }
    public static Font small()    { return derive(regular, SMALL_SIZE,    Font.PLAIN); }
    public static Font button()   { return derive(medium,  BUTTON_SIZE,   Font.BOLD);  }
    public static Font light()    { return derive(light,   SMALL_SIZE,    Font.PLAIN); }

    public static Font mono()     { return new Font("Consolas", Font.PLAIN, (int) MONO_SIZE); }

    private static Font derive(Font base, float size, int style) {
        if (base == null) return fallback(size, style);
        return base.deriveFont(style, size);
    }

    private static Font fallback(float size, int style) {
        for (String name : new String[]{"Segoe UI", "Ubuntu", "Cantarell", "Arial"}) {
            Font f = new Font(name, style, (int) size);
            if (!f.getFamily().equals("Dialog")) return f;
        }
        return new Font("Poppins", style, (int) size);
    }

    public static boolean isLoaded() { return loaded; }
}
