import { useState } from 'react'
import { mensajeError } from '../api/http'
import { fecha } from '../util/formato'
import { EstadoBadge } from './Ui'

/** Botón que despliega la bitácora de estados de una orden. */
export default function Historial({ cargar }) {
  const [abierto, setAbierto] = useState(false)
  const [eventos, setEventos] = useState(null)
  const [error, setError] = useState('')

  async function alternar() {
    const abrir = !abierto
    setAbierto(abrir)
    if (abrir) {
      setError('')
      try {
        setEventos(await cargar())
      } catch (e) {
        setError(mensajeError(e))
      }
    }
  }

  return (
    <div className="mt-3">
      <button type="button" className="link text-sm" onClick={alternar} aria-expanded={abierto}>
        {abierto ? 'Ocultar seguimiento' : 'Ver seguimiento'}
      </button>
      {abierto && (
        <div className="mt-2">
          {error && <p className="error-campo">{error}</p>}
          {!eventos && !error && <p className="texto-suave text-sm">Cargando…</p>}
          {eventos && (
            <ol className="linea-tiempo">
              {eventos.map((h, i) => (
                <li key={i}>
                  <EstadoBadge estado={h.estado} />
                  <span className="text-xs texto-suave ml-2">
                    {fecha(h.fecha)} · {h.usuario?.nombre}
                  </span>
                  {h.observacion && <p className="text-sm mt-1">{h.observacion}</p>}
                </li>
              ))}
            </ol>
          )}
        </div>
      )}
    </div>
  )
}
