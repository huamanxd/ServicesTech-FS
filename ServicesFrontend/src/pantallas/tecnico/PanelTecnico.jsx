import { useState } from 'react'
import { tecnico } from '../../api/portal'
import DetalleOrden from '../../componentes/DetalleOrden'
import Historial from '../../componentes/Historial'
import { Cargando, Indicador, Mensajes, Pestanas, Vacio } from '../../componentes/Ui'
import { useAutenticacion } from '../../contexto/Autenticacion'
import { useAccion } from '../../hooks/useAccion'
import { useCarga } from '../../hooks/useCarga'
import { horas, porcentaje, promedio } from '../../util/formato'

const PESTANAS = [
  ['mias', 'Mis órdenes'],
  ['bandeja', 'Bandeja de órdenes'],
  ['resumen', 'Mi rendimiento'],
]

export default function PanelTecnico() {
  const { usuario } = useAutenticacion()
  const [pestana, setPestana] = useState('mias')
  const mias = useCarga(() => tecnico.ordenes(), [])
  const bandeja = useCarga(() => tecnico.disponibles(), [])
  const tablero = useCarga(() => tecnico.dashboard(), [])
  const accion = useAccion()

  function refrescar() {
    mias.recargar()
    bandeja.recargar()
    tablero.recargar()
  }

  async function ejecutar(funcion, mensaje) {
    if (await accion.ejecutar(funcion, mensaje)) refrescar()
  }

  const activas = (mias.datos ?? []).filter((o) => ['ASIGNADA', 'EN_ATENCION'].includes(o.estado))
  const anteriores = (mias.datos ?? []).filter((o) => !['ASIGNADA', 'EN_ATENCION'].includes(o.estado))

  return (
    <main className="contenedor py-12">
      <span className="etiqueta">PANEL DEL TÉCNICO</span>
      <h1 className="titulo-pagina">Hola, {usuario.nombres}</h1>
      <p className="texto-apoyo mb-8">Toma órdenes de la bandeja, actualiza su estado y documenta la solución aplicada.</p>
      <Pestanas opciones={PESTANAS} activa={pestana} onCambio={setPestana} etiquetaGrupo="Secciones del técnico" />
      <Mensajes error={accion.error} exito={accion.exito} />

      {pestana === 'mias' && (
        <section className="space-y-4">
          <Mensajes error={mias.error} />
          {mias.cargando && !mias.datos && <Cargando />}
          {mias.datos && activas.length === 0 && <Vacio>No tienes órdenes activas. Revisa la bandeja para tomar una.</Vacio>}
          {activas.map((o) => (
            <OrdenActiva key={o.id} orden={o} ocupado={accion.ocupado} ejecutar={ejecutar} />
          ))}
          {anteriores.length > 0 && <h2 className="subtitulo pt-4">Atendidas anteriormente</h2>}
          {anteriores.map((o) => (
            <article key={o.id} className="tarjeta">
              <DetalleOrden orden={o} />
              <Historial cargar={() => tecnico.historial(o.id)} />
            </article>
          ))}
        </section>
      )}

      {pestana === 'bandeja' && (
        <section className="space-y-4">
          <p className="texto-suave text-sm">
            Órdenes generadas a partir de solicitudes aprobadas que todavía no tienen técnico. Ordenadas por prioridad y fecha límite.
          </p>
          <Mensajes error={bandeja.error} />
          {bandeja.cargando && !bandeja.datos && <Cargando />}
          {bandeja.datos?.length === 0 && <Vacio>No hay órdenes disponibles en este momento.</Vacio>}
          {bandeja.datos?.map((o) => (
            <article key={o.id} className="tarjeta">
              <DetalleOrden orden={o} />
              <button
                type="button"
                className="btn btn-oscuro btn-chico mt-4"
                disabled={accion.ocupado}
                onClick={() => ejecutar(() => tecnico.tomar(o.id), `Tomaste la orden #${o.id}.`)}
              >
                Tomar orden
              </button>
            </article>
          ))}
        </section>
      )}

      {pestana === 'resumen' && (
        <>
          <Mensajes error={tablero.error} />
          {tablero.datos && (
            <div className="grid-indicadores">
              <Indicador titulo="Por iniciar" valor={tablero.datos.porIniciar} />
              <Indicador titulo="En atención" valor={tablero.datos.enAtencion} />
              <Indicador titulo="Resueltas" valor={tablero.datos.indicadores.resueltas} />
              <Indicador titulo="Con retraso" valor={tablero.datos.indicadores.retrasadas} tono={tablero.datos.indicadores.retrasadas ? 'tono-alerta' : ''} />
              <Indicador titulo="Tiempo promedio" valor={horas(tablero.datos.indicadores.tiempoPromedioHoras)} />
              <Indicador titulo="Cumplimiento" valor={porcentaje(tablero.datos.indicadores.cumplimientoPct)} />
              <Indicador titulo="Satisfacción" valor={promedio(tablero.datos.indicadores.satisfaccionPromedio)} detalle={`${tablero.datos.indicadores.evaluaciones} evaluaciones`} />
              <Indicador titulo="Disponibles en bandeja" valor={tablero.datos.disponiblesEnBandeja} />
            </div>
          )}
        </>
      )}
    </main>
  )
}

function OrdenActiva({ orden, ocupado, ejecutar }) {
  const [solucion, setSolucion] = useState('')
  const [errorSolucion, setErrorSolucion] = useState('')

  function registrar(e) {
    e.preventDefault()
    if (solucion.trim().length < 10) {
      setErrorSolucion('Describe la solución en al menos 10 caracteres.')
      return
    }
    setErrorSolucion('')
    ejecutar(() => tecnico.solucion(orden.id, solucion.trim()), `Orden #${orden.id} resuelta.`)
  }

  return (
    <article className="tarjeta">
      <DetalleOrden orden={orden} />
      {orden.estado === 'ASIGNADA' && (
        <button
          type="button"
          className="btn btn-oscuro btn-chico mt-4"
          disabled={ocupado}
          onClick={() => ejecutar(() => tecnico.iniciar(orden.id), `Iniciaste la atención de la orden #${orden.id}.`)}
        >
          Iniciar atención
        </button>
      )}
      {orden.estado === 'EN_ATENCION' && (
        <form onSubmit={registrar} className="mt-4 space-y-2">
          <label className="label" htmlFor={`solucion-${orden.id}`}>
            Solución aplicada
          </label>
          <textarea
            id={`solucion-${orden.id}`}
            className="campo min-h-24"
            maxLength={500}
            value={solucion}
            onChange={(e) => setSolucion(e.target.value)}
            placeholder="Qué se diagnosticó y qué se hizo para resolverlo…"
          />
          {errorSolucion && <p className="error-campo">{errorSolucion}</p>}
          <button className="btn btn-oscuro btn-chico" disabled={ocupado}>
            Registrar solución
          </button>
        </form>
      )}
      <Historial cargar={() => tecnico.historial(orden.id)} />
    </article>
  )
}
