import { useState } from 'react'
import { mensajeError } from '../api/http'

/** Envoltorio para botones que llaman a la API: estado ocupado, error y mensaje de éxito. */
export function useAccion() {
  const [ocupado, setOcupado] = useState(false)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  async function ejecutar(funcion, mensajeExito = '') {
    setOcupado(true)
    setError('')
    setExito('')
    try {
      const resultado = await funcion()
      setExito(mensajeExito)
      return resultado
    } catch (e) {
      setError(mensajeError(e))
      return undefined
    } finally {
      setOcupado(false)
    }
  }

  const limpiar = () => {
    setError('')
    setExito('')
  }

  return { ocupado, error, exito, ejecutar, limpiar, setError }
}
