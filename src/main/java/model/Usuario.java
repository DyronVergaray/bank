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
    private int    idUsuario;
    private String primerNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String email;
    private String telefono;
    private String passwordHash;
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
                   String passwordHash, boolean activo) {
        this.idUsuario       = idUsuario;
        this.primerNombre    = primerNombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.email           = email;
        this.telefono        = telefono;
        this.passwordHash    = passwordHash;
        this.activo          = activo;
    }

    // --------------------------------------------------------
    // PROTOTYPE: clonar perfil de usuario
    // Retorna una copia profunda del objeto actual
    // --------------------------------------------------------
    @Override
    public Usuario clone() {
        try {
            return (Usuario) super.clone();
        } catch (CloneNotSupportedException e) {
            // Nunca ocurrirá ya que implementamos Cloneable
            throw new RuntimeException("[Usuario] Error al clonar perfil: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // Método utilitario: nombre completo formateado
    // --------------------------------------------------------
    public String getNombreCompleto() {
        return primerNombre + " " + apellidoPaterno
               + (apellidoMaterno != null && !apellidoMaterno.isEmpty()
                  ? " " + apellidoMaterno : "");
    }

    // --------------------------------------------------------
    // Getters y Setters
    // --------------------------------------------------------
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario
               + ", nombre='" + getNombreCompleto() + "'"
               + ", email='" + email + "'"
               + ", activo=" + activo + "}";
    }
}
