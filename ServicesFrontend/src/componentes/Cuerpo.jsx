// Se mantiene el banner, imagen y estilo del index original, cambiando el botón a una ruta real.
import { Link } from 'react-router-dom'
export default function Cuerpo() {
  return <main className="flex-1 min-h-[70vh]">
    <div className="relative w-full min-h-[70vh] h-full">
      <img src="/img/chan.png" className="absolute inset-0 w-full h-full object-cover object-center" alt="Servicio técnico de ServicesTech" />
      <div className="absolute inset-0 bg-black/20 flex items-center">
        <div className="ml-5 md:ml-20 max-w-xl p-4">
          <h1 className="text-sky-300 font-bold text-4xl md:text-6xl drop-shadow-lg">Soluciones tecnológicas</h1>
          <p className="text-orange-300 py-4 text-lg font-bold md:text-2xl mt-4 drop-shadow-md">Servicio técnico profesional para tus equipos.</p>
          <Link to="/servicios" className="inline-block mt-8 bg-sky-500 hover:bg-sky-600 text-white font-semibold border-2 border-sky-950 rounded-xl px-5 py-3">Solicitar Servicio</Link>
        </div>
      </div>
    </div>
  </main>
}
