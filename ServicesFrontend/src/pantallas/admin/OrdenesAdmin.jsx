import { useState } from 'react'
import { admin } from '../../api/portal'
import DetalleOrden from '../../componentes/DetalleOrden'
import Historial from '../../componentes/Historial'
import { Campo, Cargando, Mensajes, Vacio } from '../../componentes/Ui'
import { useAccion } from '../../hooks/useAccion'
import { useCarga } from '../../hooks/useCarga'
import { etiqueta } from '../../util/formato'

const FILTROS = ['', 'PENDIENTE', 'ASIGNADA', 'EN_ATENCION', 'RESUELTA', 'CERRADA', 'CANCELADA']
const REASIGNABLES = ['PENDIENTE', 'ASIGNADA', 'EN_ATENCION']

export default function OrdenesAdmin() {
  const [filtro, setFiltro] = useState('')
  const lista = useCarga(() => admin.ordenes(filtro), [filtro])
  const tecnicos = useCarga(() => admin.usuarios('TECNICO'), [])
  const accion = useAccion()

  async function ejecutar(funcion, mensaje) {
    if (await accion.ejecutar(funcion, mensaje)) lista.recargar()
  }

  function cancelar(o) {
    const motivo = window.prompt(`Motivo de cancelación de la orden #${o.id}:`)
    if (motivo?.trim()) ejecutar(() => admin.cancelar(o.id, motivo.trim()), `Orden #${o.id} cancelada.`)
  }

  const activos = (tecnicos.datos ?? []).filter((t) => t.activo)

  return (
    <section className="space-y-4">
      <Campo id="filtro-ordenes" etiqueta="Filtrar por estado">
        <select id="filtro-ordenes" className="campo max-w-xs" value={filtro} onChange={(e) => setFiltro(e.target.value)}>
          {FILTROS.map((f) => (
            <option key={f} value={f}>
              {f ? etiqueta(f) : 'Todas'}
            </option>
          ))}
        </select>
      </Campo>
      <Mensajes error={lista.error || accion.error} exito={accion.exito} />
      {lista.cargando && !lista.datos && <Cargando />}
      {lista.datos?.length === 0 && <Vacio>No hay órdenes con este filtro.</Vacio>}
      {lista.datos?.map((o) => (
        <article key={o.id} className="tarjeta">
          <DetalleOrden orden={o} />
          <div className="flex flex-wrap items-end gap-2 mt-4">
            {REASIGNABLES.includes(o.estado) && (
              <AsignarTecnico orden={o} tecnicos={activos} ocupado={accion.ocupado} onAsignar={(id) => ejecutar(() => admin.asignar(o.id, id), `Orden #${o.id} asignada.`)} />
            )}
            {o.estado === 'RESUELTA' && (
              <button type="button" className="btn btn-oscuro btn-chico" disabled={accion.ocupado} onClick={() => ejecutar(() => admin.cerrar(o.id), `Orden #${o.id} cerrada.`)}>
                Cerrar orden
              </button>
            )}
            {REASIGNABLES.includes(o.estado) && (
              <button type="button" className="btn btn-peligro btn-chico" disabled={accion.ocupado} onClick={() => cancelar(o)}>
                Cancelar
              </button>
            )}
          </div>
          <Historial cargar={() => admin.historial(o.id)} />
        </article>
      ))}
    </section>
  )
}

function AsignarTecnico({ orden, tecnicos, ocupado, onAsignar }) {
  const [tecnicoId, setTecnicoId] = useState('')
  const candidatos = tecnicos.filter((t) => t.id !== orden.tecnico?.id)
  return (
    <>
      <label className="sr-only" htmlFor={`asignar-${orden.id}`}>
        Técnico
      </label>
      <select id={`asignar-${orden.id}`} className="campo max-w-xs" value={tecnicoId} onChange={(e) => setTecnicoId(e.target.value)}>
        <option value="">{orden.tecnico ? 'Reasignar a…' : 'Asignar a…'}</option>
        {candidatos.map((t) => (
          <option key={t.id} value={t.id}>
            {t.nombres} {t.apellidos}
          </option>
        ))}
      </select>
      <button type="button" className="btn btn-claro btn-chico" disabled={ocupado || !tecnicoId} onClick={() => onAsignar(Number(tecnicoId))}>
        {orden.tecnico ? 'Reasignar' : 'Asignar'}
      </button>
    </>
  )
}
