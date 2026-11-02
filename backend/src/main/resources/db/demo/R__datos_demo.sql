-- Datos de demostración (perfil "demo"). Se basan en el prototipo HTML.
-- Es idempotente: no duplica registros ni pisa los cambios hechos desde la aplicación.
-- Campari queda cerca de su punto de reposición para mostrar la alerta (Observer) con un Negroni.
-- Las direcciones y los horarios son de ejemplo.

INSERT INTO barstock.establecimiento
    (id, codigo, nombre, zona, direccion, descripcion, happy_hour_inicio, happy_hour_fin, happy_hour_descuento,
     canales_alerta, email_alertas, telefono_alertas, mediodia_apertura, mediodia_cierre, noche_apertura, noche_cierre)
VALUES
    ('00000000-0000-0000-0000-0000000000a1', 'AMBAR', 'Bar Ámbar', 'Palermo · CABA', 'Gorriti 4850', 'Coctelería de autor',
     '18:00', '21:00', 20, 'PANEL,EMAIL', 'encargado@ambar.example', '+54 11 5555-0101', '12:00', '16:00', '18:00', '02:00'),
    ('00000000-0000-0000-0000-0000000000b1', 'ESQUINA', 'La Esquina', 'Núñez · CABA', 'Av. Cabildo 3120', 'Bar de barrio',
     '19:00', '21:00', 25, 'PANEL,SMS,WHATSAPP', 'encargado@esquina.example', '+54 11 5555-0202', '12:00', '16:00', '19:00', '03:00')
ON CONFLICT (id) DO NOTHING;

-- Bases creadas antes de la versión 7: completa dirección y horarios si todavía no se cargaron.
UPDATE barstock.establecimiento
SET direccion = 'Gorriti 4850', noche_apertura = '18:00', noche_cierre = '02:00'
WHERE id = '00000000-0000-0000-0000-0000000000a1' AND direccion = '';

UPDATE barstock.establecimiento
SET direccion = 'Av. Cabildo 3120', noche_apertura = '19:00', noche_cierre = '03:00'
WHERE id = '00000000-0000-0000-0000-0000000000b1' AND direccion = '';

INSERT INTO barstock.insumo
    (id, establecimiento_id, codigo, nombre, unidad_medida, stock_actual, costo_unitario, punto_reorden, contenido_envase)
