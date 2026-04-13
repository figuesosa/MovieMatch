package moviematch.modelo;

import java.util.Objects;

/**
 * Representa un género cinematográfico (tabla {@code genres}).
 */
public class Genero {

    private int id;
    private String nombre;

    public Genero() {
    }

    public Genero(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Genero genero = (Genero) o;
        return id == genero.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre != null && !nombre.isEmpty() ? nombre : "(sin nombre)";
    }
}
