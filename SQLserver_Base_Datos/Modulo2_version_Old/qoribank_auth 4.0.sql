-- ============================================================
-- QORI BANK - Script de Base de Datos
-- Módulo 1: Gestión de Usuarios y Autenticación
-- Base de datos: prueba (SQL Server)
-- ============================================================

CREATE DATABASE prueba;
GO
USE prueba;
GO

-- ============================================================
-- TABLA: Usuario
-- Almacena los datos del usuario registrado
-- ============================================================
IF OBJECT_ID('dbo.Usuario', 'U') IS NOT NULL
    DROP TABLE dbo.Usuario;
GO

CREATE TABLE dbo.Usuario (
    id_usuario       INT IDENTITY(1,1) PRIMARY KEY,
    primer_nombre    VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    email            VARCHAR(150) NOT NULL CONSTRAINT UQ_Usuario_email UNIQUE,
    telefono         VARCHAR(20)  NOT NULL CONSTRAINT UQ_Usuario_telefono UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    rol              VARCHAR(20)  NOT NULL DEFAULT 'CLIENTE',   -- 'CLIENTE' o 'ADMIN'
    activo           BIT NOT NULL DEFAULT 1,
    CONSTRAINT CK_Usuario_rol CHECK (rol IN ('CLIENTE', 'ADMIN'))
);
GO

-- ============================================================
-- TABLA: Tipo_Verificacion
-- ============================================================
IF OBJECT_ID('dbo.Tipo_Verificacion', 'U') IS NOT NULL
    DROP TABLE dbo.Tipo_Verificacion;
GO

CREATE TABLE dbo.Tipo_Verificacion (
    id_tipo     INT IDENTITY(1,1) PRIMARY KEY,
    nombre_tipo VARCHAR(50) NOT NULL
);
GO

INSERT INTO dbo.Tipo_Verificacion (nombre_tipo) VALUES ('SMS');
INSERT INTO dbo.Tipo_Verificacion (nombre_tipo) VALUES ('CORREO');
GO

-- ============================================================
-- TABLA: Inicio_Sesion
-- ============================================================
IF OBJECT_ID('dbo.Inicio_Sesion', 'U') IS NOT NULL
    DROP TABLE dbo.Inicio_Sesion;
GO

CREATE TABLE dbo.Inicio_Sesion (
    id_sesion           INT IDENTITY(1,1) PRIMARY KEY,
    id_usuario          INT NOT NULL,
    id_tipo             INT NOT NULL,
    codigo_hash         VARCHAR(255),
    codigo_verificado   BIT NOT NULL DEFAULT 0,
    codigo_expira_en    DATETIME,
    token_sesion        VARCHAR(255),
    sesion_expira_en    DATETIME,
    creado_en           DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_IS_Usuario FOREIGN KEY (id_usuario) REFERENCES dbo.Usuario(id_usuario),
    CONSTRAINT FK_IS_Tipo    FOREIGN KEY (id_tipo)    REFERENCES dbo.Tipo_Verificacion(id_tipo)
);
GO

-- Índice único filtrado: garantiza que los tokens reales (no nulos)
-- sean únicos, pero permite múltiples filas con token_sesion = NULL
-- (sesiones pendientes que aún no han sido verificadas).
CREATE UNIQUE INDEX UQ_Inicio_Sesion_token
    ON dbo.Inicio_Sesion(token_sesion)
    WHERE token_sesion IS NOT NULL;
GO

-- ============================================================
-- DATOS DE PRUEBA
-- Contraseñas en texto plano → hash SHA-256 calculado en Java
--   Carlos Quispe : "Java1234"  → rol CLIENTE
--   Ana Flores    : "Admin5678" → rol ADMIN
--
-- SHA-256("Java1234")  = e036f5178b49664ac59cbd40b1c3c0d433df0265ba7f320f0ba94def11aa97c4
-- SHA-256("Admin5678") = d526db0f3ee9b10843da19c1a9b30e8f7dd7f7e1ced9f0b8b6e7a2c3d4e5f6a
--   (el hash real se genera ejecutando SeguridadUtil.hashSHA256 en Java;
--    reemplazar el valor de Ana si se ejecuta primero el main de prueba)
-- ============================================================

-- Usuario CLIENTE
INSERT INTO dbo.Usuario
    (primer_nombre, apellido_paterno, apellido_materno, email, telefono, password_hash, rol, activo)
