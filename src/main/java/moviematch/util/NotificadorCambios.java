package moviematch.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Permite que varias pestañas refresquen datos cuando otra modifica la base.
 */
public final class NotificadorCambios {

    private final List<Runnable> suscriptores = new CopyOnWriteArrayList<>();

    public void suscribir(Runnable r) {
        if (r != null) {
            suscriptores.add(r);
        }
    }

    public void notificar() {
        for (Runnable r : suscriptores) {
            r.run();
        }
    }
}
