import { useMemo, useState } from 'react'
import { api } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from '../components/Icon.jsx'
import ReservaDetalle from '../components/ReservaDetalle.jsx'
import ReservaForm from '../components/ReservaForm.jsx'
import { Aviso, Cargando, EncabezadoPagina, Estado, Migas, Vacio, textoError, useCarga } from '../components/ui.jsx'
import { useToast } from '../components/Toast.jsx'
import { dinero, etiqueta, fecha } from '../utils/format.js'

const POR_PAGINA = 10
const ESTADOS = ['PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA']

export default function Reservas() {
  const { rol, sesion } = useAuth()
  const avisar = useToast()
  // El encargado solo ve las reservas de su prestador; admin y operador ven todas.
  const { datos, cargando, error, recargar } = useCarga(
    () => (rol === 'encargado' ? api.get('/api/reservas', { idPrestador: sesion.idPrestador }) : api.get('/api/reservas')),
    [rol, sesion?.idPrestador],
  )
  const [q, setQ] = useState('')
  const [estado, setEstado] = useState('')
  const [pagina, setPagina] = useState(1)
  const [orden, setOrden] = useState({ key: 'idReserva', asc: false })
  const [detalle, setDetalle] = useState(null)
  const [nueva, setNueva] = useState(false)

  const filas = useMemo(() => {
    let l = datos || []
    if (estado) l = l.filter((r) => r.estado === estado)
    const t = q.trim().toLowerCase()
    if (t) l = l.filter((r) => [r.idReserva, r.cliente, r.tour, r.plan].some((x) => String(x).toLowerCase().includes(t)))
    return [...l].sort((a, b) => {
      const x = a[orden.key]
      const y = b[orden.key]
      const c = typeof x === 'number' ? x - y : String(x ?? '').localeCompare(String(y ?? ''), 'es')
      return orden.asc ? c : -c
    })
  }, [datos, q, estado, orden])

  const total = Math.max(1, Math.ceil(filas.length / POR_PAGINA))
  const actual = Math.min(pagina, total)
  const visibles = filas.slice((actual - 1) * POR_PAGINA, actual * POR_PAGINA)
  const th = (key, label, num) => (
    <th className={`orden ${num ? 'num' : ''}`} onClick={() => setOrden((o) => ({ key, asc: o.key === key ? !o.asc : true }))}>
      {label} {orden.key === key ? (orden.asc ? '↑' : '↓') : ''}
    </th>
  )

  return (
    <>
      <Migas items={[{ texto: 'Administración', to: '/admin' }, { texto: 'Reservas' }]} />
      <EncabezadoPagina icono="calendar" titulo="Reservas" subtitulo="Reservas de los clientes: estado, pagos, participantes y cancelaciones.">
        <button className="btn btn-primario" onClick={() => setNueva(true)}><Icon name="plus" size={16} /> Crear reserva</button>
      </EncabezadoPagina>

      <div className="tarjeta">
        <div className="barra-tabla">
          <div><b style={{ fontSize: 13 }}>RESERVA</b><span className="contador">{filas.length} registros</span></div>
          <div className="input-icono buscar">
            <Icon name="search" size={16} />
            <input className="input" style={{ width: '100%', padding: '9px 12px 9px 38px', border: '1px solid var(--borde-fuerte)', borderRadius: 9, background: 'var(--superficie)' }} placeholder="Buscar por número, cliente, tour o plan…" value={q} onChange={(e) => { setQ(e.target.value); setPagina(1) }} />
          </div>
          <div className="chips">
            <span className={`chip ${estado === '' ? 'activo' : ''}`} onClick={() => { setEstado(''); setPagina(1) }}>Todas</span>
            {ESTADOS.map((e) => (
              <span key={e} className={`chip ${estado === e ? 'activo' : ''}`} onClick={() => { setEstado(e); setPagina(1) }}>{etiqueta(e)}</span>
            ))}
          </div>
        </div>
        {error && <div style={{ padding: '0 16px' }}><Aviso>{textoError(error)}</Aviso></div>}
        {cargando ? <Cargando /> : filas.length === 0 ? <Vacio /> : (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead>
                <tr>
                  {th('idReserva', 'N.º', true)}{th('cliente', 'Cliente')}{th('tour', 'Tour')}{th('fechaServicio', 'Servicio')}
                  {th('valorTotal', 'Valor', true)}{th('saldoPendiente', 'Saldo', true)}{th('estado', 'Estado')}
                  <th className="acciones">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {visibles.map((r) => (
                  <tr key={r.idReserva}>
                    <td className="num">{r.idReserva}</td>
                    <td className="fuerte">{r.cliente}</td>
                    <td>{r.tour}</td>
                    <td>{fecha(r.fechaServicio)}</td>
                    <td className="num">{dinero(r.valorTotal)}</td>
                    <td className="num">{dinero(r.saldoPendiente)}</td>
                    <td><Estado valor={r.estado} /></td>
                    <td className="acciones">
                      <button className="btn btn-enlace btn-chico" onClick={() => setDetalle(r.idReserva)}><Icon name="eye" size={14} /> Ver / gestionar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <div className="pie-tabla">
          <span>Mostrando {filas.length === 0 ? 0 : (actual - 1) * POR_PAGINA + 1}–{Math.min(actual * POR_PAGINA, filas.length)} de {filas.length} reservas</span>
          <div className="paginas">
            <button className="pagina-btn" disabled={actual <= 1} onClick={() => setPagina(actual - 1)}>‹</button>
            <button className="pagina-btn activo">{actual}</button><span>de {total}</span>
            <button className="pagina-btn" disabled={actual >= total} onClick={() => setPagina(actual + 1)}>›</button>
          </div>
        </div>
      </div>

      {detalle && <ReservaDetalle idReserva={detalle} onCerrar={() => setDetalle(null)} onCambio={recargar} />}
      {nueva && (
        <ReservaForm
          onCerrar={() => setNueva(false)}
          onCreada={(r) => {
            setNueva(false)
            avisar(`Reserva #${r.idReserva} creada por ${dinero(r.valorTotal)}.`)
            recargar()
            setDetalle(r.idReserva)
          }}
        />
      )}
    </>
  )
}
