import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { http, mensajeError } from '../api/http'
import { useAutenticacion } from '../contexto/Autenticacion'
import { validarRegistro } from '../util/validaciones'
const inicial={ nombres:'',apellidos:'',email:'',telefono:'',clave:'',confirmar:'' }
export default function Signup() {
 const { usuario, entrar }=useAutenticacion(),navegar=useNavigate()
 const [form,setForm]=useState(inicial),[errores,setErrores]=useState({}),[error,setError]=useState(''),[ocupado,setOcupado]=useState(false)
 if(usuario)return <Navigate to={usuario.rol==='ADMIN'?'/admin':'/cliente'} replace />
 function cambiar(e){setForm({...form,[e.target.name]:e.target.value});setErrores({...errores,[e.target.name]:''})}
 async function enviar(e){e.preventDefault();const fallas=validarRegistro(form);setErrores(fallas);setError('');if(Object.keys(fallas).length)return
  setOcupado(true);try{const { confirmar,...datos }=form;void confirmar;const {data}=await http.post('/auth/registro',datos);entrar(data);navegar('/cliente',{replace:true})}catch(err){setError(mensajeError(err))}finally{setOcupado(false)} }
 return <main className="contenedor py-14"><div className="form-card max-w-xl"><span className="etiqueta">REGISTRO DE CLIENTES</span><h1 className="titulo-seccion mt-2">Crea tu cuenta</h1><p className="texto-apoyo mb-7">Tus datos se guardarán en PostgreSQL. Puedes gestionar solicitudes desde tu espacio.</p>
 <form noValidate className="space-y-4" onSubmit={enviar}><div className="grid sm:grid-cols-2 gap-4">{[['nombres','Nombres'],['apellidos','Apellidos']].map(([campo,titulo])=><div key={campo}><label className="label" htmlFor={campo}>{titulo} *</label><input id={campo} name={campo} className="campo" maxLength={90} value={form[campo]} onChange={cambiar} autoComplete={campo==='nombres'?'given-name':'family-name'} aria-invalid={!!errores[campo]}/>{errores[campo]&&<p className="error-campo">{errores[campo]}</p>}</div>)}</div>
 <div><label htmlFor="email" className="label">Correo electrónico *</label><input id="email" name="email" type="email" className="campo" maxLength={180} autoComplete="email" value={form.email} onChange={cambiar} aria-invalid={!!errores.email}/>{errores.email&&<p className="error-campo">{errores.email}</p>}</div><div><label htmlFor="telefono" className="label">Celular peruano (opcional)</label><input id="telefono" name="telefono" type="tel" inputMode="numeric" maxLength={9} placeholder="9XXXXXXXX" className="campo" value={form.telefono} onChange={cambiar} aria-invalid={!!errores.telefono}/>{errores.telefono&&<p className="error-campo">{errores.telefono}</p>}</div>
 <div className="grid sm:grid-cols-2 gap-4">{[['clave','Contraseña'],['confirmar','Repite la contraseña']].map(([campo,titulo])=><div key={campo}><label htmlFor={campo} className="label">{titulo} *</label><input className="campo" id={campo} name={campo} type="password" autoComplete="new-password" minLength={10} value={form[campo]} onChange={cambiar} aria-invalid={!!errores[campo]}/>{errores[campo]&&<p className="error-campo">{errores[campo]}</p>}</div>)}</div>
 <p className="text-xs text-slate-500">La contraseña debe tener al menos 10 caracteres. Nunca compartas tus claves.</p>{error&&<div className="alerta" role="alert">{error}</div>}<button type="submit" disabled={ocupado} className="btn btn-oscuro w-full justify-center">{ocupado?'Creando cuenta…':'Crear cuenta de cliente'}</button></form>
 <p className="text-sm text-slate-600 text-center mt-6">¿Ya tienes cuenta? <Link className="link" to="/acceso/cliente">Inicia sesión</Link></p></div></main>
}
