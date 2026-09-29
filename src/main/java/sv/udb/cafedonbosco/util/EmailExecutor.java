package sv.udb.cafedonbosco.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Pool de hilos compartido para tareas de correo: enviar un email por SMTP
 * puede tardar varios segundos, y ninguna solicitud HTTP (por ejemplo, el
 * checkout) debe quedar bloqueada esperando esa latencia. El ciclo de vida
 * lo controla VentaSchedulerListener-style un ServletContextListener
 * (EmailExecutorListener) para que los hilos se cierren limpiamente al
 * detener la aplicacion.
 */
public final class EmailExecutor {

    private static volatile ExecutorService executor;

    private EmailExecutor() {
    }

    public static synchronized void iniciar() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(2, runnable -> {
                Thread hilo = new Thread(runnable, "email-worker");
                hilo.setDaemon(true);
                return hilo;
            });
        }
    }

    public static synchronized void apagar() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
    }

    /** Si el listener no llego a iniciar el pool (p. ej. en una prueba), igual envia la tarea en un hilo aparte. */
    public static void ejecutar(Runnable tarea) {
        ExecutorService actual = executor;
        if (actual != null && !actual.isShutdown()) {
            actual.submit(tarea);
        } else {
            Thread hilo = new Thread(tarea, "email-worker-fallback");
            hilo.setDaemon(true);
            hilo.start();
        }
    }
}
