CREATE TABLE barstock.receta (
    id UUID PRIMARY KEY,
    establecimiento_id UUID NOT NULL REFERENCES barstock.establecimiento (id),
    nombre VARCHAR(80) NOT NULL CHECK (length(trim(nombre)) > 0),
    descripcion VARCHAR(200) NOT NULL DEFAULT '',
    icono VARCHAR(16) NOT NULL DEFAULT '🍹',
    precio_base NUMERIC(12,2) NOT NULL CHECK (precio_base > 0),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_receta_establecimiento_nombre ON barstock.receta (establecimiento_id, lower(nombre));

CREATE TABLE barstock.ingrediente_receta (
    id UUID PRIMARY KEY,
    receta_id UUID NOT NULL REFERENCES barstock.receta (id) ON DELETE CASCADE,
    insumo_id UUID NOT NULL REFERENCES barstock.insumo (id),
    cantidad INTEGER NOT NULL CHECK (cantidad BETWEEN 1 AND 99999),
    posicion INTEGER NOT NULL,
    CONSTRAINT uk_ingrediente_receta UNIQUE (receta_id, insumo_id)
);

-- Ciclo de vida de la comanda (State). El precio se fija al registrar y no se recalcula.
CREATE TABLE barstock.comanda (
    id UUID PRIMARY KEY,
    establecimiento_id UUID NOT NULL REFERENCES barstock.establecimiento (id),
    numero INTEGER NOT NULL CHECK (numero > 0),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('REGISTRADA', 'EN_PREPARACION', 'DESPACHADA', 'ANULADA')),
    origen VARCHAR(20) NOT NULL CHECK (origen IN ('CLIENTE', 'BARTENDER')),
    cliente VARCHAR(60),
    estrategia_precio VARCHAR(60) NOT NULL,
    total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    costo_total NUMERIC(12,2) NOT NULL CHECK (costo_total >= 0),
    registrada_en TIMESTAMPTZ NOT NULL,
    preparada_en TIMESTAMPTZ,
    despachada_en TIMESTAMPTZ,
    anulada_en TIMESTAMPTZ,
    estado_al_anular VARCHAR(20),
    motivo_anulacion VARCHAR(220),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_comanda_numero UNIQUE (establecimiento_id, numero)
);

CREATE INDEX idx_comanda_establecimiento_estado ON barstock.comanda (establecimiento_id, estado, numero);

CREATE TABLE barstock.item_comanda (
    id UUID PRIMARY KEY,
    comanda_id UUID NOT NULL REFERENCES barstock.comanda (id) ON DELETE CASCADE,
    receta_id UUID NOT NULL REFERENCES barstock.receta (id),
    nombre_receta VARCHAR(80) NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad BETWEEN 1 AND 99),
    precio_unitario NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    costo_unitario NUMERIC(12,2) NOT NULL CHECK (costo_unitario >= 0),
    posicion INTEGER NOT NULL
);
