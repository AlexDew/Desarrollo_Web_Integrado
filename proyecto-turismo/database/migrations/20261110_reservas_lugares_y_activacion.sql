IF COL_LENGTH('dbo.reservas', 'lugar_id') IS NULL
BEGIN
    ALTER TABLE dbo.reservas ADD lugar_id BIGINT NULL;
END;
GO

DECLARE @servicioFk sysname;
DECLARE @dropServicioFk nvarchar(max);

SELECT TOP (1) @servicioFk = fk.name
FROM sys.foreign_keys fk
JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
WHERE fk.parent_object_id = OBJECT_ID('dbo.reservas')
  AND fkc.parent_column_id = COLUMNPROPERTY(OBJECT_ID('dbo.reservas'), 'servicio_id', 'ColumnId');

IF @servicioFk IS NOT NULL
BEGIN
    SET @dropServicioFk = N'ALTER TABLE dbo.reservas DROP CONSTRAINT ' + QUOTENAME(@servicioFk);
    EXEC sp_executesql @dropServicioFk;
END;

ALTER TABLE dbo.reservas ALTER COLUMN servicio_id BIGINT NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_reservas_lugares_turisticos'
)
BEGIN
    ALTER TABLE dbo.reservas
        ADD CONSTRAINT FK_reservas_lugares_turisticos
        FOREIGN KEY (lugar_id) REFERENCES dbo.lugares_turisticos(id);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_reservas_servicios_turisticos'
)
BEGIN
    ALTER TABLE dbo.reservas
        ADD CONSTRAINT FK_reservas_servicios_turisticos
        FOREIGN KEY (servicio_id) REFERENCES dbo.servicios_turisticos(id);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_reservas_un_recurso'
)
BEGIN
    ALTER TABLE dbo.reservas
        ADD CONSTRAINT CK_reservas_un_recurso
        CHECK (
            (servicio_id IS NOT NULL AND lugar_id IS NULL)
            OR (servicio_id IS NULL AND lugar_id IS NOT NULL)
        );
END;
GO

UPDATE dbo.servicios_turisticos
SET acepta_reservas = 1
WHERE activo = 1 AND acepta_reservas = 0;
GO
