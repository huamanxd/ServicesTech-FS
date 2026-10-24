import { Link } from 'react-router-dom'

export default function Piedepagina() {
  return (
    <footer className="mx-auto w-full border-t border-gray-200 py-4 bg-white">
      <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-16 items-start py-3 px-4">
        <div>
          <h2 className="font-bold">Contacto</h2>
          <p>ServicesTech S.A.C.</p>
          <p>Lima, Perú</p>
          <p>
            Tel: <a className="text-blue-600" href="tel:+51928536863">+51 928 536 863</a>
          </p>
          <p>
            Correo: <a className="text-blue-600" href="mailto:hchristian6@live.com">hchristian6@live.com</a>
          </p>
        </div>
        <div>
          <h2 className="font-bold">Ubicación</h2>
          <p>Av. Canadá 1804, Lima</p>
          <div className="w-full max-w-sm h-32 rounded-md overflow-hidden border border-gray-300 mt-2">
            <iframe
              src="https://www.google.com/maps?q=Av.+Canada+1804,+Lima,+Peru&output=embed"
              className="w-full h-full"
              loading="lazy"
              title="Mapa de ubicación de ServicesTech"
            />
          </div>
        </div>
        <div>
          <h2 className="font-bold">Legal</h2>
          <p>
            <Link className="text-blue-600 hover:underline" to="/privacidad">
              Política de privacidad
            </Link>
          </p>
          <p>
            <Link className="text-blue-600 hover:underline" to="/reclamaciones">
              Libro de reclamaciones
            </Link>
          </p>
        </div>
      </div>
      <p className="container mx-auto pt-6 border-t border-gray-200 text-center text-sm text-gray-500">
        Todos los derechos reservados © ServicesTech
      </p>
    </footer>
  )
}
