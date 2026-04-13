package moviematch.servicio;

import moviematch.dao.CalificacionDAO;
import moviematch.dao.GeneroDAO;
import moviematch.dao.MetricasExternasDAO;
import moviematch.dao.PeliculaDAO;
import moviematch.dao.PreferenciaDAO;
import moviematch.modelo.Pelicula;
import moviematch.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servicio de recomendación: combina géneros preferidos, popularidad y calificación media,
 * excluyendo títulos ya valorados o guardados en la lista personal.
 */
public class ServicioRecomendacion {

    private static final int LIMITE_RECOMENDACIONES = 10;
    private static final int LIMITE_CANDIDATAS = 80;
    private static final int CACHE_HORAS = 24;

    /**
     * Películas alineadas con los géneros marcados en {@code preferences} para el usuario.
     */
    private static final String SQL_CON_PREFERENCIAS = """
            SELECT m.movie_id, m.title, m.description, m.release_year, m.director, m.popularity, m.poster_url,
                   COALESCE((
                       SELECT AVG(r.rating) FROM ratings r WHERE r.movie_id = m.movie_id
                   ), 0) AS avg_rating
            FROM movies m
            WHERE EXISTS (
                SELECT 1 FROM movie_genres mg
                INNER JOIN preferences p ON mg.genre_id = p.genre_id AND p.user_id = ?
                WHERE mg.movie_id = m.movie_id
            )
            AND m.movie_id NOT IN (SELECT movie_id FROM ratings WHERE user_id = ?)
            AND m.movie_id NOT IN (SELECT movie_id FROM watchlist WHERE user_id = ?)
            ORDER BY m.popularity DESC, avg_rating DESC
            LIMIT """ + " " + LIMITE_CANDIDATAS;

    /**
     * Si no hay filas en {@code preferences}, se usa el catálogo completo con las mismas exclusiones.
     */
    private static final String SQL_SIN_PREFERENCIAS = """
            SELECT m.movie_id, m.title, m.description, m.release_year, m.director, m.popularity, m.poster_url,
                   COALESCE((
                       SELECT AVG(r.rating) FROM ratings r WHERE r.movie_id = m.movie_id
                   ), 0) AS avg_rating
            FROM movies m
            WHERE m.movie_id NOT IN (SELECT movie_id FROM ratings WHERE user_id = ?)
            AND m.movie_id NOT IN (SELECT movie_id FROM watchlist WHERE user_id = ?)
            ORDER BY m.popularity DESC, avg_rating DESC
            LIMIT """ + " " + LIMITE_CANDIDATAS;

    private final PreferenciaDAO preferenciaDAO = new PreferenciaDAO();
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final CalificacionDAO calificacionDAO = new CalificacionDAO();
    private final TmdbService tmdbService = new TmdbService();
    private final MetricasExternasDAO metricasExternasDAO = new MetricasExternasDAO();

    public record RecomendacionExplicada(Pelicula pelicula, String motivo) {
    }

    /**
     * Devuelve hasta 10 películas ordenadas primero por {@code popularity} descendente
     * y luego por promedio de calificaciones descendente.
     * <p>
     * Si el usuario tiene géneros preferidos, solo entran películas que comparten al menos
     * uno de esos géneros (vía {@code movie_genres} y {@code preferences}).
     * Si no tiene preferencias registradas, se consideran todas las películas no excluidas.
     */
    public List<Pelicula> obtenerRecomendacionesParaUsuario(int usuarioId) {
        List<RecomendacionExplicada> detalladas = obtenerRecomendacionesExplicadasParaUsuario(usuarioId);
        List<Pelicula> out = new ArrayList<>();
        for (RecomendacionExplicada r : detalladas) {
            out.add(r.pelicula());
        }
        return out;
    }

    public List<RecomendacionExplicada> obtenerRecomendacionesExplicadasParaUsuario(int usuarioId) {
        List<Integer> generos = preferenciaDAO.obtenerPreferenciasPorUsuario(usuarioId);
        List<Pelicula> candidatas;
        if (generos.isEmpty()) {
            candidatas = consultar(SQL_SIN_PREFERENCIAS, usuarioId, false);
        } else {
            candidatas = consultar(SQL_CON_PREFERENCIAS, usuarioId, true);
        }
        return rankearConExplicaciones(candidatas, generos);
    }

