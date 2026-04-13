package moviematch.modelo;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Entrada en la lista personal del usuario (tabla {@code watchlist}).
 * El estado puede ser {@code FAVORITE} o {@code WATCH_LATER}.
 */
public class ListaPersonal {

    private int id;
    private int usuarioId;
    private int peliculaId;
    private String estado;
    private Timestamp fechaAgregada;

    public ListaPersonal() {
    }

    public ListaPersonal(int id, int usuarioId, int peliculaId, String estado, Timestamp fechaAgregada) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.peliculaId = peliculaId;
        this.estado = estado;
        this.fechaAgregada = fechaAgregada;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Timestamp getFechaAgregada() {
        return fechaAgregada;
    }

    public void setFechaAgregada(Timestamp fechaAgregada) {
        this.fechaAgregada = fechaAgregada;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListaPersonal that = (ListaPersonal) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
