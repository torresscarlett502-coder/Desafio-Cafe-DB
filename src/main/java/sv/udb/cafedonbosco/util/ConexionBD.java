package sv.udb.cafedonbosco.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto unico de obtencion de conexiones JDBC a MySQL.
 * <p>
 * La URL, el usuario y la contrasena pueden sobreescribirse con las
 * variables de entorno DB_URL, DB_USUARIO y DB_PASSWORD para evitar dejar
 * credenciales fijas en el codigo en un despliegue real; si no estan
 * definidas se usan los valores por defecto de desarrollo local.
 */
public final class ConexionBD {

    private static final String URL = System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mysql://localhost:3306/cafe_don_bosco"
                    + "?useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&allowPublicKeyRetrieval=true"
    );

    private static final String USUARIO = System.getenv().getOrDefault("DB_USUARIO", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    static {
        // En un servidor como Tomcat, el driver vive en WEB-INF/lib y se
        // carga con el classloader propio de la aplicacion; el registro
        // automatico via ServiceLoader que funciona en un classpath plano
        // no siempre se dispara ahi, y DriverManager.getConnection()
        // termina fallando con "No suitable driver found". Forzamos la
        // carga de la clase para que su bloque estatico se registre.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontro el driver JDBC de MySQL en el classpath: " + e);
        }
    }

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
