package sv.udb.cafedonbosco.scheduler;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.service.impl.VentaServiceImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Arranca junto con la aplicacion un hilo en segundo plano que revisa
 * periodicamente los pedidos EN_PREPARACION cuyo tiempo estimado ya se
 * cumplio y los mueve automaticamente a LISTO (ver
 * VentaService.procesarPreparacionesVencidas). Sin esto, un pedido se
 * quedaria en EN_PREPARACION para siempre hasta que un administrador lo
 * cambiara manualmente.
 */
@WebListener
public class VentaSchedulerListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(VentaSchedulerListener.class);
    private static final long INTERVALO_SEGUNDOS = 30;

    private ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        VentaService ventaService = new VentaServiceImpl();
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread hilo = new Thread(runnable, "venta-scheduler");
            hilo.setDaemon(true);
            return hilo;
        });
        executor.scheduleWithFixedDelay(() -> {
            try {
                int actualizadas = ventaService.procesarPreparacionesVencidas();
                if (actualizadas > 0) {
                    LOG.info("{} pedido(s) pasaron automaticamente de EN_PREPARACION a LISTO.", actualizadas);
                }
            } catch (RuntimeException e) {
                LOG.error("Error al procesar las preparaciones vencidas", e);
            }
        }, INTERVALO_SEGUNDOS, INTERVALO_SEGUNDOS, TimeUnit.SECONDS);
        LOG.info("Scheduler de estados de venta iniciado (cada {} s).", INTERVALO_SEGUNDOS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
