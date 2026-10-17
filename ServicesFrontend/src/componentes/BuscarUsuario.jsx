// Consulta de usuarios solo para administradores, sin información sensible.
import { useEffect, useState } from 'react'
import { http, mensajeError } from '../api/http.js'
import { useAutenticacion } from '../contexto/Autenticacion.jsx'
export default function BuscarUsuario() {
  const { usuario }=useAutenticacion()
  const [usuarios,setUsuarios]=useState([]),[error,setError]=useState(''),[cargando,setCargando]=useState(false)
  useEffect(()=>{if(usuario?.rol!=='ADMIN')setUsuarios([])},[usuario])
  async function buscarUsuario(){setError('');setCargando(true);try{const {data}=await http.get('/admin/usuarios');setUsuarios(data)}catch(e){setError(mensajeError(e))}finally{setCargando(false)}}
  if(usuario?.rol!=='ADMIN')return null
  return <section className="tarjeta"><button type="button" onClick={buscarUsuario} className="btn btn-oscuro">Ver usuarios registrados</button>
    {cargando&&<p role="status">Cargando...</p>}{error&&<p role="alert" className="alerta">{error}</p>}
    <ul className="mt-4 space-y-2">{usuarios.map(u=><li key={u.id} className="registro">{u.nombres} {u.apellidos} — {u.email} ({u.rol})</li>)}</ul>
  </section>
}