VALUES
    ('00000000-0000-0000-0000-00000000a101', '00000000-0000-0000-0000-0000000000a1', 'GIN', 'Gin nacional', 'ML', 900, 25.33, 300, 750),
    ('00000000-0000-0000-0000-00000000a102', '00000000-0000-0000-0000-0000000000a1', 'CAMPARI', 'Campari', 'ML', 270, 20.00, 250, 750),
    ('00000000-0000-0000-0000-00000000a103', '00000000-0000-0000-0000-0000000000a1', 'VERMUT', 'Vermut rosso', 'ML', 900, 10.30, 300, 1000),
    ('00000000-0000-0000-0000-00000000a104', '00000000-0000-0000-0000-0000000000a1', 'VODKA', 'Vodka nacional', 'ML', 800, 18.00, 250, 750),
    ('00000000-0000-0000-0000-00000000a105', '00000000-0000-0000-0000-0000000000a1', 'FERNET', 'Fernet', 'ML', 800, 26.27, 250, 750),
    ('00000000-0000-0000-0000-00000000a106', '00000000-0000-0000-0000-0000000000a1', 'TONICA', 'Tónica', 'ML', 3000, 3.20, 800, 1000),
    ('00000000-0000-0000-0000-00000000a107', '00000000-0000-0000-0000-0000000000a1', 'COCA', 'Coca-Cola', 'ML', 3000, 2.40, 800, 1500),
    ('00000000-0000-0000-0000-00000000b101', '00000000-0000-0000-0000-0000000000b1', 'GIN', 'Gin nacional', 'ML', 420, 23.33, 180, 750),
    ('00000000-0000-0000-0000-00000000b102', '00000000-0000-0000-0000-0000000000b1', 'FERNET', 'Fernet', 'ML', 600, 28.00, 180, 750),
    ('00000000-0000-0000-0000-00000000b103', '00000000-0000-0000-0000-0000000000b1', 'COCA', 'Coca-Cola', 'ML', 2100, 2.60, 400, 1500),
    ('00000000-0000-0000-0000-00000000b104', '00000000-0000-0000-0000-0000000000b1', 'TONICA', 'Tónica', 'ML', 950, 3.40, 260, 1000),
    ('00000000-0000-0000-0000-00000000b105', '00000000-0000-0000-0000-0000000000b1', 'VERMUT', 'Vermut', 'ML', 1200, 11.00, 400, 1000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO barstock.receta (id, establecimiento_id, nombre, descripcion, icono, precio_base)
VALUES
    ('00000000-0000-0000-0000-00000000a201', '00000000-0000-0000-0000-0000000000a1', 'Gin Tonic', 'Gin nacional con tónica bien fría', '🍸', 9800),
    ('00000000-0000-0000-0000-00000000a202', '00000000-0000-0000-0000-0000000000a1', 'Negroni', 'Gin, Campari y vermut rosso en partes iguales', '🥃', 11000),
    ('00000000-0000-0000-0000-00000000a203', '00000000-0000-0000-0000-0000000000a1', 'Fernet con Coca', 'El clásico argentino', '🥤', 9500),
    ('00000000-0000-0000-0000-00000000a204', '00000000-0000-0000-0000-0000000000a1', 'Vodka Tonic', 'Vodka nacional con tónica', '🍹', 9000),
    ('00000000-0000-0000-0000-00000000b201', '00000000-0000-0000-0000-0000000000b1', 'Fernet de la casa', 'Fernet con Coca, receta del barrio', '🥤', 8200),
    ('00000000-0000-0000-0000-00000000b202', '00000000-0000-0000-0000-0000000000b1', 'Gin Tonic', 'Gin nacional con tónica', '🍸', 8900),
    ('00000000-0000-0000-0000-00000000b203', '00000000-0000-0000-0000-0000000000b1', 'Vermut con tónica', 'Aperitivo liviano', '🍷', 6800)
ON CONFLICT (id) DO NOTHING;

INSERT INTO barstock.ingrediente_receta (id, receta_id, insumo_id, cantidad, posicion)
VALUES
    ('00000000-0000-0000-0000-00000000a301', '00000000-0000-0000-0000-00000000a201', '00000000-0000-0000-0000-00000000a101', 50, 0),
    ('00000000-0000-0000-0000-00000000a302', '00000000-0000-0000-0000-00000000a201', '00000000-0000-0000-0000-00000000a106', 150, 1),
    ('00000000-0000-0000-0000-00000000a303', '00000000-0000-0000-0000-00000000a202', '00000000-0000-0000-0000-00000000a101', 30, 0),
    ('00000000-0000-0000-0000-00000000a304', '00000000-0000-0000-0000-00000000a202', '00000000-0000-0000-0000-00000000a102', 30, 1),
    ('00000000-0000-0000-0000-00000000a305', '00000000-0000-0000-0000-00000000a202', '00000000-0000-0000-0000-00000000a103', 30, 2),
    ('00000000-0000-0000-0000-00000000a306', '00000000-0000-0000-0000-00000000a203', '00000000-0000-0000-0000-00000000a105', 70, 0),
    ('00000000-0000-0000-0000-00000000a307', '00000000-0000-0000-0000-00000000a203', '00000000-0000-0000-0000-00000000a107', 150, 1),
    ('00000000-0000-0000-0000-00000000a308', '00000000-0000-0000-0000-00000000a204', '00000000-0000-0000-0000-00000000a104', 50, 0),
    ('00000000-0000-0000-0000-00000000a309', '00000000-0000-0000-0000-00000000a204', '00000000-0000-0000-0000-00000000a106', 150, 1),
    ('00000000-0000-0000-0000-00000000b301', '00000000-0000-0000-0000-00000000b201', '00000000-0000-0000-0000-00000000b102', 60, 0),
    ('00000000-0000-0000-0000-00000000b302', '00000000-0000-0000-0000-00000000b201', '00000000-0000-0000-0000-00000000b103', 180, 1),
    ('00000000-0000-0000-0000-00000000b303', '00000000-0000-0000-0000-00000000b202', '00000000-0000-0000-0000-00000000b101', 45, 0),
    ('00000000-0000-0000-0000-00000000b304', '00000000-0000-0000-0000-00000000b202', '00000000-0000-0000-0000-00000000b104', 160, 1),
    ('00000000-0000-0000-0000-00000000b305', '00000000-0000-0000-0000-00000000b203', '00000000-0000-0000-0000-00000000b105', 90, 0),
    ('00000000-0000-0000-0000-00000000b306', '00000000-0000-0000-0000-00000000b203', '00000000-0000-0000-0000-00000000b104', 90, 1)
ON CONFLICT (id) DO NOTHING;

-- Historial de las últimas cuatro semanas para que los reportes tengan datos desde el primer día:
-- ventas por turno (con Happy Hour, fines de semana más fuertes y alguna devolución), recuentos de
-- cierre con comentarios y compras. Bar Ámbar crece semana a semana y La Esquina cae en la última,
-- para comparar tendencias entre bares. Solo se genera en una base sin comandas y con semilla fija.
DO $$
DECLARE
    v_zona CONSTANT text := 'America/Argentina/Buenos_Aires';
    v_hoy date := ((now() AT TIME ZONE 'America/Argentina/Buenos_Aires') - interval '6 hours')::date;
    v_bar record;
    v_ids uuid[];
    v_nombres text[];
    v_precios numeric[];
    v_costos numeric[];
    v_insumos uuid[];
    v_insumo_nombres text[];
    v_contenidos integer[];
    v_costos_insumo numeric[];
    v_dia integer;
    v_jornada date;
    v_turno integer;
    v_cantidad integer;
    v_k integer;
    v_apertura time;
    v_duracion interval;
    v_local timestamp;
    v_momento timestamptz;
    v_numero integer;
    v_auditoria integer;
    v_comanda uuid;
    v_item uuid;
    v_nombre text;
    v_linea integer;
    v_receta integer;
    v_unidades integer;
    v_precio numeric;
    v_total numeric;
    v_costo numeric;
    v_happy boolean;
    v_factor numeric;
    v_usadas integer[];
    v_audit uuid;
    v_posicion integer;
    v_i integer;
    v_teorico integer;
    v_diferencia integer;
    v_comentario text;
    v_merma numeric;
BEGIN
    IF EXISTS (SELECT 1 FROM barstock.comanda) THEN
        RETURN;
    END IF;
    PERFORM setseed(0.42);

    FOR v_bar IN SELECT * FROM barstock.establecimiento
                 WHERE id IN ('00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-0000000000b1')
                 ORDER BY codigo LOOP
        SELECT array_agg(r.id ORDER BY r.nombre), array_agg(r.nombre ORDER BY r.nombre),
               array_agg(r.precio_base ORDER BY r.nombre), array_agg(round(c.costo, 2) ORDER BY r.nombre)
          INTO v_ids, v_nombres, v_precios, v_costos
          FROM barstock.receta r
          JOIN (SELECT ir.receta_id, sum(ir.cantidad * i.costo_unitario) AS costo
                  FROM barstock.ingrediente_receta ir
                  JOIN barstock.insumo i ON i.id = ir.insumo_id
                 GROUP BY ir.receta_id) c ON c.receta_id = r.id
         WHERE r.establecimiento_id = v_bar.id AND r.activa;
        SELECT array_agg(i.id ORDER BY i.codigo), array_agg(i.nombre ORDER BY i.codigo),
               array_agg(i.contenido_envase ORDER BY i.codigo), array_agg(i.costo_unitario ORDER BY i.codigo)
          INTO v_insumos, v_insumo_nombres, v_contenidos, v_costos_insumo
          FROM barstock.insumo i
         WHERE i.establecimiento_id = v_bar.id AND i.activo AND i.unidad_medida = 'ML';
        CONTINUE WHEN v_ids IS NULL OR v_insumos IS NULL;

        v_numero := 0;
        v_auditoria := 0;
        FOR v_dia IN REVERSE 27..0 LOOP
            v_jornada := v_hoy - v_dia;
            v_factor := CASE WHEN v_bar.codigo = 'AMBAR' THEN 0.8 + 0.4 * (27 - v_dia) / 27.0
                             WHEN v_dia < 7 THEN 0.7
                             ELSE 1.0 END;

            -- Compras de la mañana, cada cuatro jornadas.
            IF v_dia % 4 = 2 THEN
                v_i := 1 + (v_dia % array_length(v_insumos, 1));
                v_momento := (v_jornada + time '11:00') AT TIME ZONE v_zona;
                IF v_momento < now() THEN
                    INSERT INTO barstock.movimiento_stock
                        (id, establecimiento_id, insumo_id, nombre_insumo, unidad_medida, tipo, cantidad,
                         stock_resultante, monto, detalle, fecha)
                    VALUES (gen_random_uuid(), v_bar.id, v_insumos[v_i], v_insumo_nombres[v_i], 'ML', 'COMPRA',
                            2 * v_contenidos[v_i], 2 * v_contenidos[v_i] + 300 + floor(random() * 900)::int,
                            round(2 * v_contenidos[v_i] * v_costos_insumo[v_i], 2),
                            '2 envase(s) de ' || v_contenidos[v_i] || ' ml', v_momento);
                END IF;
            END IF;

            FOR v_turno IN 1..2 LOOP
                IF v_turno = 1 THEN
                    v_apertura := v_bar.mediodia_apertura;
                    v_duracion := v_bar.mediodia_cierre - v_bar.mediodia_apertura;
                    v_cantidad := round((2 + random() * 2) * v_factor);
                ELSE
                    v_apertura := v_bar.noche_apertura;
                    v_duracion := CASE WHEN v_bar.noche_cierre <= v_bar.noche_apertura
                                       THEN v_bar.noche_cierre - v_bar.noche_apertura + interval '24 hours'
                                       ELSE v_bar.noche_cierre - v_bar.noche_apertura END;
                    v_cantidad := round((5 + random() * 3) * v_factor
                                        * CASE extract(dow FROM v_jornada) WHEN 5 THEN 1.6 WHEN 6 THEN 1.6
                                                                          WHEN 4 THEN 1.2 WHEN 0 THEN 0.8 ELSE 1 END);
                END IF;

                FOR v_k IN 0..v_cantidad - 1 LOOP
                    v_local := v_jornada + v_apertura + v_duracion * ((v_k + random()) / v_cantidad);
                    v_momento := v_local AT TIME ZONE v_zona;
                    CONTINUE WHEN v_momento > now() - interval '15 minutes';
                    v_numero := v_numero + 1;
                    v_comanda := gen_random_uuid();
                    v_happy := v_local::time >= v_bar.happy_hour_inicio AND v_local::time < v_bar.happy_hour_fin;
                    INSERT INTO barstock.comanda
                        (id, establecimiento_id, numero, estado, origen, cliente, estrategia_precio, total, costo_total,
                         registrada_en, preparada_en, despachada_en)
                    VALUES (v_comanda, v_bar.id, v_numero, 'DESPACHADA',
                            CASE WHEN random() < 0.5 THEN 'CLIENTE' ELSE 'BARTENDER' END,
                            'Mesa ' || (1 + floor(random() * 12))::int,
                            CASE WHEN v_happy
                                 THEN 'Happy Hour −' || rtrim(rtrim(v_bar.happy_hour_descuento::text, '0'), '.') || ' %'
                                 ELSE 'Precio normal' END,
                            0, 0, v_momento - interval '9 minutes', v_momento - interval '6 minutes', v_momento);

                    v_total := 0;
                    v_costo := 0;
                    v_usadas := '{}';
                    FOR v_linea IN 0..(CASE WHEN random() < 0.4 THEN 1 ELSE 0 END) LOOP
                        v_receta := 1 + floor(random() * array_length(v_ids, 1))::int;
                        CONTINUE WHEN v_receta = ANY (v_usadas);
                        v_usadas := v_usadas || v_receta;
                        v_unidades := CASE WHEN random() < 0.3 THEN 2 ELSE 1 END;
                        v_precio := CASE WHEN v_happy
                                         THEN round(v_precios[v_receta] * (100 - v_bar.happy_hour_descuento) / 100, 2)
                                         ELSE v_precios[v_receta] END;
                        INSERT INTO barstock.item_comanda
                            (id, comanda_id, receta_id, nombre_receta, cantidad, precio_unitario, costo_unitario, posicion)
                        VALUES (gen_random_uuid(), v_comanda, v_ids[v_receta], v_nombres[v_receta], v_unidades,
                                v_precio, v_costos[v_receta], v_linea);
                        v_total := v_total + v_precio * v_unidades;
                        v_costo := v_costo + v_costos[v_receta] * v_unidades;
                    END LOOP;
                    UPDATE barstock.comanda SET total = v_total, costo_total = v_costo WHERE id = v_comanda;

                    -- De vez en cuando el cliente devuelve uno de dos tragos iguales.
                    IF random() < 0.05 THEN
                        SELECT ic.id, ic.nombre_receta, ic.precio_unitario INTO v_item, v_nombre, v_precio
                          FROM barstock.item_comanda ic
                         WHERE ic.comanda_id = v_comanda AND ic.cantidad = 2
                         LIMIT 1;
                        IF FOUND THEN
                            UPDATE barstock.item_comanda SET cantidad_devuelta = 1 WHERE id = v_item;
                            INSERT INTO barstock.devolucion_item
                                (id, comanda_id, item_id, nombre_receta, cantidad, importe, motivo, registrada_en)
                            VALUES (gen_random_uuid(), v_comanda, v_item, v_nombre, 1, v_precio,
                                    (ARRAY['Estaba muy dulce', 'Pidió cambiarlo por otro trago',
                                           'Se sirvió sin hielo'])[1 + floor(random() * 3)::int],
                                    v_momento + interval '10 minutes');
                        END IF;
                    END IF;
                END LOOP;
            END LOOP;

            -- Recuento de cierre cada tres jornadas, después de cerrar la noche.
            IF v_dia % 3 = 1 THEN
                v_momento := (v_jornada + v_bar.noche_cierre
                              + CASE WHEN v_bar.noche_cierre <= v_bar.noche_apertura THEN interval '1 day'
                                     ELSE interval '0' END
                              + interval '20 minutes') AT TIME ZONE v_zona;
                IF v_momento < now() THEN
                    v_auditoria := v_auditoria + 1;
                    v_audit := gen_random_uuid();
                    v_merma := 0;
                    INSERT INTO barstock.auditoria (id, establecimiento_id, numero, responsable, fecha, merma_total)
                    VALUES (v_audit, v_bar.id, v_auditoria,
                            CASE WHEN v_dia % 2 = 0 THEN 'Encargado · Sofía' ELSE 'Bartender · Martín' END,
                            v_momento, 0);
                    FOR v_posicion IN 0..1 LOOP
                        v_i := 1 + ((v_dia + v_posicion * 2) % array_length(v_insumos, 1));
                        v_teorico := greatest(600 + floor(random() * 1400)::int, v_contenidos[v_i] + 200);
                        IF v_posicion = 0 AND v_dia % 9 = 1 THEN
                            v_diferencia := -v_contenidos[v_i];
                            v_comentario := 'Se rompió una botella al reponer la barra';
                        ELSIF v_posicion = 1 THEN
                            v_diferencia := -(40 + floor(random() * 40)::int);
                            v_comentario := 'Dos tragos de cortesía para una mesa de habitués (bartender: Martín)';
                        ELSE
                            v_diferencia := -(10 + floor(random() * 25)::int);
                            v_comentario := CASE WHEN random() < 0.5 THEN 'Derrame al servir' END;
                        END IF;
                        INSERT INTO barstock.detalle_auditoria
                            (id, auditoria_id, insumo_id, nombre_insumo, unidad_medida, stock_teorico, stock_fisico,
                             diferencia, costo_merma, posicion, contenido_envase, comentario)
                        VALUES (gen_random_uuid(), v_audit, v_insumos[v_i], v_insumo_nombres[v_i], 'ML', v_teorico,
                                v_teorico + v_diferencia, v_diferencia, round(-v_diferencia * v_costos_insumo[v_i], 2),
                                v_posicion, v_contenidos[v_i], v_comentario);
                        v_merma := v_merma + round(-v_diferencia * v_costos_insumo[v_i], 2);
                    END LOOP;
                    UPDATE barstock.auditoria SET merma_total = v_merma WHERE id = v_audit;
                END IF;
            END IF;
        END LOOP;

        UPDATE barstock.establecimiento
        SET proximo_numero_comanda = v_numero + 1, proximo_numero_auditoria = v_auditoria + 1
        WHERE id = v_bar.id;
    END LOOP;
END $$;
