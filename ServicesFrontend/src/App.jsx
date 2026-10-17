// Estructura ORIGINAL: App -> Home -> Cabecera / Cuerpo / Piedepagina.
// React Router reemplaza el estado local que cambiaba de pantalla.
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Home from './pantallas/Home.jsx'
import Login from './pantallas/Login.jsx'
import Signup from './pantallas/Signup.jsx'
import Catalogo from './pantallas/Catalogo.jsx'
import Servicios from './pantallas/Servicios.jsx'
import Cuerpo from './componentes/Cuerpo.jsx'
import Restringida from './componentes/Restringida.jsx'
import PanelCliente from './pantallas/PanelCliente.jsx'
import PanelAdmin from './pantallas/PanelAdmin.jsx'
import NoEncontrado from './pantallas/NoEncontrado.jsx'
import { ProveedorAutenticacion } from './contexto/Autenticacion.jsx'
import './App.css'

export default function App() {
  return <BrowserRouter><ProveedorAutenticacion><Routes>
    <Route element={<Home />}>
      <Route index element={<Cuerpo />} />
      <Route path="/servicios" element={<Servicios />} />
      <Route path="/catalogo" element={<Catalogo />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/registro" element={<Signup />} />
      <Route path="/login/:tipo" element={<Login />} />
      <Route path="/acceso/:tipo" element={<Login />} />
      <Route path="/cliente" element={<Restringida rol="CLIENTE"><PanelCliente /></Restringida>} />
      <Route path="/admin" element={<Restringida rol="ADMIN"><PanelAdmin /></Restringida>} />
      <Route path="/nosotros" element={<Info titulo="Nosotros" texto="ServicesTech: soporte, mantenimiento y soluciones tecnológicas para particulares y empresas en Lima, Perú." />} />
      <Route path="/casos" element={<Info titulo="Casos de atención" texto="Consulta nuestros servicios. Las referencias de casos reales se publicarán con autorización de sus clientes." />} />
      <Route path="/blog" element={<Info titulo="Blog" texto="Próximamente compartiremos consejos para mantener tus equipos y sistemas." />} />
      <Route path="/contacto" element={<Info titulo="Contacto" texto="Escríbenos por WhatsApp al +51 928 536 863 para solicitar información o coordinar una visita." />} />
      <Route path="*" element={<NoEncontrado />} />
    </Route>
  </Routes></ProveedorAutenticacion></BrowserRouter>
}
function Info({ titulo, texto }) {
  return <main className="contenedor py-16 min-h-[55vh]"><h1 className="titulo-pagina">{titulo}</h1><p className="texto-apoyo max-w-2xl">{texto}</p></main>
}
