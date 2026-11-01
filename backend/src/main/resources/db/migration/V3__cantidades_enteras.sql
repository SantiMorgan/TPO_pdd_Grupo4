-- Se conserva V2 para las bases donde ya fue aplicada.
-- NUMERIC sin escala fija permite rechazar fracciones con CHECK antes de que
-- PostgreSQL las redondee automáticamente, como ocurriría con NUMERIC(12,0).
-- trim_scale quita los ceros decimales de cantidades enteras existentes.
ALTER TABLE barstock.insumo
    ALTER COLUMN stock_actual TYPE NUMERIC USING trim_scale(stock_actual),
    ALTER COLUMN punto_reorden TYPE NUMERIC USING trim_scale(punto_reorden);

-- Si existen cantidades fraccionadas, la migración falla sin redondear ni
-- perder datos. Deben corregirse explícitamente antes de volver a ejecutarla.
ALTER TABLE barstock.insumo
    ADD CONSTRAINT ck_insumo_stock_entero CHECK (
        stock_actual = trunc(stock_actual) AND stock_actual <= 999999999999
    ),
    ADD CONSTRAINT ck_insumo_punto_reorden_entero CHECK (
        punto_reorden = trunc(punto_reorden) AND punto_reorden <= 999999999999
    );