    /**
     * @param tresParametros {@code true} = consulta con EXISTS (tres veces {@code user_id})
     */
    private List<Pelicula> consultar(String sql, int usuarioId, boolean tresParametros) {
        List<Pelicula> out = new ArrayList<>();
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (tresParametros) {
                ps.setInt(1, usuarioId);
                ps.setInt(2, usuarioId);
                ps.setInt(3, usuarioId);
            } else {
                ps.setInt(1, usuarioId);
                ps.setInt(2, usuarioId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapearPelicula(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener recomendaciones: " + e.getMessage());
        }
        return out;
    }

    private static Pelicula mapearPelicula(ResultSet rs) throws SQLException {
        int id = rs.getInt("movie_id");
        String titulo = rs.getString("title");
        String desc = rs.getString("description");
        int anio = rs.getObject("release_year") == null ? 0 : rs.getInt("release_year");
        String director = rs.getString("director");
        double pop = rs.getDouble("popularity");
        String posterUrl = rs.getString("poster_url");
        return new Pelicula(id, titulo, desc, anio, director, pop, posterUrl);
    }

    private List<RecomendacionExplicada> rankearConExplicaciones(List<Pelicula> candidatas, List<Integer> generosUsuario) {
        if (candidatas.isEmpty()) {
            return List.of();
        }
        Map<Integer, String> nombreGeneroPorId = new HashMap<>();
        for (Integer gid : generosUsuario) {
            var g = generoDAO.obtenerGeneroPorId(gid);
            if (g != null) {
                nombreGeneroPorId.put(gid, g.getNombre());
            }
        }
        Map<Integer, List<Integer>> generosPorPelicula = new HashMap<>();
        List<Candidato> evaluadas = new ArrayList<>();
        for (Pelicula p : candidatas) {
            List<Integer> generosPeli = peliculaDAO.obtenerIdsGenerosDePelicula(p.getId());
            generosPorPelicula.put(p.getId(), generosPeli);
            Candidato c = evaluarCandidato(p, generosUsuario, generosPeli, nombreGeneroPorId);
            if (c != null) {
                evaluadas.add(c);
            }
        }
        if (evaluadas.isEmpty()) {
            return List.of();
        }

        evaluadas.sort((a, b) -> Double.compare(b.score, a.score));
        List<RecomendacionExplicada> out = new ArrayList<>();
        Map<Integer, Integer> frecuenciaGeneros = new HashMap<>();
        List<Candidato> restantes = new ArrayList<>(evaluadas);
        while (!restantes.isEmpty() && out.size() < LIMITE_RECOMENDACIONES) {
            int mejorIdx = -1;
            double mejorAjustado = -1;
            for (int i = 0; i < restantes.size(); i++) {
                Candidato c = restantes.get(i);
                List<Integer> gen = generosPorPelicula.getOrDefault(c.pelicula.getId(), List.of());
                double penalizacion = 0.0;
                for (Integer g : gen) {
                    int f = frecuenciaGeneros.getOrDefault(g, 0);
                    penalizacion += Math.min(0.04 * f, 0.12);
                }
                double ajustado = c.score - penalizacion;
                if (ajustado > mejorAjustado) {
                    mejorAjustado = ajustado;
                    mejorIdx = i;
                }
            }
            Candidato elegido = restantes.remove(mejorIdx);
            out.add(new RecomendacionExplicada(elegido.pelicula, elegido.motivo));
            for (Integer g : generosPorPelicula.getOrDefault(elegido.pelicula.getId(), List.of())) {
                frecuenciaGeneros.put(g, frecuenciaGeneros.getOrDefault(g, 0) + 1);
            }
        }
        return out;
    }

    private Candidato evaluarCandidato(Pelicula p,
                                       List<Integer> generosUsuario,
                                       List<Integer> generosPeli,
                                       Map<Integer, String> nombreGeneroPorId) {
        double sPref = scorePreferencias(generosUsuario, generosPeli);
        double avgLocalRaw = calificacionDAO.obtenerPromedioCalificaciones(p.getId());
        double sLocal = scoreLocal(p, avgLocalRaw);
        double sNovedad = scoreNovedad(p.getAnioLanzamiento());
        DatosExternos datos = obtenerDatosExternos(p);
        MetricasExternasDAO.MetricasTmdb tm = datos.metricas();

        if (!esPosterValido(p.getPosterUrl()) && esPosterValido(datos.posterUrl())) {
            p.setPosterUrl(datos.posterUrl());
            peliculaDAO.actualizarPosterUrl(p.getId(), p.getPosterUrl());
        }
        if (!esPosterValido(p.getPosterUrl())) {
            return null;
        }

        double score;
        if (tm == null) {
            score = 0.45 * sPref + 0.45 * sLocal + 0.10 * sNovedad;
            return new Candidato(p, score, construirMotivo(p, generosUsuario, generosPeli, nombreGeneroPorId, avgLocalRaw, null));
        }
        double sTmdb = scoreTmdb(tm);
        score = 0.35 * sPref + 0.25 * sLocal + 0.30 * sTmdb + 0.10 * sNovedad;
        return new Candidato(p, score, construirMotivo(p, generosUsuario, generosPeli, nombreGeneroPorId, avgLocalRaw, tm));
    }

    private double scorePreferencias(List<Integer> generosUsuario, List<Integer> generosPeli) {
        if (generosUsuario == null || generosUsuario.isEmpty()) {
            return 0.5;
        }
        if (generosPeli == null || generosPeli.isEmpty()) {
            return 0.0;
        }
        int interseccion = 0;
        for (Integer g : generosUsuario) {
            if (generosPeli.contains(g)) {
                interseccion++;
            }
        }
        return clamp01((double) interseccion / (double) generosUsuario.size());
    }

    private double scoreLocal(Pelicula p, double promedioLocalRaw) {
        double pop = clamp01(p.getPopularidad() / 10.0);
        double avg = clamp01(promedioLocalRaw / 5.0);
        return 0.6 * pop + 0.4 * avg;
    }

    private double scoreTmdb(MetricasExternasDAO.MetricasTmdb tm) {
        double avg = clamp01(tm.voteAverage() / 10.0);
        double pop = clamp01(tm.popularity() / (tm.popularity() + 100.0));
        double confianza = clamp01(Math.log10(tm.voteCount() + 1.0) / 3.0);
        return (0.7 * avg + 0.3 * pop) * (0.5 + 0.5 * confianza);
    }

    private double scoreNovedad(int anio) {
        if (anio <= 0) {
            return 0.3;
        }
        int actual = java.time.Year.now().getValue();
        int edad = Math.max(0, actual - anio);
        return clamp01(1.0 - (edad / 30.0));
    }

    private DatosExternos obtenerDatosExternos(Pelicula p) {
        MetricasExternasDAO.MetricasTmdb cache = metricasExternasDAO.obtenerMetricasRecientes(p.getId(), CACHE_HORAS);
        if (cache != null) {
            return new DatosExternos(cache, p.getPosterUrl());
        }
        if (!tmdbService.estaConfigurado()) {
            return new DatosExternos(null, p.getPosterUrl());
        }
        TmdbService.TmdbMetric m = tmdbService.obtenerMetricasPelicula(p.getTitulo(), p.getAnioLanzamiento());
        if (m == null) {
            return new DatosExternos(null, p.getPosterUrl());
        }
        MetricasExternasDAO.MetricasTmdb adaptada = new MetricasExternasDAO.MetricasTmdb(
                m.tmdbMovieId(),
                m.voteAverage(),
                m.voteCount(),
                m.popularity());
        metricasExternasDAO.guardarOModificarMetricas(p.getId(), adaptada);
        return new DatosExternos(adaptada, m.posterUrl());
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static boolean esPosterValido(String posterUrl) {
        return posterUrl != null && !posterUrl.isBlank();
    }

    private String construirMotivo(Pelicula p,
                                   List<Integer> generosUsuario,
                                   List<Integer> generosPeli,
                                   Map<Integer, String> nombreGeneroPorId,
                                   double promedioLocalRaw,
                                   MetricasExternasDAO.MetricasTmdb tm) {
        List<String> partes = new ArrayList<>();
        if (generosUsuario != null && !generosUsuario.isEmpty() && generosPeli != null && !generosPeli.isEmpty()) {
            Set<String> coincidentes = new HashSet<>();
            for (Integer g : generosPeli) {
                if (generosUsuario.contains(g) && nombreGeneroPorId.containsKey(g)) {
                    coincidentes.add(nombreGeneroPorId.get(g));
                }
            }
            if (!coincidentes.isEmpty()) {
                partes.add("coincide en géneros: " + String.join(", ", coincidentes));
            }
        }
        if (promedioLocalRaw > 0) {
            partes.add(String.format("media local %.1f/5", promedioLocalRaw));
        }
        if (tm != null) {
            partes.add(String.format("TMDB %.1f/10 (%d votos)", tm.voteAverage(), tm.voteCount()));
        }
        if (p.getAnioLanzamiento() > 0) {
            int actual = java.time.Year.now().getValue();
            if (actual - p.getAnioLanzamiento() <= 5) {
                partes.add("novedad reciente");
            }
        }
        if (partes.isEmpty()) {
            partes.add(String.format("popularidad %.1f", p.getPopularidad()));
        }
        return String.join(" · ", partes);
    }

    private record Candidato(Pelicula pelicula, double score, String motivo) {
    }

    private record DatosExternos(MetricasExternasDAO.MetricasTmdb metricas, String posterUrl) {
    }
}
