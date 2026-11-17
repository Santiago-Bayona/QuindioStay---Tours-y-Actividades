import { useState } from 'react'
import { api, abrirPdf } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from '../components/Icon.jsx'
import ReservaDetalle from '../components/ReservaDetalle.jsx'
import { Aviso, Cargando, EncabezadoPagina, Estado, Vacio, textoError, useCarga } from '../components/ui.jsx'
import { useToast } from '../components/Toast.jsx'
import { dinero, etiqueta, fecha, fechaHora } from '../utils/format.js'

/** Portal del cliente: sus reservas (con pago, cancelación y documentos). */
export function MisReservas() {
  const { sesion } = useAuth()
  const { datos, cargando, error, recargar } = useCarga(() => api.get('/api/reservas', { idCliente: sesion.id }), [sesion.id])
  const [detalle, setDetalle] = useState(null)
  const lista = [...(datos || [])].sort((a, b) => b.idReserva - a.idReserva)

  return (
    <>
      <EncabezadoPagina icono="calendar" titulo="Mis reservas" subtitulo="Paga, revisa o cancela tus reservas y descarga tus documentos." />
      {error && <Aviso>{textoError(error)}</Aviso>}
      <div className="tarjeta">
        {cargando ? <Cargando /> : lista.length === 0 ? <Vacio texto="Aún no tienes reservas. Explora los tours y reserva el primero." /> : (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead><tr><th className="num">N.º</th><th>Tour</th><th>Plan</th><th>Servicio</th><th className="num">Valor</th><th className="num">Saldo</th><th>Estado</th><th className="acciones" /></tr></thead>
              <tbody>
                {lista.map((r) => (
                  <tr key={r.idReserva}>
                    <td className="num">{r.idReserva}</td>
                    <td className="fuerte">{r.tour}</td>
                    <td>{r.plan}</td>
                    <td>{fecha(r.fechaServicio)}</td>
                    <td className="num">{dinero(r.valorTotal)}</td>
                    <td className="num">{dinero(r.saldoPendiente)}</td>
                    <td><Estado valor={r.estado} /></td>
                    <td className="acciones"><button className="btn btn-enlace btn-chico" onClick={() => setDetalle(r.idReserva)}><Icon name="eye" size={14} /> Ver</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {detalle && <ReservaDetalle idReserva={detalle} onCerrar={() => setDetalle(null)} onCambio={recargar} />}
    </>
  )
}

export function MisNotificaciones() {
  const { sesion } = useAuth()
  const avisar = useToast()
  const { datos, cargando, error, recargar } = useCarga(() => api.get('/api/notificaciones', { idCliente: sesion.id }), [sesion.id])
  const lista = [...(datos || [])].sort((a, b) => String(b.fechaProgramada).localeCompare(String(a.fechaProgramada))).slice(0, 60)

  async function leer(n) {
    try {
      await api.patch(`/api/notificaciones/${n.idNotificacion}/leida`)
      recargar()
    } catch (e) {
      avisar(textoError(e), 'error')
    }
  }

  return (
    <>
      <EncabezadoPagina icono="bell" titulo="Notificaciones" subtitulo="Recordatorios y avisos sobre tus reservas." />
      {error && <Aviso>{textoError(error)}</Aviso>}
      <div className="tarjeta">
        {cargando ? <Cargando /> : lista.length === 0 ? <Vacio texto="No tienes notificaciones." /> : lista.map((n) => (
          <div key={n.idNotificacion} className="salida-fila" style={{ padding: '14px 18px', borderTop: 'none', borderBottom: '1px solid var(--borde)' }}>
            <div className="info">
              <b>{etiqueta(n.tipo)}</b> · <span className="texto-suave">{etiqueta(n.canal)} · {fechaHora(n.fechaProgramada)}</span>
              <div style={{ marginTop: 2 }}>{n.mensaje}</div>
            </div>
            <Estado valor={n.estado} />
            {n.estado !== 'LEIDA' && n.estado !== 'PENDIENTE' && <button className="btn btn-chico" onClick={() => leer(n)}><Icon name="check" size={14} /> Marcar leída</button>}
          </div>
        ))}
      </div>
    </>
  )
}

export function MisDatos() {
  const { sesion } = useAuth()
  const { datos, cargando, error } = useCarga(() => api.get(`/api/clientes/${sesion.id}`), [sesion.id])
  const c = datos
  return (
    <>
      <EncabezadoPagina icono="user" titulo="Mis datos" subtitulo="Información de tu cuenta y preferencias.">
        {c && <button className="btn" onClick={() => abrirPdf(`/api/documentos/clientes/${c.idCliente}/autorizacion-datos`)}><Icon name="download" size={15} /> Autorización de datos</button>}
      </EncabezadoPagina>
      {error && <Aviso>{textoError(error)}</Aviso>}
      {cargando ? <Cargando /> : c && (
        <div className="tarjeta tarjeta-pad">
          <dl className="datos">
            <div className="dato"><dt>Nombre</dt><dd>{c.nombre} {c.apellido}</dd></div>
            <div className="dato"><dt>Documento</dt><dd>{etiqueta(c.tipoDocumento)} · {c.numeroDocumento}</dd></div>
            <div className="dato"><dt>Correo de la cuenta</dt><dd>{c.perfil?.correoCuenta}</dd></div>
            <div className="dato"><dt>Teléfono</dt><dd>{c.telefono}</dd></div>
            <div className="dato"><dt>Nacimiento</dt><dd>{fecha(c.fechaNacimiento)}</dd></div>
            <div className="dato"><dt>Ciudad</dt><dd>{c.ciudadOrigen}</dd></div>
            <div className="dato"><dt>Idioma</dt><dd>{etiqueta(c.perfil?.idiomaPreferencia)}</dd></div>
            <div className="dato"><dt>Canal de notificación</dt><dd>{etiqueta(c.perfil?.canalNotificacionPreferido)}</dd></div>
            <div className="dato"><dt>Recordatorios</dt><dd>{c.perfil?.anticipacionRecordatorios} días antes</dd></div>
            <div className="dato"><dt>Promociones</dt><dd>{etiqueta(!!c.perfil?.aceptaPromociones)}</dd></div>
          </dl>
        </div>
      )}
    </>
  )
}
