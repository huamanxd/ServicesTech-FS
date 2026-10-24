import { BrowserRouter, Navigate, Route, Routes, useParams } from 'react-router-dom'
import Cuerpo from './componentes/Cuerpo'
import Restringida from './componentes/Restringida'
import { ProveedorAutenticacion } from './contexto/Autenticacion'
import Acceso from './pantallas/Acceso'
import Catalogo from './pantallas/Catalogo'
import Home from './pantallas/Home'
import Info from './pantallas/Info'
import NoEncontrado from './pantallas/NoEncontrado'
import Registro from './pantallas/Registro'
import PanelAdmin from './pantallas/admin/PanelAdmin'
import PanelCliente from './pantallas/cliente/PanelCliente'
import PanelTecnico from './pantallas/tecnico/PanelTecnico'
import { ROLES } from './util/roles'

/** Las rutas antiguas (/login/:tipo, /signup, /servicios) redirigen a las actuales para no romper enlaces. */
function RedireccionLogin() {
  const { tipo } = useParams()
  return <Navigate replace to={`/acceso/${tipo}`} />
}

export default function App() {
  return (
    <BrowserRouter>
      <ProveedorAutenticacion>
        <Routes>
          <Route element={<Home />}>
            <Route index element={<Cuerpo />} />
            <Route path="catalogo" element={<Catalogo />} />
            <Route path="registro" element={<Registro />} />
            <Route path="acceso/:tipo" element={<Acceso />} />
            <Route
              path="cliente"
              element={
                <Restringida rol={ROLES.CLIENTE}>
                  <PanelCliente />
                </Restringida>
              }
            />
            <Route
              path="tecnico"
              element={
                <Restringida rol={ROLES.TECNICO}>
                  <PanelTecnico />
                </Restringida>
              }
            />
            <Route
              path="admin"
              element={
                <Restringida rol={ROLES.ADMINISTRADOR}>
                  <PanelAdmin />
                </Restringida>
              }
            />

            <Route path="servicios" element={<Navigate replace to="/catalogo" />} />
            <Route path="signup" element={<Navigate replace to="/registro" />} />
            <Route path="login/:tipo" element={<RedireccionLogin />} />

            <Route
              path="nosotros"
              element={
                <Info titulo="Nosotros">
                  <p>ServicesTech: soporte, mantenimiento y soluciones tecnológicas para particulares y empresas en Lima, Perú.</p>
                </Info>
              }
            />
            <Route
              path="casos"
              element={
                <Info titulo="Casos de atención">
                  <p>Publicaremos casos reales solo con autorización de nuestros clientes.</p>
                </Info>
              }
            />
            <Route
              path="blog"
              element={
                <Info titulo="Blog">
                  <p>Próximamente compartiremos consejos para mantener tus equipos y sistemas.</p>
                </Info>
              }
            />
            <Route
              path="contacto"
              element={
                <Info titulo="Contacto">
                  <p>Escríbenos por WhatsApp al +51 928 536 863 o crea una cuenta para registrar tu solicitud en línea.</p>
                </Info>
              }
            />
            <Route
              path="privacidad"
              element={
                <Info titulo="Política de privacidad">
                  <p>Usamos tus datos (nombre, DNI, correo y celular) solo para gestionar tus solicitudes de servicio.</p>
                  <p>Las contraseñas se guardan cifradas y nunca se comparten con terceros.</p>
                </Info>
              }
            />
            <Route
              path="reclamaciones"
              element={
                <Info titulo="Libro de reclamaciones">
                  <p>Para registrar un reclamo comunícate al +51 928 536 863 o al correo hchristian6@live.com.</p>
                </Info>
              }
            />
            <Route path="*" element={<NoEncontrado />} />
          </Route>
        </Routes>
      </ProveedorAutenticacion>
    </BrowserRouter>
  )
}
