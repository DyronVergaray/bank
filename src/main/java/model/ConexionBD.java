package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ============================================================
 * PATRÓN: SINGLETON
 * ============================================================
 * Garantiza una única instancia de conexión a la base de datos
 * en toda la aplicación. Evita múltiples conexiones simultáneas
 * y centraliza la gestión de acceso a SQL Server.
 *
 * Clase: ConexionBD
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class ConexionBD {

    // --- SINGLETON: instancia única ---
    private static ConexionBD instancia;

    // Objeto de conexión JDBC
    private Connection conexion;

    // Parámetros de conexión a SQL Server
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
          + "databaseName=prueba;"
          + "user=javausuario;"
          + "password=Java1234;"
          + "encrypt=false;"
          + "trustServerCertificate=true;";

    // --------------------------------------------------------
    // Constructor privado → nadie fuera puede instanciar esta clase
    // --------------------------------------------------------
    private ConexionBD() {
        conectar();
    }

    // --------------------------------------------------------
    // SINGLETON: punto de acceso global a la única instancia
    // Thread-safe con bloque sincronizado (double-checked locking)
    // --------------------------------------------------------
    public static ConexionBD getInstancia() {
        if (instancia == null) {
            synchronized (ConexionBD.class) {
                if (instancia == null) {
                    instancia = new ConexionBD();
                }
            }
        }
        return instancia;
    }

    // --------------------------------------------------------
    // Establece la conexión física a SQL Server
    // --------------------------------------------------------
    private void conectar() {
        try {
            // Cargar el driver de SQL Server (mssql-jdbc)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conexion = DriverManager.getConnection(URL);
            System.out.println("[ConexionBD] Conexión establecida con SQL Server.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ConexionBD] Driver no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ConexionBD] Error al conectar: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // Retorna la conexión activa.
    // Si está cerrada o nula, la restablece automáticamente.
    // --------------------------------------------------------
    public Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                System.out.println("[ConexionBD] Reconectando...");
                conectar();
            }
        } catch (SQLException e) {
            System.err.println("[ConexionBD] Error al verificar conexión: " + e.getMessage());
            conectar();
        }
        return conexion;
    }

    // --------------------------------------------------------
    // Cierra la conexión de forma segura
    // --------------------------------------------------------
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                instancia = null;
                System.out.println("[ConexionBD] Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("[ConexionBD] Error al cerrar: " + e.getMessage());
        }
    }
}
