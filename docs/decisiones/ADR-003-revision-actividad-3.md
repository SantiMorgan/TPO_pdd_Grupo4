# ADR-003 — Ajustes de la revisión de la Actividad 3

**Estado:** PROPUESTA implementada en la rama `feature/ajustes-observaciones` (migración V7). Pendiente de revisión
del equipo.

## Contexto

La revisión de la aplicación pidió quitar de la interfaz las explicaciones técnicas, ordenar los permisos sobre los
insumos, contar el stock en botellas, mejorar los reportes (períodos claros, filtros por bar, fecha y turno, reporte
diario), quitar la demanda alta, agregar la dirección de cada bar y devolver tragos sueltos.

## Decisiones

1. **Sin detalles técnicos en la interfaz.** Se quitaron el botón de patrones, la guía, la traza y los nombres de
   clases. Esa información está en `docs/BarStock_Patrones_y_detalles_tecnicos.docx`; la traza sigue en la API.
2. **Alta de insumos solo para el dueño.** `roles.js` define el permiso `CREAR_INSUMOS` (solo Dueño) y el formulario
   de alta, con código y stock inicial en botellas + ml, solo se muestra con ese permiso. El encargado compra, edita y
   da de baja. Como todavía no hay inicio de sesión, el permiso se aplica en la interfaz, igual que los roles.
3. **Stock en botellas.** La unidad base sigue siendo el ml (o la unidad). Las botellas se calculan con el
   `contenidoEnvase`: 970 ml en botellas de 750 = 1 botella + 220 ml.
4. **Recuento por envases y con comentario.** El recuento se carga como botellas cerradas + lo que queda en la abierta
   (`Insumo.cantidadContada`). Cada insumo contado admite un comentario (rotura, tragos de cortesía con el bartender,
   derrame). El detalle guarda el contenido del envase para expresar la merma en botellas en los reportes.
5. **Jornada y turnos.** Cada bar tiene horario de mediodía y de noche (ejemplo común: 12–16 y 20–2). La jornada
   va de 6:00 a 5:59, así lo vendido después de medianoche queda en el día en que abrió el turno. Lo que se registra
   fuera de horario se asigna al turno más cercano (por ejemplo, el recuento de las 2:20 cuenta para la noche).
6. **Reportes filtrados.** Los filtros (bares, período de jornadas, turno) se aplican antes de armar la cadena de
   decoradores. La tendencia compara el período elegido contra el anterior de igual duración y desglosa por jornada,
   por bar y por trago, con sugerencias de qué revisar. El reporte diario reúne mediodía y noche con su total.
7. **Sin demanda alta.** Se eliminaron `PrecioDemandaAlta`, el modo `DEMANDA_ALTA` y sus columnas; los bares que lo
   usaban pasan a `AUTOMATICO`. Strategy queda con `PrecioNormal` y `PrecioHappyHour`.
8. **Dirección del bar.** Campo obligatorio al crear y editar.
9. **Devolución por trago.** `Despachada` admite `devolver(item, cantidad, motivo)`: guarda la devolución, descuenta
   el importe de las ventas y no repone stock. Si se devuelven todas las unidades, pasa a `Anulada`. `Despachada` ya
   no acepta `anular` (la devolución completa es devolver todo).

## Consecuencias

- La API cambia en el recuento (`envasesCompletos`, `restante`, `comentario`), los reportes (`desde`, `hasta`,
  `turno`, `/api/reportes`, `/api/reportes/diario`) y el establecimiento (`direccion`, `horario`).
- Las comandas devueltas enteras antes de V7 siguen contando como devolución completa (no tienen detalle por trago).
- Los datos de demostración generan cuatro semanas de historial solo en una base nueva; para verlos hay que reiniciar
  la base local (`reset-db`).
