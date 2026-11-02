-- Ajustes de la revisión de la Actividad 3.

-- Dirección y horarios de atención. La jornada va de 6:00 a 5:59: el turno noche puede
-- terminar después de medianoche y sus ventas siguen siendo de la misma jornada.
ALTER TABLE barstock.establecimiento
    ADD COLUMN direccion VARCHAR(120) NOT NULL DEFAULT '',
    ADD COLUMN mediodia_apertura TIME NOT NULL DEFAULT '12:00',
    ADD COLUMN mediodia_cierre TIME NOT NULL DEFAULT '16:00',
    ADD COLUMN noche_apertura TIME NOT NULL DEFAULT '20:00',
    ADD COLUMN noche_cierre TIME NOT NULL DEFAULT '02:00';

-- Se quita la estrategia de demanda alta: los bares que la usaban pasan a modo automático.
UPDATE barstock.establecimiento SET modo_precio = 'AUTOMATICO' WHERE modo_precio = 'DEMANDA_ALTA';
ALTER TABLE barstock.establecimiento DROP CONSTRAINT establecimiento_modo_precio_check;
ALTER TABLE barstock.establecimiento
    ADD CONSTRAINT ck_establecimiento_modo_precio CHECK (modo_precio IN ('AUTOMATICO', 'NORMAL', 'HAPPY_HOUR')),
    DROP COLUMN demanda_alta_incremento,
    DROP COLUMN demanda_alta_umbral;

-- El recuento guarda el contenido del envase de ese momento (para expresar la merma en botellas)
-- y un comentario que justifica la diferencia.
ALTER TABLE barstock.detalle_auditoria
    ADD COLUMN contenido_envase INTEGER NOT NULL DEFAULT 1 CHECK (contenido_envase BETWEEN 1 AND 999999),
    ADD COLUMN comentario VARCHAR(200);

UPDATE barstock.detalle_auditoria d
SET contenido_envase = i.contenido_envase
FROM barstock.insumo i
WHERE i.id = d.insumo_id;

-- Devolución de tragos sueltos de una comanda despachada. Los insumos no se reponen.
ALTER TABLE barstock.item_comanda
    ADD COLUMN cantidad_devuelta INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_item_comanda_devuelta CHECK (cantidad_devuelta BETWEEN 0 AND cantidad);

CREATE TABLE barstock.devolucion_item (
    id UUID PRIMARY KEY,
    comanda_id UUID NOT NULL REFERENCES barstock.comanda (id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES barstock.item_comanda (id) ON DELETE CASCADE,
    nombre_receta VARCHAR(80) NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad BETWEEN 1 AND 99),
    importe NUMERIC(12,2) NOT NULL CHECK (importe >= 0),
    motivo VARCHAR(220) NOT NULL,
    registrada_en TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_devolucion_item_comanda ON barstock.devolucion_item (comanda_id);

-- Los reportes filtran por fecha de despacho, compra y recuento.
CREATE INDEX idx_comanda_establecimiento_despacho ON barstock.comanda (establecimiento_id, despachada_en)
    WHERE despachada_en IS NOT NULL;
CREATE INDEX idx_auditoria_establecimiento_fecha ON barstock.auditoria (establecimiento_id, fecha);
