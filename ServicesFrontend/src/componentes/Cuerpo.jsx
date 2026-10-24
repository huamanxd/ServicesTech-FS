import { Link } from 'react-router-dom'

/**
 * Portada: banner de ServicesTech con acceso al catálogo.
 * La imagen se muestra completa (alto según su proporción, sin recortes).
 * En pantallas medianas o grandes el texto va encima de la imagen; en móviles, debajo.
 */
export default function Cuerpo() {
  return (
    <main className="flex-1">
      <div className="relative w-full">
        <img
          src="/img/chan.png"
          width="1671"
          height="941"
          className="block w-full h-auto"
          alt="Técnico de ServicesTech atendiendo un equipo"
        />
        <div className="bg-slate-900 md:absolute md:inset-0 md:bg-transparent md:bg-linear-to-r md:from-slate-950/70 md:via-slate-950/30 md:to-transparent md:flex md:items-center">
          <div className="px-5 py-8 md:py-4 md:ml-16 lg:ml-20 max-w-xl">
            <h1 className="text-sky-300 font-bold text-3xl md:text-4xl lg:text-6xl drop-shadow-lg">Soluciones tecnológicas</h1>
            <p className="text-orange-300 text-lg md:text-xl lg:text-2xl font-bold mt-4 drop-shadow-md">
              Servicio técnico profesional para tus equipos.
            </p>
            <Link
              to="/catalogo"
              className="inline-block mt-6 lg:mt-8 bg-sky-500 hover:bg-sky-600 text-white font-semibold border-2 border-sky-950 rounded-xl px-5 py-3"
            >
              Solicitar servicio
            </Link>
          </div>
        </div>
      </div>
    </main>
  )
}
