import { useMemo, useState } from 'react'
import { api, abrirPdf } from '../api/client.js'
import Icon from '../components/Icon.jsx'
import ReservaDetalle from '../components/ReservaDetalle.jsx'
import { Aviso, Cargando, EncabezadoPagina, Estado, Migas, Vacio, textoError, useCarga } from '../components/ui.jsx'
import { useToast } from '../components/Toast.jsx'
import { dinero, etiqueta, fechaHora } from '../utils/format.js'

const POR_PAGINA = 10
const ESTADOS = ['EXITOSO', 'PENDIENTE', 'FALLIDO', 'REEMBOLSADO']

export default function Pagos() {
  const avisar = useToast()
  const { datos, cargando, error, recargar } = useCarga(() => api.get('/api/pagos'), [])
  const [q, setQ] = useState('')
  const [estado, setEstado] = useState('')
  const [pagina, setPagina] = useState(1)
  const [reserva, setReserva] = useState(null)

  const filas = useMemo(() => {
    let l = datos || []
    if (estado) l = l.filter((p) => p.estado === estado)
    const t = q.trim().toLowerCase()
    if (t) l = l.filter((p) => [p.idPago, p.idReserva, p.referenciaTransaccion, etiqueta(p.metodo)].some((x) => String(x ?? '').toLowerCase().includes(t)))
    return [...l].sort((a, b) => String(b.fecha).localeCompare(String(a.fecha)))
  }, [datos, q, estado])

  const total = Math.max(1, Math.ceil(filas.length / POR_PAGINA))
  const actual = Math.min(pagina, total)
  const visibles = filas.slice((actual - 1) * POR_PAGINA, actual * POR_PAGINA)

  async function resolver(p, nuevo) {
    try {
      await api.patch(`/api/pagos/${p.idPago}/resolver`, { estado: nuevo })
      avisar(nuevo === 'EXITOSO' ? 'Pago aprobado.' : 'Pago marcado como fallido.')
      recargar()
    } catch (e) {
      avisar(textoError(e), 'error')
    }
  }

  return (
    <>
      <Migas items={[{ texto: 'Administración', to: '/admin' }, { texto: 'Pagos' }]} />
      <EncabezadoPagina icono="card" titulo="Pagos" subtitulo="Pagos registrados. Los pendientes se aprueban o rechazan desde aquí; para registrar uno nuevo abre la reserva." />
      <div className="tarjeta">
        <div className="barra-tabla">
          <div><b style={{ fontSize: 13 }}>PAGO</b><span className="contador">{filas.length} registros</span></div>
          <div className="input-icono buscar">
            <Icon name="search" size={16} />
            <input className="input" style={{ width: '100%', padding: '9px 12px 9px 38px', border: '1px solid var(--borde-fuerte)', borderRadius: 9, background: 'var(--superficie)' }} placeholder="Buscar por pago, reserva, referencia o método…" value={q} onChange={(e) => { setQ(e.target.value); setPagina(1) }} />
          </div>
          <div className="chips">
            <span className={`chip ${estado === '' ? 'activo' : ''}`} onClick={() => { setEstado(''); setPagina(1) }}>Todos</span>
            {ESTADOS.map((e) => (
              <span key={e} className={`chip ${estado === e ? 'activo' : ''}`} onClick={() => { setEstado(e); setPagina(1) }}>{etiqueta(e)}</span>
            ))}
          </div>
        </div>
        {error && <div style={{ padding: '0 16px' }}><Aviso>{textoError(error)}</Aviso></div>}
        {cargando ? <Cargando /> : filas.length === 0 ? <Vacio /> : (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead><tr><th className="num">N.º</th><th>Fecha</th><th>Reserva</th><th>Método</th><th className="num">Monto</th><th>Referencia</th><th>Estado</th><th className="acciones">Acciones</th></tr></thead>
              <tbody>
                {visibles.map((p) => (
                  <tr key={p.idPago}>
                    <td className="num">{p.idPago}</td>
                    <td>{fechaHora(p.fecha)}</td>
                    <td><button className="btn btn-enlace btn-chico" onClick={() => setReserva(p.idReserva)}>#{p.idReserva}</button></td>
                    <td>{etiqueta(p.metodo)}</td>
                    <td className="num">{dinero(p.monto)}</td>
                    <td>{p.referenciaTransaccion || '—'}</td>
                    <td><Estado valor={p.estado} /></td>
                    <td className="acciones">
                      {p.estado === 'PENDIENTE' && (
                        <>
                          <button className="btn btn-enlace btn-chico" onClick={() => resolver(p, 'EXITOSO')}><Icon name="check" size={14} /> Aprobar</button>
                          <button className="btn btn-enlace btn-chico peligro" onClick={() => resolver(p, 'FALLIDO')}><Icon name="x" size={14} /> Rechazar</button>
                        </>
                      )}
                      <button className="btn btn-enlace btn-chico" onClick={() => abrirPdf(`/api/documentos/pagos/${p.idPago}/comprobante`)}><Icon name="download" size={14} /> Comprobante</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <div className="pie-tabla">
          <span>Mostrando {filas.length === 0 ? 0 : (actual - 1) * POR_PAGINA + 1}–{Math.min(actual * POR_PAGINA, filas.length)} de {filas.length} pagos</span>
          <div className="paginas">
            <button className="pagina-btn" disabled={actual <= 1} onClick={() => setPagina(actual - 1)}>‹</button>
            <button className="pagina-btn activo">{actual}</button><span>de {total}</span>
            <button className="pagina-btn" disabled={actual >= total} onClick={() => setPagina(actual + 1)}>›</button>
          </div>
        </div>
      </div>
      {reserva && <ReservaDetalle idReserva={reserva} onCerrar={() => setReserva(null)} onCambio={recargar} />}
    </>
  )
}
