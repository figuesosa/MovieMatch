package moviematch.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import moviematch.modelo.PeliculaExterna;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integracion minima con TMDB para buscar peliculas online e importarlas al catalogo local.
 */
public class TmdbService {

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String BASE_IMG = "https://image.tmdb.org/t/p/w342";
    private static final String LANG_DEFAULT = "en-US";
    private static final int LIMITE_RESULTADOS = 15;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Map<Integer, String>> cacheGenerosPorIdioma = new HashMap<>();

    public record TmdbMetric(
            int tmdbMovieId,
            double voteAverage,
            int voteCount,
            double popularity,
            String posterUrl) {
    }

    public boolean estaConfigurado() {
        return !obtenerToken().isBlank();
    }

    public List<PeliculaExterna> buscarPeliculas(String query) {
        return buscarPeliculas(query, obtenerIdiomaPorDefecto());
    }

    public List<PeliculaExterna> buscarPeliculas(String query, String idioma) {
        List<PeliculaExterna> out = new ArrayList<>();
        if (query == null || query.isBlank() || !estaConfigurado()) {
            return out;
        }
        String lang = normalizarIdioma(idioma);
        String q = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = BASE_URL + "/search/movie?language=" + lang + "&include_adult=false&page=1&query=" + q;
        try {
            JsonNode root = getJson(url);
            if (root == null || !root.has("results")) {
                return out;
            }
            Map<Integer, String> mapaGeneros = obtenerMapaGeneros(lang);
            for (JsonNode n : root.get("results")) {
                if (out.size() >= LIMITE_RESULTADOS) {
                    break;
                }
                PeliculaExterna p = new PeliculaExterna();
                p.setTmdbId(n.path("id").asInt(0));
                p.setTitulo(n.path("title").asText("").trim());
                p.setDescripcion(n.path("overview").asText(""));
                p.setAnioLanzamiento(extraerAnio(n.path("release_date").asText("")));
                p.setPopularidad(n.path("popularity").asDouble(0.0));
                p.setPosterUrl(construirPosterUrl(n.path("poster_path").asText("")));
                List<String> generos = new ArrayList<>();
                if (n.has("genre_ids")) {
                    for (JsonNode gid : n.get("genre_ids")) {
                        String nombre = mapaGeneros.get(gid.asInt(-1));
                        if (nombre != null && !nombre.isBlank()) {
                            generos.add(nombre.trim());
                        }
                    }
                }
                p.setGeneros(generos);
                if (!p.getTitulo().isEmpty()) {
                    out.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Error TMDB busqueda: " + e.getMessage());
        }
        return out;
    }

    /**
     * Busca una película por título/año y devuelve métricas agregadas de TMDB.
     */
    public TmdbMetric obtenerMetricasPelicula(String titulo, int anio) {
        if (!estaConfigurado() || titulo == null || titulo.isBlank()) {
            return null;
        }
        String lang = obtenerIdiomaPorDefecto();
        String q = URLEncoder.encode(titulo.trim(), StandardCharsets.UTF_8);
        StringBuilder url = new StringBuilder(BASE_URL)
                .append("/search/movie?language=").append(lang)
                .append("&include_adult=false&page=1&query=").append(q);
        if (anio > 0) {
            url.append("&year=").append(anio);
        }

        JsonNode root = getJson(url.toString());
        if (root == null || !root.has("results")) {
            return null;
        }
        JsonNode mejor = null;
        for (JsonNode n : root.get("results")) {
            if (mejor == null) {
                mejor = n;
            }
            if (anio > 0 && extraerAnio(n.path("release_date").asText("")) == anio) {
                mejor = n;
                break;
            }
        }
        if (mejor == null) {
            return null;
        }
        return new TmdbMetric(
                mejor.path("id").asInt(0),
                mejor.path("vote_average").asDouble(0.0),
                mejor.path("vote_count").asInt(0),
                mejor.path("popularity").asDouble(0.0),
                construirPosterUrl(mejor.path("poster_path").asText("")));
    }

    private Map<Integer, String> obtenerMapaGeneros(String idioma) {
        String lang = normalizarIdioma(idioma);
        if (cacheGenerosPorIdioma.containsKey(lang)) {
            return cacheGenerosPorIdioma.get(lang);
        }
        Map<Integer, String> cacheGeneros = new HashMap<>();
        String url = BASE_URL + "/genre/movie/list?language=" + lang;
        JsonNode root = getJson(url);
        if (root == null || !root.has("genres")) {
            cacheGenerosPorIdioma.put(lang, cacheGeneros);
            return cacheGeneros;
        }
        for (JsonNode g : root.get("genres")) {
            int id = g.path("id").asInt(-1);
            String name = g.path("name").asText("");
            if (id > 0 && !name.isBlank()) {
                cacheGeneros.put(id, name);
            }
        }
        cacheGenerosPorIdioma.put(lang, cacheGeneros);
        return cacheGeneros;
    }

    private JsonNode getJson(String url) {
        String token = obtenerToken();
        if (token.isBlank()) {
            return null;
        }
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        try {
            HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() / 100 != 2) {
                System.err.println("TMDB status " + r.statusCode() + " para " + url);
                return null;
            }
            return mapper.readTree(r.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            System.err.println("Error HTTP TMDB: " + e.getMessage());
            return null;
        }
    }

    private static int extraerAnio(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4) {
            return 0;
        }
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String construirPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }
        return BASE_IMG + posterPath.trim();
    }

    private static String obtenerToken() {
        String env = System.getenv("TMDB_BEARER_TOKEN");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String prop = System.getProperty("tmdb.bearer.token");
        return prop == null ? "" : prop.trim();
    }

    private static String obtenerIdiomaPorDefecto() {
        String env = System.getenv("TMDB_LANG");
        if (env != null && !env.isBlank()) {
            return normalizarIdioma(env);
        }
        String prop = System.getProperty("tmdb.lang");
        if (prop != null && !prop.isBlank()) {
            return normalizarIdioma(prop);
        }
        return LANG_DEFAULT;
    }

    private static String normalizarIdioma(String idioma) {
        if (idioma == null || idioma.isBlank()) {
            return LANG_DEFAULT;
        }
        String s = idioma.trim();
        return switch (s) {
            case "en", "en_US", "en-US" -> "en-US";
            case "es", "es_ES", "es-ES" -> "es-ES";
            default -> LANG_DEFAULT;
        };
    }
}
