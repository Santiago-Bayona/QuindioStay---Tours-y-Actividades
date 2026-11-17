import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from '../components/Icon.jsx'
import ReservaForm from '../components/ReservaForm.jsx'
import { Aviso, Cargando, Vacio, textoError, useCarga } from '../components/ui.jsx'
import { useToast } from '../components/Toast.jsx'
import { dinero, etiqueta, fecha, hora } from '../utils/format.js'

/** Catálogo público de tours con sus próximas salidas. Reservar exige iniciar sesión como cliente. */
export default function Explorar() {
  const { sesion, esCliente, esStaff } = useAuth()
  const navegar = useNavigate()
  const avisar = useToast()
  const tours = useCarga(() => api.get('/api/tours/disponibles'), [])
  const salidas = useCarga(() => api.get('/api/salidas/disponibles'), [])
  const [q, setQ] = useState('')
  const [categoria, setCategoria] = useState('')
  const [reservar, setReservar] = useState(null)

  const categorias = useMemo(() => [...new Set((tours.datos || []).map((t) => t.categoria))].sort(), [tours.datos])
  const lista = useMemo(() => {
    const t = q.trim().toLowerCase()
    return (tours.datos || []).filter(
      (x) => (!categoria || x.categoria === categoria) && (!t || [x.nombre, x.descripcion, x.prestador, x.categoria].some((s) => String(s).toLowerCase().includes(t))),
    )
  }, [tours.datos, q, categoria])
  const salidasDe = (idTour) => (salidas.datos || []).filter((s) => s.idTour === idTour).sort((a, b) => a.fechaSalida.localeCompare(b.fechaSalida)).slice(0, 3)

  function quiere(idSalida) {
    if (!sesion) {
      navegar('/login', { state: { desde: '/' } })
      return
    }
    if (esStaff) {
      navegar('/admin/reservas')
      return
    }
    setReservar(idSalida)
  }

  return (
    <>
      <section className="hero">
        <h1>Vive el Quindío con quienes lo conocen</h1>
        <p>Tours y actividades locales: café, naturaleza, aventura y cultura, con guías certificados y cupos reales.</p>
      </section>
      <div className="fila" style={{ marginBottom: 22 }}>
        <div className="input-icono" style={{ flex: 1, minWidth: 240, maxWidth: 420 }}>
          <Icon name="search" size={16} />
          <input className="input" style={{ width: '100%', padding: '10px 12px 10px 38px', border: '1px solid var(--borde-fuerte)', borderRadius: 9, background: 'var(--superficie)' }} placeholder="Buscar tours, prestadores…" value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
        <div className="chips">
          <span className={`chip ${categoria === '' ? 'activo' : ''}`} onClick={() => setCategoria('')}>Todos</span>
          {categorias.map((c) => <span key={c} className={`chip ${categoria === c ? 'activo' : ''}`} onClick={() => setCategoria(c)}>{c}</span>)}
        </div>
      </div>

      {tours.error && <Aviso>{textoError(tours.error)}</Aviso>}
      {tours.cargando ? <Cargando /> : lista.length === 0 ? <Vacio texto="No encontramos tours con esos criterios." /> : (
        <div className="tours-grid">
          {lista.map((t, i) => {
            const prox = salidasDe(t.idTour)
            return (
              <article className="tarjeta tour-card" key={t.idTour}>
                <div className={`tour-img t${i % 4}`}><span className="estado" style={{ background: 'rgba(255,255,255,.9)', color: 'var(--texto)' }}>{t.categoria}</span></div>
                <div className="tour-cuerpo">
                  <h3>{t.nombre}</h3>
                  <div className="tour-meta">
                    <span><Icon name="coffee" size={13} />{t.prestador}</span>
                    <span><Icon name="clock" size={13} />{t.duracionHoras} h</span>
                    <span><Icon name="flag" size={13} />{etiqueta(t.dificultad)}</span>
                    <span><Icon name="users" size={13} />{t.edadMinima ? `Desde ${t.edadMinima} años` : 'Todas las edades'}</span>
                  </div>
                  <p className="texto-suave" style={{ margin: 0, fontSize: 13 }}>{t.descripcion.length > 150 ? `${t.descripcion.slice(0, 150)}…` : t.descripcion}</p>
                  <div className="precio">{dinero(t.precioPersona)} <small className="texto-suave" style={{ fontSize: 12, fontFamily: 'var(--sans)' }}>por persona</small></div>
                  <div>
                    {prox.length === 0 && <div className="texto-suave" style={{ fontSize: 13 }}>Sin salidas disponibles por ahora.</div>}
                    {prox.map((s) => (
                      <div className="salida-fila" key={s.idSalida}>
                        <div className="info"><b>{fecha(s.fechaSalida)}</b> · {hora(s.horaSalida)} · {s.idioma}<br /><span className="texto-suave">{s.cuposDisponibles} cupos · guía {s.guia}</span></div>
                        <button className="btn btn-primario btn-chico" onClick={() => quiere(s.idSalida)}>Reservar</button>
                      </div>
                    ))}
                  </div>
                </div>
              </article>
            )
          })}
        </div>
      )}

      {reservar && esCliente && (
        <ReservaForm
          idClienteFijo={sesion.id}
          idSalidaInicial={reservar}
          onCerrar={() => setReservar(null)}
          onCreada={(r) => {
            setReservar(null)
            avisar(`Reserva #${r.idReserva} creada por ${dinero(r.valorTotal)}. Falta el pago para confirmarla.`)
            navegar('/cuenta')
          }}
        />
      )}
    </>
  )
}
