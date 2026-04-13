package moviematch.modelo;

import java.util.Objects;

/**
 * Representa una película (tabla {@code movies}).
 */
public class Pelicula {

    private int id;
    private String titulo;
    private String descripcion;
    private int anioLanzamiento;
    private String director;
    private double popularidad;
    private String posterUrl;

    public Pelicula() {
    }

    public Pelicula(int id, String titulo, String descripcion, int anioLanzamiento,
                    String director, double popularidad) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.anioLanzamiento = anioLanzamiento;
        this.director = director;
        this.popularidad = popularidad;
    }

    public Pelicula(int id, String titulo, String descripcion, int anioLanzamiento,
                    String director, double popularidad, String posterUrl) {
        this(id, titulo, descripcion, anioLanzamiento, director, popularidad);
        this.posterUrl = posterUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pelicula pelicula = (Pelicula) o;
        return id == pelicula.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        if (titulo == null || titulo.isEmpty()) {
            return "(sin título)";
        }
        if (id > 0) {
            return titulo + " (#" + id + ")";
        }
        return titulo;
    }
}
