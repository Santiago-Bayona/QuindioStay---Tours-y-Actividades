import { useEffect, useState } from 'react'
import { api } from '../api/client.js'
import { etiqueta } from '../utils/format.js'

// ---------- utilidades para nombres con punto ("perfil.correoCuenta") ----------
export function leer(obj, ruta) {
  return ruta.split('.').reduce((o, k) => (o == null ? undefined : o[k]), obj)
}
export function escribir(obj, ruta, valor) {
  const claves = ruta.split('.')
  const copia = { ...obj }
  let actual = copia
  claves.forEach((k, i) => {
    if (i === claves.length - 1) actual[k] = valor
    else {
      actual[k] = { ...(actual[k] || {}) }
      actual = actual[k]
    }
  })
  return copia
}

const cacheOpciones = new Map()
/** Carga las opciones de un select: lista fija (enum) o una URL de la API. */
export function useOpciones(spec) {
  const [opciones, setOpciones] = useState(() => (spec?.enum ? spec.enum.map((v) => ({ valor: v, texto: etiqueta(v) })) : []))
  useEffect(() => {
    if (!spec) return
    if (spec.enum) {
      setOpciones(spec.enum.map((v) => ({ valor: v, texto: etiqueta(v) })))
      return
    }
    let vivo = true
    const clave = spec.url + JSON.stringify(spec.params || {})
    const aplicar = (lista) =>
      vivo &&
      setOpciones(
        lista.map((x) => ({
          valor: x[spec.value || 'id'],
          texto: typeof spec.label === 'function' ? spec.label(x) : x[spec.label || 'nombre'],
        })),
      )
    if (cacheOpciones.has(clave) && !spec.sinCache) aplicar(cacheOpciones.get(clave))
    api
      .get(spec.url, spec.params)
      .then((lista) => {
        cacheOpciones.set(clave, lista)
        aplicar(lista)
      })
      .catch(() => {})
    return () => {
      vivo = false
    }
  }, [spec?.url, JSON.stringify(spec?.params), spec?.enum?.join()]) // eslint-disable-line react-hooks/exhaustive-deps
  return opciones
}

export function limpiarCacheOpciones() {
  cacheOpciones.clear()
}

/** Un campo de formulario según su tipo. */
export function Campo({ def, valor, onCambio, error, deshabilitado }) {
  const opciones = useOpciones(def.options)
  const id = `campo-${def.name}`
  const comun = {
    id,
    disabled: deshabilitado || def.disabled,
    required: false,
  }
  const set = (v) => onCambio(def.name, v)

  if (def.type === 'checkbox') {
    return (
      <div className={`campo campo-check`}>
        <input type="checkbox" id={id} checked={!!valor} onChange={(e) => set(e.target.checked)} disabled={comun.disabled} />
        <label htmlFor={id}>{def.label}</label>
      </div>
    )
  }

  let control
  switch (def.type) {
    case 'textarea':
      control = <textarea {...comun} value={valor ?? ''} onChange={(e) => set(e.target.value)} />
      break
    case 'select':
      control = (
        <select {...comun} value={valor ?? ''} onChange={(e) => set(e.target.value === '' ? '' : e.target.value)}>
          <option value="">{def.placeholder || 'Seleccione…'}</option>
          {opciones.map((o) => (
            <option key={o.valor} value={o.valor}>
              {o.texto}
            </option>
          ))}
        </select>
      )
      break
    case 'multi': {
      const actuales = new Set((valor || []).map(String))
      control = (
        <div className="chips" id={id}>
          {opciones.map((o) => {
            const activo = actuales.has(String(o.valor))
            return (
              <button
                type="button"
                key={o.valor}
                className={`chip ${activo ? 'activo' : ''}`}
                disabled={comun.disabled}
                onClick={() => {
                  const s = new Set(actuales)
                  if (activo) s.delete(String(o.valor))
                  else s.add(String(o.valor))
                  set([...s])
                }}
              >
                {o.texto}
              </button>
            )
          })}
        </div>
      )
      break
    }
    default:
      control = (
        <input
          {...comun}
          type={def.type || 'text'}
          value={valor ?? ''}
          step={def.step}
          min={def.min}
          max={def.max}
          maxLength={def.maxLength}
          inputMode={def.inputMode}
          placeholder={def.placeholder}
          autoComplete={def.autoComplete || 'off'}
          onChange={(e) => set(e.target.value)}
        />
      )
  }

  return (
    <div className={`campo ${error ? 'con-error' : ''}`}>
      <label htmlFor={id}>
        {def.label}
        {def.required && <span className="req"> *</span>}
      </label>
      {control}
      {def.help && !error && <span className="ayuda">{def.help}</span>}
      {error && <span className="error">{error}</span>}
    </div>
  )
}

/** Validación del lado del cliente (el backend vuelve a validar todo). */
export function validar(campos, valores) {
  const errores = {}
  campos.forEach((c) => {
    if (c.showIf && !c.showIf(valores)) return
    const v = leer(valores, c.name)
    const vacio = v === undefined || v === null || v === '' || (Array.isArray(v) && v.length === 0)
    if (c.required && !c.disabled && (c.type === 'checkbox' ? false : vacio)) {
      errores[c.name] = 'Este campo es obligatorio'
      return
    }
    if (vacio) return
    if (c.pattern && !new RegExp(c.pattern).test(String(v))) errores[c.name] = c.patternMsg || 'Formato no válido'
    if (c.minLength && String(v).length < c.minLength) errores[c.name] = `Mínimo ${c.minLength} caracteres`
    if (c.type === 'number' && c.min !== undefined && Number(v) < Number(c.min)) errores[c.name] = `Debe ser al menos ${c.min}`
    if (c.type === 'number' && c.exclusivoMin !== undefined && Number(v) <= Number(c.exclusivoMin)) errores[c.name] = `Debe ser mayor que ${c.exclusivoMin}`
  })
  return errores
}

/** Une los errores que devuelve el backend ({campo: mensaje}) con los de los campos del formulario. */
export function erroresDeApi(e) {
  return e?.errores || {}
}
