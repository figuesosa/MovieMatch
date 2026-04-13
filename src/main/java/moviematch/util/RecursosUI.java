package moviematch.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Icono de aplicación desde classpath y variantes escaladas (barra de título, barra de tareas, dock).
 */
public final class RecursosUI {

    private static final String RUTA_ICONO = "/images/moviematch-icon.png";
    private static final int[] TAMAÑOS_ICONO_VENTANA = {16, 24, 32, 48, 64, 128, 256};

    private static BufferedImage imagenBase;

    private RecursosUI() {
    }

    public static synchronized BufferedImage obtenerImagenBase() {
        if (imagenBase != null) {
            return imagenBase;
        }
        try (InputStream in = RecursosUI.class.getResourceAsStream(RUTA_ICONO)) {
            if (in == null) {
                return null;
            }
            imagenBase = ImageIO.read(in);
        } catch (IOException e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
            return null;
        }
        return imagenBase;
    }

    /**
     * Iconos en varios tamaños para {@link java.awt.Window#setIconImages}.
     */
    public static List<Image> iconosVentana() {
        BufferedImage base = obtenerImagenBase();
        List<Image> lista = new ArrayList<>();
        if (base == null) {
            return lista;
        }
        for (int s : TAMAÑOS_ICONO_VENTANA) {
            lista.add(escalar(base, s, s));
        }
        return lista;
    }

    public static void aplicarIconoVentana(Window ventana) {
        List<Image> imgs = iconosVentana();
        if (!imgs.isEmpty()) {
            ventana.setIconImages(imgs);
        }
    }

    /**
     * Logo compacto para cabeceras dentro de la ventana (p. ej. 32 px).
     */
    public static ImageIcon iconoCabecera(int px) {
        BufferedImage base = obtenerImagenBase();
        if (base == null) {
            return null;
        }
        return new ImageIcon(escalar(base, px, px));
    }

    private static BufferedImage escalar(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}
