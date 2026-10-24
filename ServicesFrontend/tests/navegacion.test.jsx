import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../src/api/portal', () => ({
  auth: { perfil: vi.fn(), ingresar: vi.fn(), salir: vi.fn(), registro: vi.fn() },
}))

import { auth } from '../src/api/portal'
import Restringida from '../src/componentes/Restringida'
import { ProveedorAutenticacion } from '../src/contexto/Autenticacion'
import Acceso from '../src/pantallas/Acceso'

function montar(ruta) {
  return render(
    <MemoryRouter initialEntries={[ruta]}>
      <ProveedorAutenticacion>
        <Routes>
          <Route path="/acceso/:tipo" element={<Acceso />} />
          <Route path="/admin" element={<Restringida rol="ADMINISTRADOR"><p>Panel admin</p></Restringida>} />
          <Route path="/tecnico" element={<Restringida rol="TECNICO"><p>Panel técnico</p></Restringida>} />
          <Route path="/cliente" element={<Restringida rol="CLIENTE"><p>Panel cliente</p></Restringida>} />
        </Routes>
      </ProveedorAutenticacion>
    </MemoryRouter>,
  )
}

describe('rutas protegidas y acceso', () => {
  beforeEach(() => vi.clearAllMocks())

  it('sin sesión, una ruta de personal lleva al acceso de personal', async () => {
    montar('/admin')
    expect(await screen.findByText(/Acceso para técnicos y administradores/)).toBeInTheDocument()
  })

  it('un técnico que entra por el portal de personal llega a su panel', async () => {
    auth.ingresar.mockResolvedValue({ token: 't', usuario: { id: 1, nombres: 'Luis', roles: ['TECNICO'] } })
    montar('/acceso/personal')
    await userEvent.type(screen.getByLabelText('Correo electrónico'), 'luis@st.pe')
    await userEvent.type(screen.getByLabelText('Contraseña'), 'ClaveTecnico2026')
    await userEvent.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(await screen.findByText('Panel técnico')).toBeInTheDocument()
    expect(auth.ingresar).toHaveBeenCalledWith('personal', { email: 'luis@st.pe', clave: 'ClaveTecnico2026' })
    expect(sessionStorage.getItem('st_token')).toBe('t')
  })

  it('un usuario con sesión pero sin el rol es enviado a su propio panel', async () => {
    sessionStorage.setItem('st_token', 'abc')
    auth.perfil.mockResolvedValue({ id: 2, nombres: 'Ana', roles: ['CLIENTE'] })
    montar('/admin')
    expect(await screen.findByText('Panel cliente')).toBeInTheDocument()
  })

  it('muestra el error del backend sin navegar', async () => {
    auth.ingresar.mockRejectedValue({ response: { status: 401, data: { message: 'Correo o contraseña incorrectos' } } })
    montar('/acceso/cliente')
    await userEvent.type(screen.getByLabelText('Correo electrónico'), 'ana@st.pe')
    await userEvent.type(screen.getByLabelText('Contraseña'), 'mala')
    await userEvent.click(screen.getByRole('button', { name: 'Ingresar' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Correo o contraseña incorrectos')
  })
})
