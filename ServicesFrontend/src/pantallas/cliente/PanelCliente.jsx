import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { cliente } from '../../api/portal'
import { Cargando, Mensajes, Pestanas } from '../../componentes/Ui'
import { useAutenticacion } from '../../contexto/Autenticacion'
import { useCarga } from '../../hooks/useCarga'
import MisSolicitudes from './MisSolicitudes'
import NuevaSolicitud from './NuevaSolicitud'
import ResumenCliente from './ResumenCliente'

const PESTANAS = [
  ['resumen', 'Resumen'],
  ['nueva', 'Nueva solicitud'],
  ['historial', 'Mis solicitudes'],
]

/** Espacio del cliente: solo ve su propia información (el backend filtra por el token). */
export default function PanelCliente() {
  const { usuario } = useAutenticacion()
  const [parametros] = useSearchParams()
  const servicioInicial = parametros.get('servicio') ?? ''
  const [pestana, setPestana] = useState(servicioInicial ? 'nueva' : 'resumen')
  const tablero = useCarga(() => cliente.dashboard(), [])
  const solicitudes = useCarga(() => cliente.solicitudes(), [])

  function refrescar() {
    tablero.recargar()
    solicitudes.recargar()
  }

  return (
    <main className="contenedor py-12">
      <span className="etiqueta">ESPACIO DE CLIENTE</span>
      <h1 className="titulo-pagina">¡Hola, {usuario.nombres}!</h1>
      <p className="texto-apoyo mb-8">Registra solicitudes, sigue cada atención y califica el servicio recibido.</p>
      <Pestanas opciones={PESTANAS} activa={pestana} onCambio={setPestana} etiquetaGrupo="Secciones del cliente" />

      {pestana === 'resumen' && (
        <>
          <Mensajes error={tablero.error} />
          {tablero.cargando && !tablero.datos ? <Cargando /> : tablero.datos && <ResumenCliente datos={tablero.datos} onVerHistorial={() => setPestana('historial')} />}
        </>
      )}
      {pestana === 'nueva' && (
        <NuevaSolicitud
          servicioInicial={servicioInicial}
          onCreada={() => {
            refrescar()
            setPestana('historial')
          }}
        />
      )}
      {pestana === 'historial' && <MisSolicitudes estado={solicitudes} onCambio={refrescar} />}
    </main>
  )
}
