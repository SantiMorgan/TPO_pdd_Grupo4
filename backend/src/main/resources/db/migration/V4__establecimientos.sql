-- Cada establecimiento administra su propio inventario, menú y comandas.
-- La política de precios (Strategy) y los canales de alerta (Observer + Simple Factory)
-- se configuran por establecimiento.
CREATE TABLE barstock.establecimiento (
    id UUID PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    zona VARCHAR(80) NOT NULL,
    descripcion VARCHAR(150) NOT NULL DEFAULT '',
    proximo_numero_comanda INTEGER NOT NULL DEFAULT 1 CHECK (proximo_numero_comanda > 0),
    proximo_numero_auditoria INTEGER NOT NULL DEFAULT 1 CHECK (proximo_numero_auditoria > 0),
    modo_precio VARCHAR(20) NOT NULL DEFAULT 'AUTOMATICO'
        CHECK (modo_precio IN ('AUTOMATICO', 'NORMAL', 'HAPPY_HOUR', 'DEMANDA_ALTA')),
    happy_hour_inicio TIME NOT NULL DEFAULT '18:00',
    happy_hour_fin TIME NOT NULL DEFAULT '21:00',
    happy_hour_descuento NUMERIC(5,2) NOT NULL DEFAULT 20 CHECK (happy_hour_descuento BETWEEN 0 AND 90),
    demanda_alta_incremento NUMERIC(5,2) NOT NULL DEFAULT 15 CHECK (demanda_alta_incremento BETWEEN 0 AND 100),
    demanda_alta_umbral INTEGER NOT NULL DEFAULT 4 CHECK (demanda_alta_umbral BETWEEN 1 AND 99),
    margen_minimo NUMERIC(6,2) NOT NULL DEFAULT 50 CHECK (margen_minimo BETWEEN 0 AND 1000),
    hora_simulada TIME,
    canales_alerta VARCHAR(60) NOT NULL DEFAULT 'PANEL,EMAIL',
    email_alertas VARCHAR(120),
    telefono_alertas VARCHAR(30),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_establecimiento_codigo CHECK (length(trim(codigo)) > 0 AND codigo = upper(trim(codigo))),
    CONSTRAINT ck_establecimiento_nombre CHECK (length(trim(nombre)) > 0)
);

-- Los insumos cargados antes de esta versión pasan a un establecimiento inicial.
INSERT INTO barstock.establecimiento (id, codigo, nombre, zona, descripcion)
SELECT '00000000-0000-0000-0000-0000000000a1', 'AMBAR', 'Bar Ámbar', 'Palermo · CABA', 'Coctelería de autor'
WHERE EXISTS (SELECT 1 FROM barstock.insumo);

ALTER TABLE barstock.insumo
    ADD COLUMN establecimiento_id UUID REFERENCES barstock.establecimiento (id),
    ADD COLUMN contenido_envase INTEGER NOT NULL DEFAULT 1
        CHECK (contenido_envase BETWEEN 1 AND 999999);

UPDATE barstock.insumo SET establecimiento_id = '00000000-0000-0000-0000-0000000000a1';

ALTER TABLE barstock.insumo ALTER COLUMN establecimiento_id SET NOT NULL;

-- El código pasa a ser único dentro de cada establecimiento.
ALTER TABLE barstock.insumo DROP CONSTRAINT insumo_codigo_key;
ALTER TABLE barstock.insumo
    ADD CONSTRAINT uk_insumo_establecimiento_codigo UNIQUE (establecimiento_id, codigo);

DROP INDEX barstock.idx_insumo_activo_nombre;
CREATE INDEX idx_insumo_establecimiento_activo ON barstock.insumo (establecimiento_id, activo, nombre, id);
