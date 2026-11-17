import { useState } from 'react'
import { Link, NavLink, Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from './Icon.jsx'

/** Menú del panel de administración. `roles` = quién lo ve (el backend igual valida cada operación). */
const TODOS = ['admin', 'encargado', 'operador']
export const MENU = [
  { grupo: 'General', items: [{ to: '/admin', icono: 'home', texto: 'Inicio', roles: TODOS, fin: true }] },
  {
    grupo: 'Operación',
    items: [
      { to: '/admin/reservas', icono: 'calendar', texto: 'Reservas', roles: TODOS },
      { to: '/admin/salidas', icono: 'flag', texto: 'Salidas', roles: TODOS },
      { to: '/admin/pagos', icono: 'card', texto: 'Pagos', roles: TODOS },
      { to: '/admin/notificaciones', icono: 'bell', texto: 'Notificaciones', roles: TODOS },
      { to: '/admin/clientes', icono: 'users', texto: 'Clientes', roles: TODOS },
    ],
  },
  {
    grupo: 'Oferta',
    items: [
      { to: '/admin/tours', icono: 'tour', texto: 'Tours', roles: TODOS },
      { to: '/admin/planes', icono: 'tag', texto: 'Planes', roles: TODOS },
      { to: '/admin/guias', icono: 'compass', texto: 'Guías', roles: TODOS },
      { to: '/admin/prestadores', icono: 'coffee', texto: 'Prestadores', roles: TODOS },
    ],
  },
  {
    grupo: 'Catálogos',
    items: [
      { to: '/admin/municipios', icono: 'pin', texto: 'Municipios', roles: ['admin'] },
      { to: '/admin/tipos-prestador', icono: 'list', texto: 'Tipos de prestador', roles: ['admin'] },
      { to: '/admin/categorias', icono: 'tag', texto: 'Categorías de tour', roles: ['admin'] },
      { to: '/admin/idiomas', icono: 'file', texto: 'Idiomas', roles: ['admin'] },
      { to: '/admin/roles', icono: 'lock', texto: 'Roles', roles: ['admin'] },
      { to: '/admin/usuarios', icono: 'user', texto: 'Usuarios del sistema', roles: ['admin'] },
    ],
  },
  { grupo: 'Análisis', items: [{ to: '/admin/reportes', icono: 'chart', texto: 'Reportes', roles: TODOS }] },
]

function iniciales(nombre = '') {
  return nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('')
}

function UsuarioChip() {
  const { sesion, cerrarSesion } = useAuth()
  const navegar = useNavigate()
  return (
    <div className="usuario-chip">
      <div className="avatar">{iniciales(sesion?.nombre)}</div>
      <div>
        <div className="nombre">{sesion?.nombre}</div>
        <div className="rol">{sesion?.tipo === 'CLIENTE' ? 'Cliente' : sesion?.rol}</div>
      </div>
      <button
        className="btn btn-chico"
        onClick={() => {
          cerrarSesion()
          navegar('/login')
        }}
        title="Cerrar sesión"
      >
        <Icon name="logout" size={15} /> Salir
      </button>
    </div>
  )
}

/** Panel para usuarios internos (administrador, encargado de prestador y operador). */
export function AdminLayout() {
  const { esStaff, rol } = useAuth()
  const [abierto, setAbierto] = useState(false)
  const ubicacion = useLocation()
  if (!esStaff) return <Navigate to="/login" state={{ desde: ubicacion.pathname }} replace />

  return (
    <div className="shell">
      <aside className={`lateral ${abierto ? 'abierto' : ''}`} onClick={() => setAbierto(false)}>
        <div className="logo">
          <Icon name="sprout" size={24} /> QuindioStay
        </div>
        {MENU.map((g) => {
          const items = g.items.filter((i) => i.roles.includes(rol))
          if (!items.length) return null
          return (
            <div key={g.grupo}>
              <div className="nav-grupo">{g.grupo}</div>
              {items.map((i) => (
                <NavLink key={i.to} to={i.to} end={i.fin} className={({ isActive }) => `nav-enlace ${isActive ? 'activo' : ''}`}>
                  <Icon name={i.icono} /> {i.texto}
                </NavLink>
              ))}
            </div>
          )
        })}
      </aside>
      <div className="principal">
        <header className="barra-superior">
          <button className="btn btn-chico btn-menu" onClick={() => setAbierto((a) => !a)} aria-label="Menú">
            <Icon name="menu" />
          </button>
          <span className="texto-suave">Administración</span>
          <div className="espacio" />
          <Link to="/" className="btn btn-chico">
            <Icon name="arrowLeft" size={14} /> Volver a explorar
          </Link>
          <UsuarioChip />
        </header>
        <main className="contenido">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

/** Barra superior de la parte pública y del portal del cliente. */
export function PublicoLayout({ protegido }) {
  const { sesion, esStaff, esCliente } = useAuth()
  const ubicacion = useLocation()
  if (protegido && !esCliente) return <Navigate to="/login" state={{ desde: ubicacion.pathname + ubicacion.search }} replace />
  return (
    <>
      <header className="publico-barra">
        <Link to="/" className="logo" style={{ textDecoration: 'none' }}>
          <Icon name="sprout" size={24} /> QuindioStay
        </Link>
        <nav>
          <NavLink to="/" end className={({ isActive }) => `nav-enlace ${isActive ? 'activo' : ''}`}>Explorar</NavLink>
          {esCliente && (
            <>
              <NavLink to="/cuenta" end className={({ isActive }) => `nav-enlace ${isActive ? 'activo' : ''}`}>Mis reservas</NavLink>
              <NavLink to="/cuenta/notificaciones" className={({ isActive }) => `nav-enlace ${isActive ? 'activo' : ''}`}>Notificaciones</NavLink>
              <NavLink to="/cuenta/datos" className={({ isActive }) => `nav-enlace ${isActive ? 'activo' : ''}`}>Mis datos</NavLink>
            </>
          )}
        </nav>
        <div className="espacio" />
        {sesion ? (
          <>
            {esStaff && (
              <Link to="/admin" className="btn btn-chico">
                <Icon name="home" size={14} /> Ir al panel
              </Link>
            )}
            <UsuarioChip />
          </>
        ) : (
          <>
            <Link to="/registro" className="btn btn-chico">Regístrate</Link>
            <Link to="/login" className="btn btn-chico btn-primario">Iniciar sesión</Link>
          </>
        )}
      </header>
      <main className="pagina-publica">
        <Outlet />
      </main>
    </>
  )
}
