// Piezas visuales reutilizadas por todos los paneles.
import { etiqueta } from '../util/formato'

const COLOR_ESTADO = {
  PENDIENTE: 'estado-gris',
  EN_REVISION: 'estado-azul',
  APROBADA: 'estado-verde',
  RECHAZADA: 'estado-rojo',
  CANCELADA: 'estado-rojo',
  ASIGNADA: 'estado-azul',
  EN_ATENCION: 'estado-ambar',
  RESUELTA: 'estado-verde',
  CERRADA: 'estado-oscuro',
}

export function EstadoBadge({ estado }) {
  return <span className={`estado ${COLOR_ESTADO[estado] ?? ''}`}>{etiqueta(estado)}</span>
}

export function PrioridadBadge({ prioridad }) {
  return <span className={`prioridad prioridad-${prioridad?.toLowerCase()}`}>{etiqueta(prioridad)}</span>
}

export function Indicador({ titulo, valor, detalle, tono = '' }) {
  return (
    <div className={`indicador ${tono}`}>
      <span className="indicador-titulo">{titulo}</span>
      <strong className="indicador-valor">{valor}</strong>
      {detalle && <span className="indicador-detalle">{detalle}</span>}
    </div>
  )
}

/** Barras horizontales simples para conteos por estado o categoría. */
export function BarrasConteo({ titulo, conteos, etiquetar = etiqueta }) {
  const entradas = Object.entries(conteos ?? {})
  const maximo = Math.max(1, ...entradas.map(([, n]) => n))
  return (
    <section className="tarjeta">
      <h3 className="subtitulo">{titulo}</h3>
      {entradas.length === 0 && <p className="texto-suave">Sin datos todavía.</p>}
      <ul className="space-y-2 mt-3">
        {entradas.map(([nombre, cantidad]) => (
          <li key={nombre} className="barra-fila">
            <span className="barra-nombre">{etiquetar(nombre)}</span>
            <span className="barra-pista" aria-hidden="true">
              <span className="barra-relleno" style={{ width: `${(cantidad / maximo) * 100}%` }} />
            </span>
            <span className="barra-valor">{cantidad}</span>
          </li>
        ))}
      </ul>
    </section>
  )
}

export function Pestanas({ opciones, activa, onCambio, etiquetaGrupo }) {
  return (
    <div role="tablist" aria-label={etiquetaGrupo} className="flex flex-wrap gap-2 mb-6">
      {opciones.map(([id, nombre]) => (
        <button
          key={id}
          type="button"
          role="tab"
          aria-selected={activa === id}
          onClick={() => onCambio(id)}
          className={`btn btn-chico ${activa === id ? 'btn-oscuro' : 'btn-claro'}`}
        >
          {nombre}
        </button>
      ))}
    </div>
  )
}

export function Mensajes({ error, exito }) {
  return (
    <>
      {error && (
        <div role="alert" className="alerta mb-4">
          {error}
        </div>
      )}
      {exito && (
        <div role="status" className="exito mb-4">
          {exito}
        </div>
      )}
    </>
  )
}

export function Cargando({ texto = 'Cargando…' }) {
  return (
    <p role="status" className="texto-suave py-6">
      {texto}
    </p>
  )
}

export function Vacio({ children }) {
  return <p className="texto-suave py-4">{children}</p>
}

export function Campo({ id, etiqueta: texto, error, children, ayuda }) {
  return (
    <div>
      <label htmlFor={id} className="label">
        {texto}
      </label>
      {children}
      {ayuda && !error && <p className="ayuda-campo">{ayuda}</p>}
      {error && (
        <p className="error-campo" id={`${id}-error`}>
          {error}
        </p>
      )}
    </div>
  )
}
