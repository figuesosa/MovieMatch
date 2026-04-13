package moviematch.util;

import moviematch.modelo.Pelicula;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cache local de posters para evitar depender siempre de la red.
 */
public final class CachePosters {

    private static final CachePosters INSTANCIA = new CachePosters();

    private final HttpClient http = HttpClient.newHttpClient();
    private final Path directorioCache = Paths.get(System.getProperty("user.home"), ".moviematch", "poster-cache");

    private CachePosters() {
    }

    public static CachePosters get() {
        return INSTANCIA;
    }

    public ImageIcon obtenerPosterEscalado(Pelicula pelicula, int ancho, int alto) {
        if (pelicula == null || pelicula.getPosterUrl() == null || pelicula.getPosterUrl().isBlank()) {
            return null;
        }
        try {
            Path archivo = resolverArchivoCache(pelicula);
            if (!Files.exists(archivo)) {
                descargarPoster(pelicula.getPosterUrl(), archivo);
            }
            if (!Files.exists(archivo)) {
                return null;
            }
            BufferedImage img = ImageIO.read(archivo.toFile());
            if (img == null) {
                return null;
            }
            Image escalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            return new ImageIcon(escalada);
        } catch (Exception e) {
            System.err.println("No se pudo obtener poster cacheado: " + e.getMessage());
            return null;
        }
    }

    private Path resolverArchivoCache(Pelicula pelicula) throws IOException {
        Files.createDirectories(directorioCache);
        String base;
        if (pelicula.getId() > 0) {
            base = "movie-" + pelicula.getId();
        } else {
            base = "url-" + sha1Hex(pelicula.getPosterUrl());
        }
        return directorioCache.resolve(base + ".jpg");
    }

    private void descargarPoster(String url, Path destino) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<byte[]> r = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (r.statusCode() / 100 == 2 && r.body() != null && r.body().length > 0) {
                Files.write(destino, r.body());
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            System.err.println("Error descargando poster: " + e.getMessage());
        }
    }

    private static String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
