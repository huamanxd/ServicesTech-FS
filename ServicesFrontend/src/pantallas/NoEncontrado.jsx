import { Link } from 'react-router-dom'
export default function NoEncontrado(){return <main className="contenedor py-24 text-center"><h1 className="text-5xl font-extrabold">404</h1><p className="mt-4 text-slate-600">Esta página no existe.</p><Link to="/" className="btn btn-oscuro mt-7">Volver al inicio</Link></main>}
