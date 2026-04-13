package moviematch.util;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;

/**
 * Look & feel y colores globales alineados con la marca MovieMatch (tonos cine / cobre).
 */
public final class TemaAplicacion {

    /** Fondo principal de paneles (azul grafito). */
    public static final Color FONDO_PANEL = new Color(0x1C, 0x25, 0x30);
    /** Superficie elevada (tarjetas, cabecera). */
    public static final Color FONDO_SUPERFICIE = new Color(0x2D, 0x3A, 0x4B);
    /** Acento cobre / ámbar (botones, foco, pestaña activa). */
    public static final Color ACENTO = new Color(0xD4, 0x8C, 0x4D);
    /** Texto secundario / bordes suaves. */
    public static final Color TEXTO_SUAVE = new Color(0x7A, 0x8D, 0xA1);

    private TemaAplicacion() {
    }

    public static void instalar() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                return;
            }
        }

        UIManager.put("Component.accentColor", new ColorUIResource(ACENTO));
        UIManager.put("Component.focusColor", new ColorUIResource(ACENTO));
        UIManager.put("Button.default.background", new ColorUIResource(oscurecer(ACENTO, 0.85f)));
        UIManager.put("Button.default.foreground", new ColorUIResource(Color.WHITE));
        UIManager.put("TabbedPane.selectedBackground", new ColorUIResource(FONDO_SUPERFICIE));
        UIManager.put("TabbedPane.hoverColor", new ColorUIResource(oscurecer(FONDO_SUPERFICIE, 0.92f)));
        UIManager.put("TabbedPane.underlineColor", new ColorUIResource(ACENTO));
        UIManager.put("TabbedPane.focusColor", new ColorUIResource(ACENTO));
        UIManager.put("Menu.selectionBackground", new ColorUIResource(oscurecer(ACENTO, 0.75f)));
        UIManager.put("MenuItem.selectionBackground", new ColorUIResource(oscurecer(ACENTO, 0.75f)));
        UIManager.put("ScrollBar.thumb", new ColorUIResource(TEXTO_SUAVE));
    }

    private static Color oscurecer(Color c, float factor) {
        return new Color(
                Math.max(0, Math.round(c.getRed() * factor)),
                Math.max(0, Math.round(c.getGreen() * factor)),
                Math.max(0, Math.round(c.getBlue() * factor)));
    }
}
