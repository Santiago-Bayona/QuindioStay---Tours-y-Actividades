import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from '../components/Icon.jsx'
import { Aviso } from '../components/ui.jsx'

export default function Login() {
  const { sesion, iniciarSesion } = useAuth()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [usuario, setUsuario] = useState('')
  const [clave, setClave] = useState('')
  const [ver, setVer] = useState(false)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (sesion) return <Navigate to={sesion.tipo === 'USUARIO' ? '/admin' : '/cuenta'} replace />

  async function enviar(e) {
    e.preventDefault()
    setError(null)
    if (!usuario.trim() || !clave) {
      setError('Escribe tu usuario o correo y tu contraseña.')
      return
    }
    setEnviando(true)
    try {
      const s = await iniciarSesion(usuario, clave)
      const destino = ubicacion.state?.desde
      navegar(destino || (s.tipo === 'USUARIO' ? '/admin' : '/cuenta'), { replace: true })
    } catch (err) {
      setError(err.estado === 401 || err.estado === 400 ? err.message : err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="login-pagina">
      <div className="login-cab">
        <div className="logo">
          <Icon name="sprout" size={24} /> QuindioStay
        </div>
        <Link to="/" className="btn btn-enlace">
          Volver a explorar <Icon name="arrow" size={14} />
        </Link>
      </div>

      <div className="login-cuerpo">
        <div className="login-foto">
          <span className="lugar">
            <Icon name="pin" size={13} /> Quindío, Colombia
          </span>
          <div className="sello">— Experiencias con raíces</div>
          <h2>Tu próxima pausa comienza aquí.</h2>
          <p>Entre montañas, aroma a café y lugares que se sienten como en casa.</p>
        </div>

        <form className="login-form" onSubmit={enviar} noValidate>
          <div className="icono-cafe">
            <Icon name="coffee" size={20} />
          </div>
          <h1>Qué bueno tenerte aquí</h1>
          <p className="sub">Inicia sesión y encuentra tu próximo refugio en el corazón del Eje Cafetero.</p>

          <Aviso>{error}</Aviso>

          <div className="campo">
            <label htmlFor="usuario">Usuario o correo electrónico</label>
            <div className="input-icono">
              <Icon name="mail" size={16} />
              <input
                id="usuario"
                type="text"
                autoComplete="username"
                placeholder="usuario o tucorreo@ejemplo.com"
                value={usuario}
                onChange={(e) => setUsuario(e.target.value)}
                autoFocus
              />
            </div>
          </div>

          <div className="campo">
            <label htmlFor="clave">Contraseña</label>
            <div className="input-icono">
              <Icon name="lock" size={16} />
              <input
                id="clave"
                type={ver ? 'text' : 'password'}
                autoComplete="current-password"
                placeholder="Ingresa tu contraseña"
                value={clave}
                onChange={(e) => setClave(e.target.value)}
              />
              <button type="button" className="btn btn-enlace ojo" onClick={() => setVer((v) => !v)} aria-label="Mostrar contraseña">
                <Icon name="eye" size={16} />
              </button>
            </div>
          </div>

          <button className="btn btn-primario btn-ancho" disabled={enviando}>
            {enviando ? 'Ingresando…' : (<>Iniciar sesión <Icon name="arrow" size={16} /></>)}
          </button>

          <div className="login-pie">
            ¿Aún no tienes cuenta? <Link to="/registro"><b>Regístrate</b></Link>
          </div>
        </form>
      </div>

      <div className="pie-pagina">
        <span>© 2026 QuindioStay. Hecho con calma, en el Quindío.</span>
        <span>Proyecto Bases de Datos I · Universidad del Quindío</span>
      </div>
    </div>
  )
}
