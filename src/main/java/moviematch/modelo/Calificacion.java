package moviematch.modelo;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Representa la calificación de una película por un usuario (tabla {@code ratings}).
 */
public class Calificacion {

    private int id;
    private int usuarioId;
    private int peliculaId;
    /** Puntuación de 1 a 5. */
    private int puntuacion;
    private String resena;
    private Timestamp fechaCalificacion;

    public Calificacion() {
    }

    public Calificacion(int id, int usuarioId, int peliculaId, int puntuacion,
                        String resena, Timestamp fechaCalificacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.peliculaId = peliculaId;
        this.puntuacion = puntuacion;
        this.resena = resena;
        this.fechaCalificacion = fechaCalificacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getPeliculaId() {
        return peliculaId;
    }

    public void setPeliculaId(int peliculaId) {
        this.peliculaId = peliculaId;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getResena() {
        return resena;
    }

    public void setResena(String resena) {
        this.resena = resena;
    }

    public Timestamp getFechaCalificacion() {
        return fechaCalificacion;
    }

    public void setFechaCalificacion(Timestamp fechaCalificacion) {
        this.fechaCalificacion = fechaCalificacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Calificacion that = (Calificacion) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
