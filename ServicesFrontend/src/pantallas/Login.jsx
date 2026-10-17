import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate, useParams } from 'react-router-dom'
import { http, mensajeError } from '../api/http'
import { useAutenticacion } from '../contexto/Autenticacion'
import { validarAcceso } from '../util/validaciones'
export default function Login() {
 const { tipo }=useParams(), esPersonal=tipo==='personal', valido=esPersonal||tipo==='cliente'
 const { usuario, entrar }=useAutenticacion(), navegar=useNavigate(), location=useLocation()
 const [form,setForm]=useState({ email:'',clave:'' }),[errores,setErrores]=useState({}),[error,setError]=useState(''),[ocupado,setOcupado]=useState(false)
 if(!valido)return <Navigate to="/" replace />
 if(usuario)return <Navigate replace to={usuario.rol==='ADMIN'?'/admin':'/cliente'} />
 const cambiar=e=>{setForm({...form,[e.target.name]:e.target.value});setErrores({...errores,[e.target.name]:''})}
 async function enviar(e){e.preventDefault();const fallas=validarAcceso(form);setErrores(fallas);setError('');if(Object.keys(fallas).length)return
  setOcupado(true);try{const {data}=await http.post(esPersonal?'/auth/personal':'/auth/cliente',{email:form.email.trim(),clave:form.clave});entrar(data);const destino=location.state?.desde; navegar(destino&&(esPersonal?destino.startsWith('/admin'):destino.startsWith('/cliente'))?destino:esPersonal?'/admin':'/cliente',{replace:true})}catch(err){setError(mensajeError(err))}finally{setOcupado(false)} }
 return <main className="contenedor py-16"><div className="form-card"><div className={esPersonal?'form-insignia personal':'form-insignia'}>{esPersonal?'PERSONAL AUTORIZADO':'PORTAL DE CLIENTES'}</div><h1 className="titulo-seccion mt-3">{esPersonal?'Acceso para trabajadores':'Bienvenido de nuevo'}</h1><p className="texto-apoyo mb-6">{esPersonal?'Administración segura de servicios y solicitudes.':'Consulta tus atenciones y solicita soporte.'}</p>
 <div className="flex rounded-xl p-1 bg-slate-100 mb-6"><Link className={`selector ${!esPersonal?'seleccionado':''}`} to="/acceso/cliente">Cliente</Link><Link className={`selector ${esPersonal?'seleccionado':''}`} to="/acceso/personal">Personal</Link></div>
 <form onSubmit={enviar} noValidate className="space-y-4"><div><label htmlFor="email" className="label">Correo electrónico</label><input className="campo" id="email" name="email" type="email" autoComplete="username" value={form.email} onChange={cambiar} required aria-invalid={!!errores.email}/>{errores.email&&<p className="error-campo">{errores.email}</p>}</div><div><label htmlFor="clave" className="label">Contraseña</label><input className="campo" id="clave" name="clave" type="password" autoComplete="current-password" value={form.clave} onChange={cambiar} required aria-invalid={!!errores.clave}/>{errores.clave&&<p className="error-campo">{errores.clave}</p>}</div>
 {error&&<div className="alerta" role="alert">{error}</div>}<button className="btn btn-oscuro w-full justify-center" type="submit" disabled={ocupado}>{ocupado?'Validando…':'Ingresar al sistema'}</button></form>
 {esPersonal?<p className="text-xs text-slate-500 mt-6">El alta de trabajadores está reservada al administrador. No hay registro público de personal.</p>:<p className="text-sm text-slate-600 mt-6 text-center">¿Aún no tienes cuenta? <Link className="link" to="/registro">Regístrate aquí</Link></p>}
 </div></main>
}
