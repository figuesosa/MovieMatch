package moviematch.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de busqueda remota (TMDB) para importar al catalogo local.
 */
public class PeliculaExterna {

    private int tmdbId;
    private String titulo;
    private String descripcion;
    private int anioLanzamiento;
    private double popularidad;
    private String posterUrl;
    private List<String> generos = new ArrayList<>();

    public int getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getAnioLanzamiento() {
        return anioLanzamiento;
    }

    public void setAnioLanzamiento(int anioLanzamiento) {
        this.anioLanzamiento = anioLanzamiento;
    }

    public double getPopularidad() {
        return popularidad;
    }

    public void setPopularidad(double popularidad) {
        this.popularidad = popularidad;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public List<String> getGeneros() {
        return generos;
    }

    public void setGeneros(List<String> generos) {
        this.generos = generos;
    }

    @Override
    public String toString() {
        String anio = anioLanzamiento > 0 ? String.valueOf(anioLanzamiento) : "s/d";
        return titulo + " (" + anio + ")";
    }
}
