import { Campo } from './Ui'

const CAMPOS = [
  ['nombres', 'Nombres', 'text', 'given-name', 90],
  ['apellidos', 'Apellidos', 'text', 'family-name', 90],
  ['dni', 'DNI', 'text', 'off', 8],
  ['telefono', 'Celular', 'tel', 'tel', 9],
  ['email', 'Correo electrónico', 'email', 'email', 180],
  ['clave', 'Contraseña (10 a 72 caracteres)', 'password', 'new-password', 72],
  ['confirmar', 'Repite la contraseña', 'password', 'new-password', 72],
]

/** Campos de datos personales compartidos por el registro de clientes y el alta de personal. */
export default function CamposPersona({ valores, errores, onCambio, prefijo = '', unaColumna = false  }) {
  return (
   <div className={unaColumna ? 'grid grid-cols-1 gap-4' : 'grid sm:grid-cols-2 gap-4'}>
    {CAMPOS.map(([nombre, texto, tipo, autocompletar, maximo]) => (
        <Campo key={nombre} id={`${prefijo}${nombre}`} etiqueta={`${texto} *`} error={errores[nombre]}>
          <input
            id={`${prefijo}${nombre}`}
            name={nombre}
            type={tipo}
            className="campo"
            maxLength={maximo}
            autoComplete={autocompletar}
            inputMode={nombre === 'dni' || nombre === 'telefono' ? 'numeric' : undefined}
            placeholder={nombre === 'telefono' ? '9XXXXXXXX' : undefined}
            value={valores[nombre]}
            onChange={onCambio}
            aria-invalid={Boolean(errores[nombre])}
          />
        </Campo>
      ))}
    </div>
  )
}
