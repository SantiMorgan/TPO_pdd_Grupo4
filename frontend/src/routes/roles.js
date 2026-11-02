// Qué ve cada rol. La guarda de VistaPorRol solo deja abrir las vistas de esta lista.
// La separación es de interfaz: todavía no hay autenticación (README, decisión abierta 5).

export const ROLES = {
  CLIENTE: {
    nombre: 'Cliente',
    icono: '🙋',
    descripcion: 'Mirá la carta con el precio del momento, hacé tu pedido y seguilo en vivo.',
    pideNombre: 'Tu nombre o mesa',
    vistas: [
      { id: 'carta', icono: '🍸', titulo: 'Carta' },
      { id: 'pedidos', icono: '🧾', titulo: 'Mis pedidos' },
    ],
  },
  BARTENDER: {
    nombre: 'Bartender',
    icono: '🧑‍🍳',
    descripcion: 'Toma comandas, las prepara y despacha; consulta stock y carga el recuento del cierre.',
    pideNombre: 'Tu nombre',
    vistas: [
      { id: 'barra', icono: '🍸', titulo: 'Barra / comandas' },
      { id: 'stock', icono: '▤', titulo: 'Stock disponible' },
      { id: 'cierre', icono: '◈', titulo: 'Cierre de turno' },
    ],
  },
  ENCARGADO: {
    nombre: 'Encargado',
    icono: '📋',
    descripcion: 'Operación diaria: insumos, compras, auditoría de mermas y alertas de reposición.',
    pideNombre: 'Tu nombre',
    vistas: [
      { id: 'inventario', icono: '▤', titulo: 'Inventario y compras' },
      { id: 'auditoria', icono: '◈', titulo: 'Auditoría' },
      { id: 'alertas', icono: '⚠', titulo: 'Alertas' },
    ],
  },
  DUENO: {
    nombre: 'Dueño',
    icono: '👔',
    descripcion: 'Rentabilidad de sus bares: reportes, política de precios, menú y configuración.',
    pideNombre: 'Tu nombre',
    vistas: [
      { id: 'resumen', icono: '◫', titulo: 'Resumen' },
      { id: 'reportes', icono: '▥', titulo: 'Reportes' },
      { id: 'precios', icono: '％', titulo: 'Precios' },
      { id: 'menu', icono: '🍹', titulo: 'Menú y recetas' },
      { id: 'establecimientos', icono: '⚙', titulo: 'Establecimientos' },
    ],
  },
};

export const vistaPermitida = (rol, vista) => ROLES[rol]?.vistas.some((v) => v.id === vista);
export const vistaInicial = (rol) => ROLES[rol]?.vistas[0].id;
