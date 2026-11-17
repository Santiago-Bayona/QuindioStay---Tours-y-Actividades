import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { Campo, escribir, erroresDeApi, leer, validar } from '../components/Formulario.jsx'
import Icon from '../components/Icon.jsx'
import { Aviso, textoError } from '../components/ui.jsx'
import { useToast } from '../components/Toast.jsx'
import { ENTIDADES } from '../crud/entities.jsx'

/**
 * Auto-registro de clientes (el POST /api/clientes es público). Reutiliza los campos del CRUD de clientes.
 * En el modelo, el correo de contacto (cliente) y el de la cuenta (perfil_cliente) son datos distintos
 * porque el perfil es opcional; en el registro en línea se pide uno solo y se usa para ambos.
 */
export default function Registro() {
  const { sesion, iniciarSesion } = useAuth()
  const navegar = useNavigate()
  const avisar = useToast()
  const c = ENTIDADES.clientes
  const campos = c.fields
      .filter((f) => f.name !== 'perfil.correoCuenta')
      .map((f) => {
        if (f.name === 'correo') return { ...f, label: 'Correo', help: 'También es el correo con el que inicias sesión.' }
        if (f.name === 'perfil.contrasena') return { ...f, required: true, label: 'Contraseña', help: 'Mínimo 8 caracteres.' }
        return f
      })
  const [valores, setValores] = useState(() => {
    let v = {}
    campos.forEach((f) => {
      if (f.default !== undefined) v = escribir(v, f.name, f.default)
    })
    return v
  })
  const [errores, setErrores] = useState({})
  const [mensaje, setMensaje] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (sesion) return <Navigate to={sesion.tipo === 'USUARIO' ? '/admin' : '/cuenta'} replace />

  async function enviar(e) {
    e.preventDefault()
    setMensaje(null)
    const errs = validar(campos, valores)
    if (Object.keys(errs).length) return setErrores(errs)
    // El correo de la cuenta es el mismo correo de contacto
    const datos = escribir(valores, 'perfil.correoCuenta', valores.correo)
    setEnviando(true)
    try {
      await api.post('/api/clientes', c.aBody(datos))
      avisar('Cuenta creada. ¡Bienvenido a QuindioStay!')
      await iniciarSesion(datos.perfil.correoCuenta, datos.perfil.contrasena)
      navegar('/', { replace: true })
    } catch (err) {
      const errsApi = erroresDeApi(err)
      // Si el backend rechaza el correo de la cuenta, se muestra en el único campo de correo
      if (errsApi['perfil.correoCuenta']) {
        errsApi.correo = errsApi['perfil.correoCuenta']
        delete errsApi['perfil.correoCuenta']
      }
      setErrores(errsApi)
      setMensaje(textoError(err))
    } finally {
      setEnviando(false)
    }
  }

  return (
      <div className="login-pagina">
        <div className="login-cab">
          <div className="logo"><Icon name="sprout" size={24} /> QuindioStay</div>
          <Link to="/login" className="btn btn-enlace">Ya tengo cuenta <Icon name="arrow" size={14} /></Link>
        </div>
        <div style={{ maxWidth: 820, width: '100%', margin: '0 auto', flex: 1, padding: '24px 0' }}>
          <h1 style={{ fontSize: 36, marginBottom: 6 }}>Crea tu cuenta</h1>
          <p className="texto-suave" style={{ marginBottom: 22 }}>Regístrate para reservar tours y actividades en el Quindío.</p>
          <form className="tarjeta tarjeta-pad" onSubmit={enviar} noValidate>
            <Aviso>{mensaje}</Aviso>
            <div className="fila-campos">
              {campos.map((f) => (
                  <Campo
                      key={f.name}
                      def={f}
                      valor={leer(valores, f.name)}
                      error={errores[f.name]}
                      onCambio={(n, v) => {
                        setValores((x) => escribir(x, n, v))
                        setErrores((x) => ({ ...x, [n]: undefined }))
                      }}
                  />
              ))}
            </div>
            <button className="btn btn-primario btn-ancho" disabled={enviando}>{enviando ? 'Creando cuenta…' : 'Crear cuenta'}</button>
          </form>
        </div>
      </div>
  )
}
