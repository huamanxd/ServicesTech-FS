import { BarrasConteo, Indicador } from '../../componentes/Ui'
import { horas, porcentaje, promedio } from '../../util/formato'

export default function ResumenCliente({ datos, onVerHistorial }) {
  const i = datos.indicadores
  return (
    <div className="space-y-6">
      {datos.pendientesDeCalificar > 0 && (
        <div className="aviso flex flex-wrap items-center justify-between gap-3">
          <span>
            Tienes {datos.pendientesDeCalificar} servicio(s) resuelto(s) por calificar. Tu opinión nos ayuda a mejorar.
          </span>
          <button type="button" className="btn btn-chico btn-oscuro" onClick={onVerHistorial}>
            Calificar ahora
          </button>
        </div>
      )}
      <div className="grid-indicadores">
        <Indicador titulo="Solicitudes" valor={datos.solicitudes} />
        <Indicador titulo="Atenciones en curso" valor={i.abiertas} />
        <Indicador titulo="Resueltas" valor={i.resueltas} />
        <Indicador titulo="Con retraso" valor={i.retrasadas} tono={i.retrasadas ? 'tono-alerta' : ''} />
        <Indicador titulo="Tiempo promedio de atención" valor={horas(i.tiempoPromedioHoras)} />
        <Indicador titulo="Cumplimiento de plazos" valor={porcentaje(i.cumplimientoPct)} />
        <Indicador titulo="Tu calificación promedio" valor={promedio(i.satisfaccionPromedio)} detalle={`${i.evaluaciones} evaluaciones`} />
      </div>
      <BarrasConteo titulo="Tus atenciones por estado" conteos={datos.porEstado} />
    </div>
  )
}
