import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext.jsx'
import { AdminLayout, PublicoLayout } from './components/Layouts.jsx'
import CrudPage from './crud/CrudPage.jsx'
import { ENTIDADES } from './crud/entities.jsx'
import Login from './pages/Login.jsx'
import Registro from './pages/Registro.jsx'
import Explorar from './pages/Explorar.jsx'
import Inicio from './pages/Inicio.jsx'
import Reservas from './pages/Reservas.jsx'
import Pagos from './pages/Pagos.jsx'
import { ReporteDetalle, ReportesMenu } from './pages/Reportes.jsx'
import { MisDatos, MisNotificaciones, MisReservas } from './pages/Cuenta.jsx'

/** Pantalla de gestión de una entidad; `soloAdmin` oculta las pantallas de catálogos a los demás roles. */
function Entidad({ clave, soloAdmin }) {
  const { rol } = useAuth()
  if (soloAdmin && rol !== 'admin') return <Navigate to="/admin" replace />
  return <CrudPage key={clave} config={ENTIDADES[clave]} />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/registro" element={<Registro />} />

      {/* Parte pública */}
      <Route element={<PublicoLayout />}>
        <Route index element={<Explorar />} />
      </Route>

      {/* Portal del cliente */}
      <Route path="/cuenta" element={<PublicoLayout protegido />}>
        <Route index element={<MisReservas />} />
        <Route path="notificaciones" element={<MisNotificaciones />} />
        <Route path="datos" element={<MisDatos />} />
      </Route>

      {/* Panel interno: administrador, encargado de prestador y operador */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Inicio />} />
        <Route path="reservas" element={<Reservas />} />
        <Route path="salidas" element={<Entidad clave="salidas" />} />
        <Route path="pagos" element={<Pagos />} />
        <Route path="notificaciones" element={<Entidad clave="notificaciones" />} />
        <Route path="clientes" element={<Entidad clave="clientes" />} />
        <Route path="tours" element={<Entidad clave="tours" />} />
        <Route path="planes" element={<Entidad clave="planes" />} />
        <Route path="guias" element={<Entidad clave="guias" />} />
        <Route path="prestadores" element={<Entidad clave="prestadores" />} />
        <Route path="municipios" element={<Entidad clave="municipios" soloAdmin />} />
        <Route path="tipos-prestador" element={<Entidad clave="tipos-prestador" soloAdmin />} />
        <Route path="categorias" element={<Entidad clave="categorias" soloAdmin />} />
        <Route path="idiomas" element={<Entidad clave="idiomas" soloAdmin />} />
        <Route path="roles" element={<Entidad clave="roles" soloAdmin />} />
        <Route path="usuarios" element={<Entidad clave="usuarios" soloAdmin />} />
        <Route path="reportes" element={<ReportesMenu />} />
        <Route path="reportes/:numero" element={<ReporteDetalle />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