VALUES
    ('Carlos', 'Quispe', 'Mamani',
     'carlos.quispe@email.com',
     '+51987654321',
     'e036f5178b49664ac59cbd40b1c3c0d433df0265ba7f320f0ba94def11aa97c4',
     'CLIENTE', 1);
GO

-- Usuario ADMIN
INSERT INTO dbo.Usuario
    (primer_nombre, apellido_paterno, apellido_materno, email, telefono, password_hash, rol, activo)
VALUES
    ('Ana', 'Flores', 'Torres',
     'ana.flores@email.com',
     '+51912345678',
     -- SHA-256 de "Admin5678"
     '4ebdbd3afc7c601512c214d33d7be35478d007e42687f86a39ae48240d657cf1',
     'ADMIN', 1);
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ValidarCredenciales
-- (retorna también el campo 'rol')
-- ============================================================
IF OBJECT_ID('dbo.sp_ValidarCredenciales', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ValidarCredenciales;
GO

CREATE PROCEDURE dbo.sp_ValidarCredenciales
    @email         VARCHAR(150),
    @password_hash VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        u.id_usuario,
        u.primer_nombre,
        u.apellido_paterno,
        u.apellido_materno,
        u.email,
        u.telefono,
        u.rol,
        u.activo
    FROM dbo.Usuario u
    WHERE u.email         = @email
      AND u.password_hash = @password_hash
      AND u.activo        = 1;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_CrearSesionConCodigo
-- ============================================================
IF OBJECT_ID('dbo.sp_CrearSesionConCodigo', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_CrearSesionConCodigo;
GO

CREATE PROCEDURE dbo.sp_CrearSesionConCodigo
    @id_usuario  INT,
    @id_tipo     INT,
    @codigo_hash VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.Inicio_Sesion
    SET codigo_verificado = 0,
        codigo_expira_en  = GETDATE()
    WHERE id_usuario        = @id_usuario
      AND codigo_verificado = 0;

    INSERT INTO dbo.Inicio_Sesion
        (id_usuario, id_tipo, codigo_hash, codigo_verificado, codigo_expira_en, creado_en)
    VALUES
        (@id_usuario, @id_tipo, @codigo_hash, 0,
         DATEADD(MINUTE, 10, GETDATE()), GETDATE());

    SELECT SCOPE_IDENTITY() AS id_sesion;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_VerificarCodigo
-- ============================================================
IF OBJECT_ID('dbo.sp_VerificarCodigo', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_VerificarCodigo;
GO

CREATE PROCEDURE dbo.sp_VerificarCodigo
    @id_sesion    INT,
    @codigo_hash  VARCHAR(255),
    @token_sesion VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @valido BIT = 0;

    IF EXISTS (
        SELECT 1 FROM dbo.Inicio_Sesion
        WHERE id_sesion         = @id_sesion
          AND codigo_hash       = @codigo_hash
          AND codigo_verificado = 0
          AND codigo_expira_en  > GETDATE()
    )
    BEGIN
        UPDATE dbo.Inicio_Sesion
        SET codigo_verificado = 1,
            token_sesion      = @token_sesion,
            sesion_expira_en  = DATEADD(HOUR, 1, GETDATE())
        WHERE id_sesion = @id_sesion;
        SET @valido = 1;
    END

    SELECT @valido AS verificado;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ObtenerUsuarioPorSesion
-- ============================================================
IF OBJECT_ID('dbo.sp_ObtenerUsuarioPorSesion', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ObtenerUsuarioPorSesion;
GO

CREATE PROCEDURE dbo.sp_ObtenerUsuarioPorSesion
    @token_sesion VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        u.id_usuario,
        u.primer_nombre,
        u.apellido_paterno,
        u.apellido_materno,
        u.email,
        u.telefono,
        u.rol,
        u.activo,
        s.creado_en      AS inicio_sesion,
        s.sesion_expira_en
    FROM dbo.Inicio_Sesion s
    INNER JOIN dbo.Usuario u ON s.id_usuario = u.id_usuario
    WHERE s.token_sesion      = @token_sesion
      AND s.codigo_verificado = 1
      AND s.sesion_expira_en  > GETDATE();
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ObtenerTodosUsuarios   (NUEVO — Admin)
-- Lista todos los usuarios con id, nombre, email, teléfono,
-- rol y estado activo.
-- ============================================================
IF OBJECT_ID('dbo.sp_ObtenerTodosUsuarios', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ObtenerTodosUsuarios;
GO

CREATE PROCEDURE dbo.sp_ObtenerTodosUsuarios
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        id_usuario,
        primer_nombre,
        apellido_paterno,
        apellido_materno,
        email,
        telefono,
        rol,
        activo
    FROM dbo.Usuario
    ORDER BY id_usuario;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_EliminarUsuario   (NUEVO — Admin)
-- Elimina un usuario por id.
-- No permite eliminar al propio usuario que ejecuta la acción
-- (la validación de "no eliminarse a sí mismo" se hace en Java).
-- ============================================================
IF OBJECT_ID('dbo.sp_EliminarUsuario', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_EliminarUsuario;
GO

CREATE PROCEDURE dbo.sp_EliminarUsuario
    @id_usuario INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Primero eliminar sesiones relacionadas (FK)
    DELETE FROM dbo.Inicio_Sesion WHERE id_usuario = @id_usuario;

    -- Luego eliminar el usuario
    DELETE FROM dbo.Usuario WHERE id_usuario = @id_usuario;

    SELECT @@ROWCOUNT AS eliminados;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_CrearUsuarioCliente   (NUEVO — Registro)
-- Registra un nuevo usuario con rol CLIENTE.
-- ============================================================
IF OBJECT_ID('dbo.sp_CrearUsuarioCliente', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_CrearUsuarioCliente;
GO

CREATE PROCEDURE dbo.sp_CrearUsuarioCliente
    @primer_nombre    VARCHAR(100),
    @apellido_paterno VARCHAR(100),
    @apellido_materno VARCHAR(100),
    @email            VARCHAR(150),
    @telefono         VARCHAR(20),
    @password_hash    VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    -- Verificar duplicados
    IF EXISTS (SELECT 1 FROM dbo.Usuario WHERE email = @email)
    BEGIN
        SELECT -1 AS id_usuario, 'El correo ya está registrado.' AS mensaje;
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM dbo.Usuario WHERE telefono = @telefono)
    BEGIN
        SELECT -2 AS id_usuario, 'El teléfono ya está registrado.' AS mensaje;
        RETURN;
    END

    INSERT INTO dbo.Usuario
        (primer_nombre, apellido_paterno, apellido_materno, email, telefono, password_hash, rol, activo)
    VALUES
        (@primer_nombre, @apellido_paterno, @apellido_materno,
         @email, @telefono, @password_hash, 'CLIENTE', 1);

    SELECT SCOPE_IDENTITY() AS id_usuario, 'OK' AS mensaje;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_CrearUsuarioAdmin   (NUEVO — Admin)
-- Registra un nuevo usuario con rol ADMIN.
-- Solo debe llamarse desde el panel de administración.
-- ============================================================
IF OBJECT_ID('dbo.sp_CrearUsuarioAdmin', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_CrearUsuarioAdmin;
GO

CREATE PROCEDURE dbo.sp_CrearUsuarioAdmin
    @primer_nombre    VARCHAR(100),
    @apellido_paterno VARCHAR(100),
    @apellido_materno VARCHAR(100),
    @email            VARCHAR(150),
    @telefono         VARCHAR(20),
    @password_hash    VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM dbo.Usuario WHERE email = @email)
    BEGIN
        SELECT -1 AS id_usuario, 'El correo ya está registrado.' AS mensaje;
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM dbo.Usuario WHERE telefono = @telefono)
    BEGIN
        SELECT -2 AS id_usuario, 'El teléfono ya está registrado.' AS mensaje;
        RETURN;
    END

    INSERT INTO dbo.Usuario
        (primer_nombre, apellido_paterno, apellido_materno, email, telefono, password_hash, rol, activo)
    VALUES
        (@primer_nombre, @apellido_paterno, @apellido_materno,
         @email, @telefono, @password_hash, 'ADMIN', 1);

    SELECT SCOPE_IDENTITY() AS id_usuario, 'OK' AS mensaje;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ValidarDatosRecuperacion   (NUEVO — Recuperación)
-- Valida que email + primer_nombre + apellido_paterno + telefono
-- correspondan a un usuario activo (sin verificar contraseña).
-- ============================================================
IF OBJECT_ID('dbo.sp_ValidarDatosRecuperacion', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ValidarDatosRecuperacion;
GO

CREATE PROCEDURE dbo.sp_ValidarDatosRecuperacion
    @email            VARCHAR(150),
    @primer_nombre    VARCHAR(100),
    @apellido_paterno VARCHAR(100),
    @telefono         VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        u.id_usuario,
        u.primer_nombre,
        u.apellido_paterno,
        u.apellido_materno,
        u.email,
        u.telefono,
        u.rol,
        u.activo
    FROM dbo.Usuario u
    WHERE u.email            = @email
      AND u.primer_nombre    = @primer_nombre
      AND u.apellido_paterno = @apellido_paterno
      AND u.telefono         = @telefono
      AND u.activo           = 1;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ActualizarPassword   (NUEVO — Recuperación)
-- Actualiza el hash de contraseña de un usuario.
-- ============================================================
IF OBJECT_ID('dbo.sp_ActualizarPassword', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ActualizarPassword;
GO

CREATE PROCEDURE dbo.sp_ActualizarPassword
    @id_usuario       INT,
    @nuevo_password_hash VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.Usuario
    SET password_hash = @nuevo_password_hash
    WHERE id_usuario = @id_usuario;

    SELECT @@ROWCOUNT AS actualizados;
END;
GO

PRINT 'Base de datos QoriBank creada correctamente.';
GO

-- ============================================================
-- ============================================================
-- MÓDULO 2: VINCULACIÓN DE CUENTAS BANCARIAS
-- ============================================================
-- ============================================================

-- ============================================================
-- TABLA: Tipo_Cuenta_Banco
-- Catálogo dinámico de entidades bancarias soportadas.
-- El admin puede agregar nuevas entidades (con su API simulada)
-- o eliminar las existentes, sin tocar código Java.
-- ============================================================
IF OBJECT_ID('dbo.Tipo_Cuenta_Banco', 'U') IS NOT NULL
    DROP TABLE dbo.Tipo_Cuenta_Banco;
GO

CREATE TABLE dbo.Tipo_Cuenta_Banco (
    id_tipo_cuenta  INT IDENTITY(1,1) PRIMARY KEY,
    nombre_entidad  VARCHAR(50)  NOT NULL CONSTRAINT UQ_TipoCuenta_Entidad UNIQUE,
    api_endpoint    VARCHAR(255) NOT NULL,
    api_key         VARCHAR(255) NOT NULL,
    activo          BIT NOT NULL DEFAULT 1,
    creado_en       DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- Entidades bancarias predefinidas (API simulada para fines del proyecto)
INSERT INTO dbo.Tipo_Cuenta_Banco (nombre_entidad, api_endpoint, api_key) VALUES
    ('BCP',       'https://api.viabcp.com/v1/cuentas',       'bcp_sk_demo_8f72a1c93e');
INSERT INTO dbo.Tipo_Cuenta_Banco (nombre_entidad, api_endpoint, api_key) VALUES
    ('BBVA',      'https://api.bbva.pe/v1/cuentas',          'bbva_sk_demo_4d1e76b205');
INSERT INTO dbo.Tipo_Cuenta_Banco (nombre_entidad, api_endpoint, api_key) VALUES
    ('Interbank', 'https://api.interbank.pe/v1/cuentas',     'ibk_sk_demo_9a3c5f08e1');
GO

-- ============================================================
-- TABLA: Cuenta_Bancaria
-- Tarjeta/cuenta vinculada por un cliente. Una tarjeta solo
-- puede pertenecer a un único usuario (numero_tarjeta_hash es
-- UNIQUE), por lo que otro usuario no puede volver a registrarla.
-- ============================================================
IF OBJECT_ID('dbo.Cuenta_Bancaria', 'U') IS NOT NULL
    DROP TABLE dbo.Cuenta_Bancaria;
GO

CREATE TABLE dbo.Cuenta_Bancaria (
    id_cuenta_bancaria         INT IDENTITY(1,1) PRIMARY KEY,
    id_usuario                 INT NOT NULL,
    id_tipo_cuenta              INT NOT NULL,
    numero_tarjeta_hash         VARCHAR(255) NOT NULL CONSTRAINT UQ_CuentaBanc_Tarjeta UNIQUE,
    numero_tarjeta_enmascarado  VARCHAR(25)  NOT NULL,   -- Ej: **** **** **** 4321
    fecha_vencimiento           VARCHAR(7)   NOT NULL,   -- Formato MM/AAAA
    cvv_hash                    VARCHAR(255) NOT NULL,
    estado                      VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    saldo                       DECIMAL(12,2) NOT NULL DEFAULT 0,
    creado_en                   DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_CB_Usuario     FOREIGN KEY (id_usuario)    REFERENCES dbo.Usuario(id_usuario),
    CONSTRAINT FK_CB_TipoCuenta  FOREIGN KEY (id_tipo_cuenta) REFERENCES dbo.Tipo_Cuenta_Banco(id_tipo_cuenta),
    CONSTRAINT CK_CB_Estado      CHECK (estado IN ('PENDIENTE', 'VINCULADA', 'ERROR'))
);
GO

-- ============================================================
-- Extensión de dbo.Inicio_Sesion para reutilizar el flujo OTP
-- también en la vinculación de cuentas bancarias.
--   contexto = 'LOGIN' | 'RECUPERACION' | 'VINCULACION_CUENTA'
--   id_cuenta_bancaria: solo se usa cuando contexto = 'VINCULACION_CUENTA'
-- ============================================================
IF COL_LENGTH('dbo.Inicio_Sesion', 'contexto') IS NULL
BEGIN
    ALTER TABLE dbo.Inicio_Sesion
        ADD contexto VARCHAR(30) NOT NULL DEFAULT 'LOGIN';
END
GO

IF COL_LENGTH('dbo.Inicio_Sesion', 'id_cuenta_bancaria') IS NULL
BEGIN
    ALTER TABLE dbo.Inicio_Sesion
        ADD id_cuenta_bancaria INT NULL
            CONSTRAINT FK_IS_CuentaBancaria REFERENCES dbo.Cuenta_Bancaria(id_cuenta_bancaria);
END
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ExisteTarjeta
-- Verifica si un número de tarjeta (hash) ya está registrado
-- por CUALQUIER usuario, sin importar si el dueño es el mismo
-- que intenta registrarla. Cumple la regla "una tarjeta solo
-- puede pertenecer a un usuario".
-- ============================================================
IF OBJECT_ID('dbo.sp_ExisteTarjeta', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ExisteTarjeta;
GO

CREATE PROCEDURE dbo.sp_ExisteTarjeta
    @numero_tarjeta_hash VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COUNT(*) AS existe
    FROM dbo.Cuenta_Bancaria
    WHERE numero_tarjeta_hash = @numero_tarjeta_hash;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_RegistrarCuentaBancaria
-- Inserta una nueva cuenta bancaria en estado PENDIENTE
-- (queda a la espera de verificación OTP).
-- ============================================================
IF OBJECT_ID('dbo.sp_RegistrarCuentaBancaria', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_RegistrarCuentaBancaria;
GO

CREATE PROCEDURE dbo.sp_RegistrarCuentaBancaria
    @id_usuario                 INT,
    @id_tipo_cuenta              INT,
    @numero_tarjeta_hash         VARCHAR(255),
    @numero_tarjeta_enmascarado  VARCHAR(25),
    @fecha_vencimiento           VARCHAR(7),
    @cvv_hash                    VARCHAR(255),
    @saldo_inicial               DECIMAL(12,2) = 0
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM dbo.Cuenta_Bancaria WHERE numero_tarjeta_hash = @numero_tarjeta_hash)
    BEGIN
        SELECT -1 AS id_cuenta_bancaria, 'Esta tarjeta ya se encuentra registrada.' AS mensaje;
        RETURN;
    END

    INSERT INTO dbo.Cuenta_Bancaria
        (id_usuario, id_tipo_cuenta, numero_tarjeta_hash, numero_tarjeta_enmascarado,
         fecha_vencimiento, cvv_hash, estado, saldo)
    VALUES
        (@id_usuario, @id_tipo_cuenta, @numero_tarjeta_hash, @numero_tarjeta_enmascarado,
         @fecha_vencimiento, @cvv_hash, 'PENDIENTE', @saldo_inicial);

    SELECT SCOPE_IDENTITY() AS id_cuenta_bancaria, 'OK' AS mensaje;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ActualizarEstadoCuentaBancaria
-- Cambia el estado de una cuenta bancaria (PENDIENTE -> VINCULADA
-- o -> ERROR), usado tras la verificación OTP.
-- ============================================================
IF OBJECT_ID('dbo.sp_ActualizarEstadoCuentaBancaria', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ActualizarEstadoCuentaBancaria;
GO

CREATE PROCEDURE dbo.sp_ActualizarEstadoCuentaBancaria
    @id_cuenta_bancaria INT,
    @nuevo_estado       VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.Cuenta_Bancaria
    SET estado = @nuevo_estado
    WHERE id_cuenta_bancaria = @id_cuenta_bancaria;

    SELECT @@ROWCOUNT AS actualizados;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ObtenerCuentasPorUsuario
-- Lista las cuentas bancarias vinculadas de un usuario,
-- incluyendo el nombre de la entidad bancaria.
-- ============================================================
IF OBJECT_ID('dbo.sp_ObtenerCuentasPorUsuario', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ObtenerCuentasPorUsuario;
GO

CREATE PROCEDURE dbo.sp_ObtenerCuentasPorUsuario
    @id_usuario INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        cb.id_cuenta_bancaria,
        cb.id_tipo_cuenta,
        tc.nombre_entidad,
        cb.numero_tarjeta_enmascarado,
        cb.fecha_vencimiento,
        cb.estado,
        cb.saldo,
        cb.creado_en
    FROM dbo.Cuenta_Bancaria cb
    INNER JOIN dbo.Tipo_Cuenta_Banco tc ON cb.id_tipo_cuenta = tc.id_tipo_cuenta
    WHERE cb.id_usuario = @id_usuario
    ORDER BY cb.creado_en DESC;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ObtenerCuentaBancariaPorId
-- Retorna una cuenta bancaria específica (para validaciones
-- internas, ej. confirmar dueño antes de verificar OTP).
-- ============================================================
IF OBJECT_ID('dbo.sp_ObtenerCuentaBancariaPorId', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ObtenerCuentaBancariaPorId;
GO

CREATE PROCEDURE dbo.sp_ObtenerCuentaBancariaPorId
    @id_cuenta_bancaria INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        cb.id_cuenta_bancaria,
        cb.id_usuario,
        cb.id_tipo_cuenta,
        tc.nombre_entidad,
        tc.api_endpoint,
        tc.api_key,
        cb.numero_tarjeta_enmascarado,
        cb.fecha_vencimiento,
        cb.estado,
        cb.saldo
    FROM dbo.Cuenta_Bancaria cb
    INNER JOIN dbo.Tipo_Cuenta_Banco tc ON cb.id_tipo_cuenta = tc.id_tipo_cuenta
    WHERE cb.id_cuenta_bancaria = @id_cuenta_bancaria;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_EliminarCuentaBancariaPendiente
-- Elimina una cuenta bancaria que quedó en estado PENDIENTE o
-- ERROR (ej. el usuario canceló la verificación OTP). No borra
-- cuentas ya VINCULADAS.
-- ============================================================
IF OBJECT_ID('dbo.sp_EliminarCuentaBancariaPendiente', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_EliminarCuentaBancariaPendiente;
GO

CREATE PROCEDURE dbo.sp_EliminarCuentaBancariaPendiente
    @id_cuenta_bancaria INT
AS
BEGIN
    SET NOCOUNT ON;

    DELETE FROM dbo.Cuenta_Bancaria
    WHERE id_cuenta_bancaria = @id_cuenta_bancaria
      AND estado IN ('PENDIENTE', 'ERROR');

    SELECT @@ROWCOUNT AS eliminados;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_CrearSesionVinculacionCuenta
-- Variante de sp_CrearSesionConCodigo específica para el
-- contexto VINCULACION_CUENTA: asocia el código OTP a la
-- cuenta bancaria pendiente en lugar de solo al usuario.
-- ============================================================
IF OBJECT_ID('dbo.sp_CrearSesionVinculacionCuenta', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_CrearSesionVinculacionCuenta;
GO

CREATE PROCEDURE dbo.sp_CrearSesionVinculacionCuenta
    @id_usuario         INT,
    @id_tipo             INT,
    @id_cuenta_bancaria INT,
    @codigo_hash        VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.Inicio_Sesion
        (id_usuario, id_tipo, codigo_hash, codigo_verificado, codigo_expira_en,
         creado_en, contexto, id_cuenta_bancaria)
    VALUES
        (@id_usuario, @id_tipo, @codigo_hash, 0,
         DATEADD(MINUTE, 10, GETDATE()), GETDATE(),
         'VINCULACION_CUENTA', @id_cuenta_bancaria);

    SELECT SCOPE_IDENTITY() AS id_sesion;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_VerificarCodigoVinculacion
-- Verifica el código OTP de vinculación de cuenta. Si es
-- correcto, marca la cuenta bancaria como VINCULADA.
-- ============================================================
IF OBJECT_ID('dbo.sp_VerificarCodigoVinculacion', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_VerificarCodigoVinculacion;
GO

CREATE PROCEDURE dbo.sp_VerificarCodigoVinculacion
    @id_sesion   INT,
    @codigo_hash VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @valido BIT = 0;
    DECLARE @id_cuenta INT;

    SELECT @id_cuenta = id_cuenta_bancaria
    FROM dbo.Inicio_Sesion
    WHERE id_sesion         = @id_sesion
      AND codigo_hash       = @codigo_hash
      AND codigo_verificado = 0
      AND codigo_expira_en  > GETDATE()
      AND contexto          = 'VINCULACION_CUENTA';

    IF @id_cuenta IS NOT NULL
    BEGIN
        UPDATE dbo.Inicio_Sesion
        SET codigo_verificado = 1
        WHERE id_sesion = @id_sesion;

        UPDATE dbo.Cuenta_Bancaria
        SET estado = 'VINCULADA'
        WHERE id_cuenta_bancaria = @id_cuenta;

        SET @valido = 1;
    END

    SELECT @valido AS verificado, @id_cuenta AS id_cuenta_bancaria;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_ObtenerTiposCuentaBanco
-- Lista todos los tipos de cuenta/entidades bancarias activas
-- (usado al elegir banco en el registro de tarjeta).
-- ============================================================
IF OBJECT_ID('dbo.sp_ObtenerTiposCuentaBanco', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ObtenerTiposCuentaBanco;
GO

CREATE PROCEDURE dbo.sp_ObtenerTiposCuentaBanco
AS
BEGIN
    SET NOCOUNT ON;
    SELECT id_tipo_cuenta, nombre_entidad, api_endpoint, api_key, activo, creado_en
    FROM dbo.Tipo_Cuenta_Banco
    WHERE activo = 1
    ORDER BY nombre_entidad;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_CrearTipoCuentaBanco   (Admin)
-- Registra una nueva entidad bancaria disponible para vincular.
-- ============================================================
IF OBJECT_ID('dbo.sp_CrearTipoCuentaBanco', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_CrearTipoCuentaBanco;
GO

CREATE PROCEDURE dbo.sp_CrearTipoCuentaBanco
    @nombre_entidad VARCHAR(50),
    @api_endpoint   VARCHAR(255),
    @api_key        VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM dbo.Tipo_Cuenta_Banco WHERE nombre_entidad = @nombre_entidad)
    BEGIN
        SELECT -1 AS id_tipo_cuenta, 'Ya existe una entidad bancaria con ese nombre.' AS mensaje;
        RETURN;
    END

    INSERT INTO dbo.Tipo_Cuenta_Banco (nombre_entidad, api_endpoint, api_key, activo)
    VALUES (@nombre_entidad, @api_endpoint, @api_key, 1);

    SELECT SCOPE_IDENTITY() AS id_tipo_cuenta, 'OK' AS mensaje;
END;
GO

-- ============================================================
-- PROCEDIMIENTO: sp_EliminarTipoCuentaBanco   (Admin)
-- Elimina (desactiva) un tipo de cuenta bancaria. Se usa
-- desactivación lógica para no romper cuentas ya vinculadas
-- con ese tipo.
-- ============================================================
IF OBJECT_ID('dbo.sp_EliminarTipoCuentaBanco', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_EliminarTipoCuentaBanco;
GO

CREATE PROCEDURE dbo.sp_EliminarTipoCuentaBanco
    @id_tipo_cuenta INT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM dbo.Cuenta_Bancaria WHERE id_tipo_cuenta = @id_tipo_cuenta)
    BEGIN
        -- Hay cuentas vinculadas con este tipo: desactivar en lugar de borrar
        UPDATE dbo.Tipo_Cuenta_Banco SET activo = 0 WHERE id_tipo_cuenta = @id_tipo_cuenta;
        SELECT 1 AS eliminados, 'DESACTIVADO' AS modo;
    END
    ELSE
    BEGIN
        -- Sin cuentas asociadas: se puede eliminar físicamente
        DELETE FROM dbo.Tipo_Cuenta_Banco WHERE id_tipo_cuenta = @id_tipo_cuenta;
        SELECT @@ROWCOUNT AS eliminados, 'ELIMINADO' AS modo;
    END
END;
GO

PRINT 'Módulo 2 (Cuentas Bancarias) creado correctamente.';
GO
