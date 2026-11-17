import { useEffect, useMemo, useState } from 'react'
import { api } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from './Icon.jsx'
import { Campo } from './Formulario.jsx'
import { Aviso, Modal, textoError, useCarga } from './ui.jsx'
import { dinero, edadEn, etiqueta, fecha, hora } from '../utils/format.js'

const CANALES = ['MOSTRADOR', 'WEB', 'APP_MOVIL', 'TELEFONO', 'AGENCIA_INTERMEDIARIA']
const vacio = () => ({ nombre: '', documento: '', fechaNacimiento: '' })

/**
 * Formulario de nueva reserva. Muestra las reglas de negocio que el backend valida:
 * cupo disponible, edad mínima del tour, plan del mismo prestador y vigente en la fecha.
 * El valor total lo calcula el servidor (plan + participantes), por eso no se estima aquí.
 */
export default function ReservaForm({ idClienteFijo, idSalidaInicial, onCerrar, onCreada }) {
  const { esStaff } = useAuth()
  const [idCliente, setIdCliente] = useState(idClienteFijo || '')
  const [idSalida, setIdSalida] = useState(idSalidaInicial ? String(idSalidaInicial) : '')
  const [idPlan, setIdPlan] = useState('')
  const [canal, setCanal] = useState(esStaff ? 'MOSTRADOR' : 'WEB')
  const [participantes, setParticipantes] = useState([vacio()])
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const salidas = useCarga(() => api.get('/api/salidas/disponibles'), [])
  const tours = useCarga(() => api.get('/api/tours/disponibles'), [])

  const salida = useMemo(() => (salidas.datos || []).find((s) => String(s.idSalida) === String(idSalida)), [salidas.datos, idSalida])
  const tour = useMemo(() => (tours.datos || []).find((t) => t.idTour === salida?.idTour), [tours.datos, salida])

  const [planes, setPlanes] = useState([])
  useEffect(() => {
    setIdPlan('')
    if (!tour || !salida) {
      setPlanes([])
      return
    }
    let vivo = true
    api
      .get('/api/planes', { idPrestador: tour.idPrestador, fecha: salida.fechaSalida })
      .then((l) => vivo && setPlanes(l.filter((p) => p.estado === 'ACTIVO')))
      .catch(() => vivo && setPlanes([]))
    return () => {
      vivo = false
    }
  }, [tour?.idPrestador, salida?.fechaSalida]) // eslint-disable-line react-hooks/exhaustive-deps

  const cambiar = (i, campo, valor) => setParticipantes((l) => l.map((p, k) => (k === i ? { ...p, [campo]: valor } : p)))
  const cupos = salida?.cuposDisponibles ?? null
  const excede = cupos !== null && participantes.length > cupos

  async function enviar(e) {
    e.preventDefault()
    setError(null)
    if (!idCliente) return setError('Selecciona el cliente.')
    if (!salida) return setError('Selecciona la salida.')
    if (!idPlan) return setError('Selecciona un plan vigente para la fecha de la salida.')
    if (excede) return setError(`La salida solo tiene ${cupos} cupos disponibles.`)
    for (const [i, p] of participantes.entries()) {
      if (!p.nombre.trim() || !p.documento.trim() || !p.fechaNacimiento) return setError(`Completa los datos del participante ${i + 1}.`)
      const edad = edadEn(p.fechaNacimiento, salida.fechaSalida)
      if (tour && edad !== null && edad < tour.edadMinima) {
        return setError(`El participante ${i + 1} tendría ${edad} años en la salida y el tour exige mínimo ${tour.edadMinima}.`)
      }
    }
    const docs = participantes.map((p) => p.documento.trim())
    if (new Set(docs).size !== docs.length) return setError('Hay participantes con el mismo documento.')
    setEnviando(true)
    try {
      const r = await api.post('/api/reservas', {
        idCliente: Number(idCliente),
        idPlan: Number(idPlan),
        idSalida: Number(idSalida),
        canal,
        participantes: participantes.map((p) => ({ nombre: p.nombre.trim(), documento: p.documento.trim(), fechaNacimiento: p.fechaNacimiento })),
      })
      onCreada(r)
    } catch (err) {
      setError(textoError(err))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal
      titulo="Nueva reserva"
      ancho
      onCerrar={onCerrar}
      pie={
        <>
          <button className="btn" type="button" onClick={onCerrar}>Cancelar</button>
          <button className="btn btn-primario" form="form-reserva" disabled={enviando}>{enviando ? 'Reservando…' : 'Crear reserva'}</button>
        </>
      }
    >
      <form id="form-reserva" onSubmit={enviar} noValidate>
        <Aviso>{error}</Aviso>
        <div className="fila-campos">
          {esStaff && !idClienteFijo && (
            <Campo
              def={{ name: 'cliente', label: 'Cliente', type: 'select', required: true, options: { url: '/api/clientes', label: (c) => `${c.nombre} ${c.apellido} · ${c.numeroDocumento}`, value: 'idCliente' } }}
              valor={idCliente}
              onCambio={(_, v) => setIdCliente(v)}
            />
          )}
          <div className="campo">
            <label>Salida <span className="req">*</span></label>
            <select value={idSalida} onChange={(e) => setIdSalida(e.target.value)} className="input">
              <option value="">Seleccione…</option>
              {(salidas.datos || []).map((s) => (
                <option key={s.idSalida} value={s.idSalida}>
                  {s.tour} · {fecha(s.fechaSalida)} {hora(s.horaSalida)} · {s.idioma} · {s.cuposDisponibles} cupos
                </option>
              ))}
            </select>
          </div>
          <div className="campo">
            <label>Plan <span className="req">*</span></label>
            <select value={idPlan} onChange={(e) => setIdPlan(e.target.value)} className="input" disabled={!salida}>
              <option value="">{salida ? (planes.length ? 'Seleccione…' : 'No hay planes vigentes para esa fecha') : 'Primero elige la salida'}</option>
              {planes.map((p) => (
                <option key={p.idPlan} value={p.idPlan}>{p.nombre} · {etiqueta(p.tipo)} · {dinero(p.precio)}</option>
              ))}
            </select>
            <span className="ayuda">Solo se ofrecen planes activos del mismo prestador del tour y vigentes en la fecha de la salida.</span>
          </div>
          {esStaff && (
            <Campo def={{ name: 'canal', label: 'Canal de la reserva', type: 'select', options: { enum: CANALES } }} valor={canal} onCambio={(_, v) => setCanal(v)} />
          )}
        </div>

        {salida && tour && (
          <div className="aviso aviso-info">
            <Icon name="alert" size={16} />
            <div>
              <b>{tour.nombre}</b> · {tour.duracionHoras} h · precio por persona {dinero(tour.precioPersona)} ·{' '}
              {tour.edadMinima ? `edad mínima ${tour.edadMinima} años` : 'sin restricción de edad'} · {cupos} cupos disponibles de {salida.cupo}.
              El valor total lo calcula el sistema al crear la reserva.
            </div>
          </div>
        )}

        <div className="seccion-titulo" style={{ marginTop: 8 }}>
          <h3>Participantes ({participantes.length})</h3>
          <button type="button" className="btn btn-chico" onClick={() => setParticipantes((l) => [...l, vacio()])} disabled={cupos !== null && participantes.length >= cupos}>
            <Icon name="plus" size={14} /> Agregar participante
          </button>
        </div>
        {participantes.map((p, i) => (
          <div className="participante-form" key={i}>
            <div className="campo">
              {i === 0 && <label>Nombre completo</label>}
              <input className="input" placeholder={`Participante ${i + 1}`} value={p.nombre} onChange={(e) => cambiar(i, 'nombre', e.target.value)} maxLength={120} />
            </div>
            <div className="campo">
              {i === 0 && <label>Documento</label>}
              <input className="input" placeholder="Documento" value={p.documento} onChange={(e) => cambiar(i, 'documento', e.target.value)} maxLength={30} />
            </div>
            <div className="campo">
              {i === 0 && <label>Nacimiento</label>}
              <input className="input" type="date" value={p.fechaNacimiento} onChange={(e) => cambiar(i, 'fechaNacimiento', e.target.value)} />
            </div>
            <button type="button" className="btn btn-enlace peligro" disabled={participantes.length === 1} onClick={() => setParticipantes((l) => l.filter((_, k) => k !== i))} aria-label="Quitar participante">
              <Icon name="trash" size={15} />
            </button>
          </div>
        ))}
        {excede && <Aviso>La salida solo tiene {cupos} cupos disponibles.</Aviso>}
      </form>
    </Modal>
  )
}
