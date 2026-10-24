import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { publico } from '../api/portal'
import { Cargando, Mensajes, Vacio } from '../componentes/Ui'
import { useCarga } from '../hooks/useCarga'
import { SOLES } from '../util/formato'

const CATEGORIAS = ['EQUIPOS', 'SOFTWARE', 'REDES', 'SEGURIDAD', 'EMPRESAS']

export default function Catalogo() {
  const { datos: servicios, cargando, error } = useCarga(() => publico.servicios(), [])
  const [categoria, setCategoria] = useState('')
  const [busqueda, setBusqueda] = useState('')

  const visibles = useMemo(() => {
    const texto = busqueda.trim().toLowerCase()
    return (servicios ?? []).filter(
      (s) => (!categoria || s.categoria === categoria) && `${s.nombre} ${s.descripcion}`.toLowerCase().includes(texto),
    )
  }, [servicios, categoria, busqueda])

  return (
    <main className="contenedor py-14">
      <div className="mb-9">
        <span className="etiqueta">CATÁLOGO SERVICESTECH</span>
        <h1 className="titulo-pagina">Servicios técnicos para cada necesidad</h1>
        <p className="texto-apoyo max-w-2xl">
          Tarifas referenciales para Lima, Perú, en soles. El monto final depende del diagnóstico, el alcance y los
          materiales necesarios.
        </p>
      </div>

      <div className="flex flex-wrap gap-3 mb-8 items-center">
        <label className="sr-only" htmlFor="buscar">
          Buscar servicios
        </label>
        <input
          id="buscar"
          placeholder="Buscar por nombre o especialidad…"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          className="campo max-w-sm"
        />
        <label className="sr-only" htmlFor="categoria">
          Filtrar por categoría
        </label>
        <select id="categoria" value={categoria} onChange={(e) => setCategoria(e.target.value)} className="campo max-w-xs">
          <option value="">Todas las categorías</option>
          {CATEGORIAS.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>
      </div>

      {cargando && <Cargando texto="Cargando catálogo… (si el servidor estaba inactivo puede tardar hasta un minuto)" />}
      <Mensajes error={error} />

      {servicios && (
        <>
          <p className="text-slate-500 text-sm mb-4">{visibles.length} servicios disponibles</p>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {visibles.map((s) => (
              <article className="tarjeta servicio" key={s.id}>
                <div className="flex justify-between gap-2 items-center">
                  <span className="etiqueta">{s.categoria}</span>
                  <span className="text-slate-400 text-xs">{s.codigo}</span>
                </div>
                <h2 className="text-xl font-bold mt-4 mb-2">{s.nombre}</h2>
                <p className="text-slate-600 text-sm flex-1">{s.descripcion}</p>
                <div className="flex justify-between items-end mt-5 pt-4 border-t border-slate-200 gap-3">
                  <div>
                    <span className="block text-xs text-slate-500">Desde / referencial</span>
                    <strong className="text-2xl text-teal-800">{SOLES.format(s.precioReferencial)}</strong>
                    <span className="block text-xs text-slate-500">~{s.duracionMinutos} min estimados</span>
                  </div>
                  <Link className="btn btn-oscuro btn-chico" to={`/cliente?servicio=${s.id}`}>
                    Solicitar
                  </Link>
                </div>
              </article>
            ))}
          </div>
          {visibles.length === 0 && <Vacio>No hay servicios para esta búsqueda.</Vacio>}
        </>
      )}

      <p className="mt-8 text-sm text-slate-500">
        * Los precios no constituyen una oferta cerrada ni incluyen repuestos o licencias salvo indicación expresa.
        Solicitar un servicio no realiza ningún cobro.
      </p>
    </main>
  )
}
