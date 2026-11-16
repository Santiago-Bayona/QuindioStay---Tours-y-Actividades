import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import Icon from './Icon.jsx'
import { etiqueta } from '../utils/format.js'

/** Color de la etiqueta según el estado que llega del backend. */
const TONO = {
  ACTIVO: 'ok', CONFIRMADA: 'ok', EXITOSO: 'ok', REALIZADA: 'ok', ENVIADA: 'ok', COMPLETADA: 'ok',
  PENDIENTE: 'amarillo', PROGRAMADA: 'info', SUSPENDIDO: 'amarillo', LEIDA: 'info',
  CANCELADA: 'malo', FALLIDO: 'malo', FALLIDA: 'malo', RETIRADO: 'malo', BLOQUEADO: 'malo', INACTIVO: 'malo',
  REEMBOLSADO: 'info',
}
export function Estado({ valor }) {
  return <span className={`estado ${TONO[valor] || ''}`}>{etiqueta(valor)}</span>
}

export function Migas({ items }) {
  return (
    <div className="migas">
      {items.map((it, i) => (
        <span key={i}>
          {i > 0 && ' › '}
          {it.to ? <Link to={it.to}>{it.texto}</Link> : <b>{it.texto}</b>}
        </span>
      ))}
    </div>
  )
}

export function EncabezadoPagina({ icono, titulo, subtitulo, children }) {
  return (
    <div className="encabezado-pagina">
      {icono && (
        <div className="circulo">
          <Icon name={icono} size={24} />
        </div>
      )}
      <div>
        <h1>{titulo}</h1>
        {subtitulo && <p>{subtitulo}</p>}
      </div>
      <div className="espacio" />
      <div className="fila">{children}</div>
    </div>
  )
}

export function Modal({ titulo, onCerrar, children, pie, ancho }) {
  useEffect(() => {
    const f = (e) => e.key === 'Escape' && onCerrar()
    window.addEventListener('keydown', f)
    return () => window.removeEventListener('keydown', f)
  }, [onCerrar])
  return (
    <div className="fondo-modal" onMouseDown={(e) => e.target === e.currentTarget && onCerrar()}>
      <div className={`modal ${ancho ? 'ancho' : ''}`} role="dialog" aria-modal="true" aria-label={titulo}>
        <div className="modal-cab">
          <h2>{titulo}</h2>
          <button className="btn btn-enlace" onClick={onCerrar} aria-label="Cerrar">
            <Icon name="x" />
          </button>
        </div>
        <div className="modal-cuerpo">{children}</div>
        {pie && <div className="modal-pie">{pie}</div>}
      </div>
    </div>
  )
}

export function Confirmar({ titulo, mensaje, textoOk = 'Confirmar', peligro, onOk, onCancelar, ocupado }) {
  return (
    <Modal
      titulo={titulo}
      onCerrar={onCancelar}
      pie={
        <>
          <button className="btn" onClick={onCancelar} disabled={ocupado}>Cancelar</button>
          <button className={`btn ${peligro ? 'btn-peligro' : 'btn-primario'}`} onClick={onOk} disabled={ocupado}>
            {ocupado ? 'Procesando…' : textoOk}
          </button>
        </>
      }
    >
      <p style={{ margin: 0 }}>{mensaje}</p>
    </Modal>
  )
}

export function Aviso({ tipo = 'error', children }) {
  if (!children) return null
  return (
    <div className={`aviso aviso-${tipo}`}>
      <Icon name={tipo === 'error' ? 'alert' : 'check'} size={16} />
      <div>{children}</div>
    </div>
  )
}

export function Cargando({ texto = 'Cargando…' }) {
  return <div className="cargando">{texto}</div>
}

export function Vacio({ texto = 'No hay registros para mostrar.' }) {
  return <div className="vacio">{texto}</div>
}

/**
 * Carga datos de la API al montar (y cuando cambian las dependencias).
 * Devuelve { datos, cargando, error, recargar }.
 */
export function useCarga(funcion, deps = []) {
  const [estado, setEstado] = useState({ datos: null, cargando: true, error: null })
  const [version, setVersion] = useState(0)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const f = useCallback(funcion, deps)
  useEffect(() => {
    let vivo = true
    setEstado((s) => ({ ...s, cargando: true, error: null }))
    f()
      .then((d) => vivo && setEstado({ datos: d, cargando: false, error: null }))
      .catch((e) => vivo && setEstado({ datos: null, cargando: false, error: e }))
    return () => {
      vivo = false
    }
  }, [f, version])
  return { ...estado, recargar: () => setVersion((v) => v + 1) }
}

/** Texto de error para mostrar al usuario (incluye los errores por campo del backend). */
export function textoError(e) {
  if (!e) return null
  if (e.errores && Object.keys(e.errores).length) {
    return Object.entries(e.errores)
      .map(([k, v]) => `${k}: ${v}`)
      .join(' · ')
  }
  return e.message || 'Ocurrió un error inesperado'
}
