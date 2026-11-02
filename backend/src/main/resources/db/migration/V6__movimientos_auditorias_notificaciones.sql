-- Historial de stock: compras, consumos por despacho y ajustes de auditoría.
CREATE TABLE barstock.movimiento_stock (
    id UUID PRIMARY KEY,
    establecimiento_id UUID NOT NULL REFERENCES barstock.establecimiento (id),
    insumo_id UUID NOT NULL REFERENCES barstock.insumo (id),
    nombre_insumo VARCHAR(150) NOT NULL,
    unidad_medida VARCHAR(10) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('COMPRA', 'CONSUMO', 'AJUSTE')),
    cantidad NUMERIC NOT NULL CHECK (cantidad <> 0 AND cantidad = trunc(cantidad)),
    stock_resultante NUMERIC NOT NULL CHECK (stock_resultante >= 0),
    monto NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (monto >= 0),
    detalle VARCHAR(160) NOT NULL,
    fecha TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_movimiento_establecimiento_fecha ON barstock.movimiento_stock (establecimiento_id, fecha DESC);

-- Recuento físico de cierre de turno contra el stock teórico.
CREATE TABLE barstock.auditoria (
    id UUID PRIMARY KEY,
    establecimiento_id UUID NOT NULL REFERENCES barstock.establecimiento (id),
    numero INTEGER NOT NULL CHECK (numero > 0),
    responsable VARCHAR(60) NOT NULL,
    fecha TIMESTAMPTZ NOT NULL,
    merma_total NUMERIC(12,2) NOT NULL CHECK (merma_total >= 0),
    CONSTRAINT uk_auditoria_numero UNIQUE (establecimiento_id, numero)
);

CREATE TABLE barstock.detalle_auditoria (
    id UUID PRIMARY KEY,
    auditoria_id UUID NOT NULL REFERENCES barstock.auditoria (id) ON DELETE CASCADE,
    insumo_id UUID NOT NULL REFERENCES barstock.insumo (id),
    nombre_insumo VARCHAR(150) NOT NULL,
    unidad_medida VARCHAR(10) NOT NULL,
    stock_teorico NUMERIC NOT NULL CHECK (stock_teorico >= 0),
    stock_fisico NUMERIC NOT NULL CHECK (stock_fisico >= 0 AND stock_fisico = trunc(stock_fisico)),
    diferencia NUMERIC NOT NULL,
    costo_merma NUMERIC(12,2) NOT NULL CHECK (costo_merma >= 0),
    posicion INTEGER NOT NULL
);

-- Registro de cada aviso emitido por los canales creados con NotificacionFactory.
CREATE TABLE barstock.notificacion (
    id UUID PRIMARY KEY,
    establecimiento_id UUID NOT NULL REFERENCES barstock.establecimiento (id),
    insumo_id UUID REFERENCES barstock.insumo (id),
    canal VARCHAR(20) NOT NULL CHECK (canal IN ('PANEL', 'EMAIL', 'SMS', 'WHATSAPP')),
    destinatario VARCHAR(120) NOT NULL,
    mensaje VARCHAR(300) NOT NULL,
    resultado VARCHAR(20) NOT NULL CHECK (resultado IN ('MOSTRADA', 'SIMULADA', 'FALLIDA')),
    fecha TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_notificacion_establecimiento_fecha ON barstock.notificacion (establecimiento_id, fecha DESC);
