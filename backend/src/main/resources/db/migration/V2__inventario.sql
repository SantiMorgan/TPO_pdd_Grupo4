CREATE TABLE barstock.insumo (
    id UUID PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    unidad_medida VARCHAR(10) NOT NULL CHECK (unidad_medida IN ('ML', 'UNIDAD')),
    stock_actual NUMERIC(12,3) NOT NULL CHECK (stock_actual >= 0),
    costo_unitario NUMERIC(12,2) NOT NULL CHECK (costo_unitario >= 0),
    punto_reorden NUMERIC(12,3) NOT NULL CHECK (punto_reorden >= 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_insumo_codigo CHECK (length(trim(codigo)) > 0 AND codigo = upper(trim(codigo))),
    CONSTRAINT ck_insumo_nombre CHECK (length(trim(nombre)) > 0)
);

CREATE INDEX idx_insumo_activo_nombre ON barstock.insumo (activo, nombre, id);
