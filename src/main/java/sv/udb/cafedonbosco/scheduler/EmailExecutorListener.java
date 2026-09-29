package sv.udb.cafedonbosco.scheduler;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import sv.udb.cafedonbosco.util.EmailExecutor;

/** Arranca y detiene el pool de hilos de EmailServiceImpl junto con la aplicacion. */
@WebListener
public class EmailExecutorListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EmailExecutor.iniciar();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EmailExecutor.apagar();
    }
}
