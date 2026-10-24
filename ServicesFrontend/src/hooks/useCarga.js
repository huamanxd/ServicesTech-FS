import { useCallback, useEffect, useRef, useState } from 'react'
import { mensajeError } from '../api/http'

/**
 * Ejecuta una función asíncrona al montar, cuando cambian las dependencias o al llamar recargar().
 * Las respuestas que llegan tarde (componente desmontado o petición reemplazada) se ignoran.
 */
export function useCarga(funcion, dependencias = []) {
  const funcionActual = useRef(funcion)
  const [version, setVersion] = useState(0)
  const [estado, setEstado] = useState({ datos: null, error: '', peticion: null })

  const peticion = `${JSON.stringify(dependencias)}#${version}`

  useEffect(() => {
    funcionActual.current = funcion
  })

  useEffect(() => {
    let vigente = true
    funcionActual
      .current()
      .then((datos) => {
        if (vigente) setEstado({ datos, error: '', peticion })
      })
      .catch((e) => {
        if (vigente) setEstado((previo) => ({ datos: previo.datos, error: mensajeError(e), peticion }))
      })
    return () => {
      vigente = false
    }
  }, [peticion])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])

  return { datos: estado.datos, error: estado.error, cargando: estado.peticion !== peticion, recargar }
}
