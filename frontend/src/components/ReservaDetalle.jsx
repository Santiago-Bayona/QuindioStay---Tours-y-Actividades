import { useState } from 'react'
import { api, abrirPdf } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from './Icon.jsx'
import { Campo } from './Formulario.jsx'
import { Aviso, Cargando, Confirmar, Estado, Modal, textoError, useCarga } from './ui.jsx'
import { useToast } from './Toast.jsx'
import { dinero, edadEn, etiqueta, fecha, fechaHora } from '../utils/format.js'

const METODOS = ['TARJETA_CREDITO', 'TARJETA_DEBITO', 'PSE', 'TRANSFERENCIA', 'EFECTIVO']
const EN_LINEA = ['TARJETA_CREDITO', 'TARJETA_DEBITO', 'PSE']

/** Detalle de una reserva: datos, participantes, pagos y todas las acciones del ciclo de vida. */
export default function ReservaDetalle({ idReserva, onCerrar, onCambio }) {
  const { esStaff } = useAuth()
  const avisar = useToast()
  const reserva = useCarga(() => api.get(`/api/reservas/${idReserva}`), [idReserva])
  const pagos = useCarga(() => api.get('/api/pagos', { idReserva }), [idReserva])
  const [modo, setModo] = useState(null) // 'pago' | 'cancelar' | 'participante'
  const [ocupado, setOcupado] = useState(false)

  const r = reserva.datos
  const recargar = () => {
    reserva.recargar()
    pagos.recargar()
    onCambio?.()
  }

  async function ejecutar(fn, exito) {
    setOcupado(true)
    try {
      const res = await fn()
      avisar(typeof exito === 'function' ? exito(res) : exito)
      setModo(null)
      recargar()
    } catch (e) {
      avisar(textoError(e), 'error')
    } finally {
      setOcupado(false)
    }
  }

  const abierta = r && (r.estado === 'PENDIENTE' || r.estado === 'CONFIRMADA')

  return (
    <Modal titulo={`Reserva #${idReserva}`} ancho onCerrar={onCerrar} pie={<button className="btn" onClick={onCerrar}>Cerrar</button>}>
      {reserva.cargando && <Cargando />}
      {reserva.error && <Aviso>{textoError(reserva.error)}</Aviso>}
      {r && (
        <>
          <dl className="datos">
            <div className="dato"><dt>Estado</dt><dd><Estado valor={r.estado} /></dd></div>
            <div className="dato"><dt>Cliente</dt><dd>{r.cliente}</dd></div>
            <div className="dato"><dt>Tour</dt><dd>{r.tour}</dd></div>
            <div className="dato"><dt>Plan</dt><dd>{r.plan}</dd></div>
            <div className="dato"><dt>Fecha del servicio</dt><dd>{fecha(r.fechaServicio)}</dd></div>
            <div className="dato"><dt>Creada</dt><dd>{fechaHora(r.fechaCreacion)}</dd></div>
            <div className="dato"><dt>Canal</dt><dd>{etiqueta(r.canal)}</dd></div>
            {r.fechaCancelacion && <div className="dato"><dt>Cancelada</dt><dd>{fechaHora(r.fechaCancelacion)}</dd></div>}
            <div className="dato"><dt>Valor total</dt><dd>{dinero(r.valorTotal)}</dd></div>
            <div className="dato"><dt>Pagado</dt><dd>{dinero(r.totalPagado)}</dd></div>
            <div className="dato"><dt>Saldo pendiente</dt><dd><b>{dinero(r.saldoPendiente)}</b></dd></div>
          </dl>

          <div className="acciones-fila" style={{ margin: '18px 0' }}>
            {abierta && r.saldoPendiente > 0 && (
              <button className="btn btn-primario btn-chico" onClick={() => setModo('pago')}><Icon name="card" size={14} /> Registrar pago</button>
            )}
            {esStaff && r.estado === 'PENDIENTE' && (
              <button className="btn btn-chico" onClick={() => ejecutar(() => api.patch(`/api/reservas/${r.idReserva}/confirmar`), 'Reserva confirmada.')} disabled={ocupado}>
                <Icon name="check" size={14} /> Confirmar
              </button>
            )}
            {r.estado === 'PENDIENTE' && (
              <button className="btn btn-chico" onClick={() => setModo('participante')}><Icon name="plus" size={14} /> Agregar participante</button>
            )}
            {abierta && (
              <button className="btn btn-chico" style={{ color: 'var(--peligro)' }} onClick={() => setModo('cancelar')}><Icon name="x" size={14} /> Cancelar reserva</button>
            )}
            <button className="btn btn-chico" onClick={() => abrirPdf(`/api/documentos/reservas/${r.idReserva}/comprobante`)}><Icon name="download" size={14} /> Comprobante</button>
            <button className="btn btn-chico" onClick={() => abrirPdf(`/api/documentos/reservas/${r.idReserva}/factura`)}><Icon name="download" size={14} /> Factura</button>
            <button className="btn btn-chico" onClick={() => abrirPdf(`/api/documentos/reservas/${r.idReserva}/autorizacion-menores`)}><Icon name="download" size={14} /> Autorización de menores</button>
          </div>

          <div className="seccion-titulo"><h3>Participantes ({r.participantes.length})</h3></div>
          <div className="tarjeta tabla-contenedor" style={{ marginBottom: 18 }}>
            <table className="tabla">
              <thead><tr><th>#</th><th>Nombre</th><th>Documento</th><th>Nacimiento</th><th>Edad en la salida</th><th /></tr></thead>
              <tbody>
                {r.participantes.map((p) => (
                  <tr key={p.numParticipante}>
                    <td>{p.numParticipante}</td>
                    <td className="fuerte">{p.nombre}</td>
                    <td>{p.documento}</td>
                    <td>{fecha(p.fechaNacimiento)}</td>
                    <td>{edadEn(p.fechaNacimiento, r.fechaServicio)} años</td>
                    <td className="acciones">
                      {r.estado === 'PENDIENTE' && r.participantes.length > 1 && (
                        <button
                          className="btn btn-enlace btn-chico peligro"
                          disabled={ocupado}
                          onClick={() => ejecutar(() => api.del(`/api/reservas/${r.idReserva}/participantes/${p.numParticipante}`), 'Participante retirado; se recalculó el valor.')}
                        >
                          <Icon name="trash" size={14} /> Quitar
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="seccion-titulo"><h3>Pagos</h3></div>
          <div className="tarjeta tabla-contenedor">
            {pagos.cargando ? (
              <Cargando />
            ) : (pagos.datos || []).length === 0 ? (
              <div className="vacio">Aún no hay pagos registrados.</div>
            ) : (
              <table className="tabla">
                <thead><tr><th>#</th><th>Fecha</th><th>Método</th><th className="num">Monto</th><th>Referencia</th><th>Estado</th><th /></tr></thead>
                <tbody>
                  {pagos.datos.map((p) => (
                    <tr key={p.idPago}>
                      <td>{p.idPago}</td>
                      <td>{fechaHora(p.fecha)}</td>
                      <td>{etiqueta(p.metodo)}</td>
                      <td className="num">{dinero(p.monto)}</td>
                      <td>{p.referenciaTransaccion || '—'}</td>
                      <td><Estado valor={p.estado} /></td>
                      <td className="acciones">
                        <button className="btn btn-enlace btn-chico" onClick={() => abrirPdf(`/api/documentos/pagos/${p.idPago}/comprobante`)}><Icon name="download" size={14} /> PDF</button>
                        {esStaff && p.estado === 'PENDIENTE' && (
                          <>
                            <button className="btn btn-enlace btn-chico" disabled={ocupado} onClick={() => ejecutar(() => api.patch(`/api/pagos/${p.idPago}/resolver`, { estado: 'EXITOSO' }), 'Pago aprobado.')}>Aprobar</button>
                            <button className="btn btn-enlace btn-chico peligro" disabled={ocupado} onClick={() => ejecutar(() => api.patch(`/api/pagos/${p.idPago}/resolver`, { estado: 'FALLIDO' }), 'Pago marcado como fallido.')}>Rechazar</button>
                          </>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}

      {modo === 'pago' && r && <FormularioPago reserva={r} esStaff={esStaff} onCerrar={() => setModo(null)} onHecho={() => { setModo(null); avisar('Pago registrado.'); recargar() }} />}
      {modo === 'participante' && r && <FormularioParticipante reserva={r} onCerrar={() => setModo(null)} onHecho={() => { setModo(null); avisar('Participante agregado; se recalculó el valor.'); recargar() }} />}
      {modo === 'cancelar' && r && (
        <Confirmar
          titulo="Cancelar reserva"
          peligro
          textoOk="Cancelar reserva"
          ocupado={ocupado}
          mensaje={
            esStaff
              ? 'Cancelación por el operador: se reembolsa el 100 % de lo pagado.'
              : 'Si faltan más de 5 días para la salida se reembolsa el 80 % de lo pagado; si no, no hay reembolso.'
          }
          onCancelar={() => setModo(null)}
          onOk={() =>
            ejecutar(
              () => api.patch(`/api/reservas/${r.idReserva}/cancelar`, undefined, { porOperador: !!esStaff }),
              (res) => `Reserva cancelada. Reembolso: ${dinero(res.reembolso)}.`,
            )
          }
        />
      )}
    </Modal>
  )
}

function FormularioPago({ reserva, esStaff, onCerrar, onHecho }) {
  const [monto, setMonto] = useState(String(reserva.saldoPendiente ?? ''))
  const [metodo, setMetodo] = useState(esStaff ? 'EFECTIVO' : 'PSE')
  const [referencia, setReferencia] = useState('')
  const [estado, setEstado] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const enLinea = EN_LINEA.includes(metodo)

  async function enviar(e) {
    e.preventDefault()
    setError(null)
    if (!(Number(monto) > 0)) return setError('Escribe un monto mayor que cero.')
    if (Number(monto) > Number(reserva.saldoPendiente)) return setError(`El monto no puede superar el saldo pendiente (${dinero(reserva.saldoPendiente)}).`)
    if (enLinea && !referencia.trim()) return setError('Los pagos en línea requieren la referencia de la transacción.')
    setEnviando(true)
    try {
      await api.post('/api/pagos', {
        idReserva: reserva.idReserva,
        monto: Number(monto),
        metodo,
        referenciaTransaccion: referencia.trim() || null,
        estado: estado || null,
      })
      onHecho()
    } catch (err) {
      setError(textoError(err))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal
      titulo="Registrar pago"
      onCerrar={onCerrar}
      pie={<><button className="btn" type="button" onClick={onCerrar}>Cancelar</button><button className="btn btn-primario" form="form-pago" disabled={enviando}>{enviando ? 'Registrando…' : 'Registrar pago'}</button></>}
    >
      <form id="form-pago" onSubmit={enviar} noValidate>
        <Aviso>{error}</Aviso>
        <p className="texto-suave" style={{ marginTop: 0 }}>Saldo pendiente: <b>{dinero(reserva.saldoPendiente)}</b>. El primer pago exitoso confirma la reserva.</p>
        <Campo def={{ name: 'monto', label: 'Monto (COP)', type: 'number', step: '1000', required: true }} valor={monto} onCambio={(_, v) => setMonto(v)} />
        <Campo def={{ name: 'metodo', label: 'Método de pago', type: 'select', options: { enum: METODOS } }} valor={metodo} onCambio={(_, v) => setMetodo(v)} />
        <Campo def={{ name: 'ref', label: 'Referencia de la transacción', required: enLinea, maxLength: 100, help: enLinea ? 'Obligatoria en pagos en línea.' : 'Opcional.' }} valor={referencia} onCambio={(_, v) => setReferencia(v)} />
        {esStaff && (
          <Campo def={{ name: 'estado', label: 'Estado del pago', type: 'select', placeholder: 'Automático', options: { enum: ['EXITOSO', 'PENDIENTE', 'FALLIDO'] }, help: 'Déjalo en automático salvo que quieras forzar un estado.' }} valor={estado} onCambio={(_, v) => setEstado(v)} />
        )}
      </form>
    </Modal>
  )
}

function FormularioParticipante({ reserva, onCerrar, onHecho }) {
  const [p, setP] = useState({ nombre: '', documento: '', fechaNacimiento: '' })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  async function enviar(e) {
    e.preventDefault()
    if (!p.nombre.trim() || !p.documento.trim() || !p.fechaNacimiento) return setError('Completa todos los datos del participante.')
    setEnviando(true)
    try {
      await api.post(`/api/reservas/${reserva.idReserva}/participantes`, { nombre: p.nombre.trim(), documento: p.documento.trim(), fechaNacimiento: p.fechaNacimiento })
      onHecho()
    } catch (err) {
      setError(textoError(err))
    } finally {
      setEnviando(false)
    }
  }
  return (
    <Modal
      titulo="Agregar participante"
      onCerrar={onCerrar}
      pie={<><button className="btn" type="button" onClick={onCerrar}>Cancelar</button><button className="btn btn-primario" form="form-part" disabled={enviando}>{enviando ? 'Agregando…' : 'Agregar'}</button></>}
    >
      <form id="form-part" onSubmit={enviar} noValidate>
        <Aviso>{error}</Aviso>
        <p className="texto-suave" style={{ marginTop: 0 }}>Se vuelve a validar cupo, edad mínima y documento, y se recalcula el valor total.</p>
        <Campo def={{ name: 'nombre', label: 'Nombre completo', required: true, maxLength: 120 }} valor={p.nombre} onCambio={(_, v) => setP({ ...p, nombre: v })} />
        <Campo def={{ name: 'documento', label: 'Documento', required: true, maxLength: 30 }} valor={p.documento} onCambio={(_, v) => setP({ ...p, documento: v })} />
        <Campo def={{ name: 'fn', label: 'Fecha de nacimiento', type: 'date', required: true }} valor={p.fechaNacimiento} onCambio={(_, v) => setP({ ...p, fechaNacimiento: v })} />
      </form>
    </Modal>
  )
}
