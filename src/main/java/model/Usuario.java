package model;

/**
 * ============================================================
 * PATRÓN: PROTOTYPE
 * ============================================================
 * La clase Usuario implementa Cloneable para permitir duplicar
 * configuraciones de perfil de usuario. Útil cuando se necesita
 * crear usuarios con datos similares (ej. usuarios de prueba,
 * perfiles temporales durante el flujo de autenticación).
 *
 * Clase: Usuario
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class Usuario implements Cloneable {

    // --- Atributos mapeados a la tabla dbo.Usuario ---
    private int     idUsuario;
    private String  primerNombre;
    private String  apellidoPaterno;
    private String  apellidoMaterno;
    private String  email;
    private String  telefono;
    private String  passwordHash;
    private String  rol;      // 'CLIENTE' o 'ADMIN'
    private boolean activo;

    // --------------------------------------------------------
    // Constructor vacío
    // --------------------------------------------------------
    public Usuario() {}

    // --------------------------------------------------------
    // Constructor completo
    // --------------------------------------------------------
    public Usuario(int idUsuario, String primerNombre, String apellidoPaterno,
                   String apellidoMaterno, String email, String telefono,
                   String passwordHash, String rol, boolean activo) {
        this.idUsuario       = idUsuario;
        this.primerNombre    = primerNombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.email           = email;
        this.telefono        = telefono;
        this.passwordHash    = passwordHash;
        this.rol             = rol;
        this.activo          = activo;
    }

    // --------------------------------------------------------
    // PROTOTYPE: clonar perfil de usuario
    // --------------------------------------------------------
    @Override
    public Usuario clone() {
        try {
            return (Usuario) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("[Usuario] Error al clonar perfil: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // Helpers
    // --------------------------------------------------------
    public String getNombreCompleto() {
        return primerNombre + " " + apellidoPaterno
               + (apellidoMaterno != null && !apellidoMaterno.isEmpty()
                  ? " " + apellidoMaterno : "");
    }

    public boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(rol);
    }

    // --------------------------------------------------------
    // Getters y Setters
    // --------------------------------------------------------
    public int getIdUsuario()                       { return idUsuario; }
    public void setIdUsuario(int idUsuario)         { this.idUsuario = idUsuario; }

    public String getPrimerNombre()                 { return primerNombre; }
    public void setPrimerNombre(String v)           { this.primerNombre = v; }

    public String getApellidoPaterno()              { return apellidoPaterno; }
    public void setApellidoPaterno(String v)        { this.apellidoPaterno = v; }

    public String getApellidoMaterno()              { return apellidoMaterno; }
    public void setApellidoMaterno(String v)        { this.apellidoMaterno = v; }

    public String getEmail()                        { return email; }
    public void setEmail(String v)                  { this.email = v; }

    public String getTelefono()                     { return telefono; }
    public void setTelefono(String v)               { this.telefono = v; }

    public String getPasswordHash()                 { return passwordHash; }
    public void setPasswordHash(String v)           { this.passwordHash = v; }

    public String getRol()                          { return rol; }
    public void setRol(String rol)                  { this.rol = rol; }

    public boolean isActivo()                       { return activo; }
    public void setActivo(boolean activo)           { this.activo = activo; }

    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario
               + ", nombre='" + getNombreCompleto() + "'"
               + ", email='" + email + "'"
               + ", rol='" + rol + "'"
               + ", activo=" + activo + "}";
    }
}
