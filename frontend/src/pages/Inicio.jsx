import { Link } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from '../components/Icon.jsx'
import { Aviso, Cargando, Estado, textoError, useCarga } from '../components/ui.jsx'
import { dinero, fecha, hoyISO, hora } from '../utils/format.js'

export default function Inicio() {
  const { sesion, rol } = useAuth()
  const reservas = useCarga(() => (rol === 'encargado' ? api.get('/api/reservas', { idPrestador: sesion.idPrestador }) : api.get('/api/reservas')), [rol])
  const salidas = useCarga(() => api.get('/api/salidas/disponibles'), [])
  const pagos = useCarga(() => api.get('/api/pagos'), [])
  const pendientes = useCarga(() => api.get('/api/notificaciones/pendientes'), [])

  const r = reservas.datos || []
  const pagosPend = (pagos.datos || []).filter((p) => p.estado === 'PENDIENTE')
  const ingresos = (pagos.datos || []).filter((p) => p.estado === 'EXITOSO').reduce((s, p) => s + Number(p.monto), 0)
  const proximas = [...(salidas.datos || [])].sort((a, b) => `${a.fechaSalida}${a.horaSalida}`.localeCompare(`${b.fechaSalida}${b.horaSalida}`)).slice(0, 6)
  const error = reservas.error || salidas.error || pagos.error

  return (
    <>
      <div className="encabezado-pagina">
        <div>
          <h1>Hola, {sesion.nombre}</h1>
          <p>Resumen de la operación al {fecha(hoyISO())}.</p>
        </div>
        <div className="espacio" />
        <Link to="/admin/reservas" className="btn btn-primario"><Icon name="plus" size={16} /> Nueva reserva</Link>
      </div>
      {error && <Aviso>{textoError(error)}</Aviso>}

      <div className="metricas">
        <div className="tarjeta metrica oscura">
          <div className="etq">Ingresos recibidos</div>
          <div className="val">{pagos.cargando ? '…' : dinero(ingresos)}</div>
          <div className="nota">Pagos exitosos registrados</div>
        </div>
        <div className="tarjeta metrica">
          <div className="etq">Reservas</div>
          <div className="val">{reservas.cargando ? '…' : r.length}</div>
          <div className="nota">{r.filter((x) => x.estado === 'PENDIENTE').length} pendientes · {r.filter((x) => x.estado === 'CONFIRMADA').length} confirmadas</div>
        </div>
        <div className="tarjeta metrica">
          <div className="etq">Pagos por resolver</div>
          <div className="val">{pagos.cargando ? '…' : pagosPend.length}</div>
          <div className="nota"><Link to="/admin/pagos">Ir a pagos</Link></div>
        </div>
        <div className="tarjeta metrica">
          <div className="etq">Notificaciones pendientes</div>
          <div className="val">{pendientes.cargando ? '…' : (pendientes.datos || []).length}</div>
          <div className="nota"><Link to="/admin/notificaciones">Ir a notificaciones</Link></div>
        </div>
      </div>

      <div className="seccion-titulo"><h3>Próximas salidas con cupo</h3><Link to="/admin/salidas">Ver todas</Link></div>
      <div className="tarjeta">
        {salidas.cargando ? <Cargando /> : proximas.length === 0 ? <div className="vacio">No hay salidas programadas.</div> : (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead><tr><th>Tour</th><th>Fecha</th><th>Guía</th><th>Idioma</th><th className="num">Cupos libres</th><th>Estado</th></tr></thead>
              <tbody>
                {proximas.map((s) => (
                  <tr key={s.idSalida}>
                    <td className="fuerte">{s.tour}</td>
                    <td>{fecha(s.fechaSalida)} · {hora(s.horaSalida)}</td>
                    <td>{s.guia}</td>
                    <td>{s.idioma}</td>
                    <td className="num">{s.cuposDisponibles} / {s.cupo}</td>
                    <td><Estado valor={s.estado} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  )
}
