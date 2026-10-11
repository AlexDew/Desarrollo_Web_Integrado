IF DB_ID('turismo_piura') IS NULL
    CREATE DATABASE turismo_piura;
GO

USE turismo_piura;
GO

-- ---------- USUARIOS ----------
IF OBJECT_ID('dbo.usuarios', 'U') IS NULL
CREATE TABLE dbo.usuarios (
    id            BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activo        BIT          NOT NULL DEFAULT 1,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);
GO

-- ---------- ROLES DE USUARIO ----------
IF OBJECT_ID('dbo.usuario_roles', 'U') IS NULL
CREATE TABLE dbo.usuario_roles (
    usuario_id BIGINT      NOT NULL,
    rol        VARCHAR(30) NOT NULL,
    PRIMARY KEY (usuario_id, rol),
    CONSTRAINT fk_usuario_roles_usuario
        FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios (id) ON DELETE CASCADE
);
GO

-- ---------- SERVICIOS TURÍSTICOS ----------
IF OBJECT_ID('dbo.servicios_turisticos', 'U') IS NULL
CREATE TABLE dbo.servicios_turisticos (
    id              BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    nombre          VARCHAR(150)  NOT NULL,
    tipo            VARCHAR(60)   NOT NULL,
    descripcion     VARCHAR(2000) NOT NULL,
    horarios        VARCHAR(255)  NOT NULL,
    precio          DECIMAL(10,2) NOT NULL,
    ruta            VARCHAR(255)  NULL,
    contacto        VARCHAR(180)  NOT NULL,
    acepta_reservas BIT           NOT NULL,
    atenciones      INT           NOT NULL DEFAULT 0,
    calificacion    FLOAT         NOT NULL DEFAULT 0,
    activo          BIT           NOT NULL DEFAULT 1,
    creador_id      BIGINT        NULL,
    CONSTRAINT fk_servicios_creador
        FOREIGN KEY (creador_id) REFERENCES dbo.usuarios (id) ON DELETE SET NULL
);
GO

-- ---------- DATOS DE EJEMPLO ----------
INSERT INTO dbo.servicios_turisticos
    (nombre, tipo, descripcion, horarios, precio, ruta, contacto, acepta_reservas, atenciones, calificacion, activo)
VALUES
    (N'Sabores del Norte', N'restaurante',
     N'Comida piurana tradicional preparada con productos locales, como ceviche, seco de chabelo y malarrabia.',
     N'Lunes a sábado, 12:00 - 22:00', 35.00, N'Av. Grau 450, Piura', N'999888777', 1, 0, 4.7, 1),
    (N'Guías Piura Aventura', N'guia',
     N'Recorridos guiados por lugares naturales y culturales de Piura para grupos y familias.',
     N'Previa reserva, todos los días', 80.00, N'Piura - Ayabaca - Huancabamba', N'guias@piura.pe', 1, 0, 4.9, 1),
    (N'Hotel Río Piura', N'hotel',
     N'Hospedaje con habitaciones cómodas, desayuno y atención para visitantes de la región.',
     N'Atención 24 horas', 120.00, N'Jr. Lima 210, Piura', N'reservas@hotelrio.pe', 1, 0, 4.5, 1),
    (N'Artesanías Tallán', N'tienda',
     N'Tienda de recuerdos, tejidos y artesanías elaboradas por productores locales de Piura.',
     N'Lunes a sábado, 09:00 - 20:00', 25.00, N'Jr. Ayacucho 125, Piura', N'artesanias.tallan@gmail.com', 0, 0, 4.4, 1);
GO

SELECT * FROM dbo.servicios_turisticos;