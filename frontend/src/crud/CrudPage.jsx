import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, abrirPdf } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from '../components/Icon.jsx'
import { Campo, erroresDeApi, escribir, leer, limpiarCacheOpciones, validar } from '../components/Formulario.jsx'
import { Aviso, Cargando, Confirmar, EncabezadoPagina, Migas, Modal, Vacio, textoError, useCarga } from '../components/ui.jsx'
import { useToast } from '../components/Toast.jsx'
import { etiqueta } from '../utils/format.js'

const POR_PAGINA = 10

function textoCelda(col, fila) {
  const v = leer(fila, col.key)
  return col.texto ? col.texto(v, fila) : v
}

/**
 * Pantalla de gestión genérica (la del prototipo de "Gestión de municipios"): búsqueda, orden,
 * paginación, Ver / Editar / Eliminar y formulario en ventana. Cada entidad se describe en entities.js.
 */
export default function CrudPage({ config }) {
  const { rol, sesion } = useAuth()
  const avisar = useToast()
  const c = config

  const listar = useMemo(() => c.listar || (() => api.get(c.endpoint, c.paramsLista)), [c])
  const { datos, cargando, error, recargar } = useCarga(listar, [c.endpoint])

  const [busqueda, setBusqueda] = useState('')
  const [orden, setOrden] = useState({ key: c.ordenInicial || c.columns[0].key, asc: true })
  const [pagina, setPagina] = useState(1)
  const [ver, setVer] = useState(null)
  const [edicion, setEdicion] = useState(null) // { fila|null }
  const [borrar, setBorrar] = useState(null)
  const [accion, setAccion] = useState(null)
  const [ocupado, setOcupado] = useState(false)

  const puede = (permiso, fila) => {
    const regla = c.permisos?.[permiso]
    if (regla === undefined) return true
    if (typeof regla === 'function') return regla({ rol, sesion, fila })
    return regla.includes(rol)
  }

  const filas = useMemo(() => {
    let lista = datos || []
    if (c.filtroFila) lista = lista.filter((f) => c.filtroFila(f, { rol, sesion }))
    const q = busqueda.trim().toLowerCase()
    if (q) {
      lista = lista.filter((f) =>
        (c.busqueda || c.columns.map((x) => x.key)).some((k) => String(leer(f, k) ?? '').toLowerCase().includes(q) || String(etiqueta(leer(f, k))).toLowerCase().includes(q)),
      )
    }
    const col = c.columns.find((x) => x.key === orden.key)
    const val = (f) => {
      const v = leer(f, orden.key)
      return col?.orden ? col.orden(v, f) : v
    }
    return [...lista].sort((a, b) => {
      const x = val(a)
      const y = val(b)
      const r = typeof x === 'number' && typeof y === 'number' ? x - y : String(x ?? '').localeCompare(String(y ?? ''), 'es', { numeric: true })
      return orden.asc ? r : -r
    })
  }, [datos, busqueda, orden, c, rol, sesion])

  const totalPaginas = Math.max(1, Math.ceil(filas.length / POR_PAGINA))
  const actual = Math.min(pagina, totalPaginas)
  const visibles = filas.slice((actual - 1) * POR_PAGINA, actual * POR_PAGINA)

  function ordenar(key) {
    setOrden((o) => ({ key, asc: o.key === key ? !o.asc : true }))
  }

  async function eliminar() {
    setOcupado(true)
    try {
      await api.del(`${c.endpoint}/${borrar[c.id]}`)
      avisar(`${c.singular[0].toUpperCase() + c.singular.slice(1)} eliminado.`)
      limpiarCacheOpciones()
      setBorrar(null)
      recargar()
    } catch (e) {
      avisar(textoError(e), 'error')
      setBorrar(null)
    } finally {
      setOcupado(false)
    }
  }

  async function ejecutarAccion(valor) {
    const a = accion.def
    setOcupado(true)
    try {
      await a.ejecutar(accion.fila, valor)
      avisar(a.exito || 'Listo.')
      limpiarCacheOpciones()
      setAccion(null)
      recargar()
    } catch (e) {
      avisar(textoError(e), 'error')
      setAccion(null)
    } finally {
      setOcupado(false)
    }
  }

  const accionesDeFila = (fila) => (c.acciones || []).filter((a) => (!a.mostrar || a.mostrar(fila, { rol, sesion })) && (!a.permiso || puede(a.permiso, fila)))

  return (
    <>
      <Migas items={[{ texto: 'Administración', to: '/admin' }, { texto: c.plural[0].toUpperCase() + c.plural.slice(1) }]} />
      <EncabezadoPagina icono={c.icono} titulo={c.titulo} subtitulo={c.subtitulo}>
        {(c.botonesEncabezado || []).map((b) =>
          puede(b.permiso || 'crear') ? (
            <button
              key={b.texto}
              className="btn"
              onClick={async () => {
                try {
                  const r = await b.ejecutar()
                  avisar(b.exito ? b.exito(r) : 'Listo.')
                  recargar()
                } catch (e) {
                  avisar(textoError(e), 'error')
                }
              }}
            >
              <Icon name={b.icono || 'send'} size={15} /> {b.texto}
            </button>
          ) : null,
        )}
        {c.crear !== false && puede('crear') && (
          <button className="btn btn-primario" onClick={() => setEdicion({ fila: null })}>
            <Icon name="plus" size={16} /> Crear {c.singular}
          </button>
        )}
      </EncabezadoPagina>

      <div className="tarjeta">
        <div className="barra-tabla">
          <div>
            <b style={{ fontSize: 13 }}>{c.singular.toUpperCase()}</b>
            <span className="contador">{filas.length} registros</span>
          </div>
          <div className="input-icono buscar">
            <Icon name="search" size={16} />
            <input
              className="input"
              style={{ width: '100%', padding: '9px 12px 9px 38px', border: '1px solid var(--borde-fuerte)', borderRadius: 9, background: 'var(--superficie)' }}
              placeholder={c.placeholderBusqueda || 'Buscar…'}
              value={busqueda}
              onChange={(e) => {
                setBusqueda(e.target.value)
                setPagina(1)
              }}
            />
          </div>
          {c.filtros && c.filtros({ datos, filtro: c.estadoFiltro })}
        </div>

        {error && <div style={{ padding: '0 16px' }}><Aviso>{textoError(error)}</Aviso></div>}
        {cargando ? (
          <Cargando />
        ) : filas.length === 0 ? (
          <Vacio />
        ) : (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead>
                <tr>
                  {c.columns.map((col) => (
                    <th key={col.key} className={`orden ${col.num ? 'num' : ''}`} onClick={() => ordenar(col.key)}>
                      {col.label} {orden.key === col.key ? (orden.asc ? '↑' : '↓') : ''}
                    </th>
                  ))}
                  <th className="acciones">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {visibles.map((fila) => (
                  <tr key={fila[c.id]}>
                    {c.columns.map((col) => (
                      <td key={col.key} className={`${col.num ? 'num' : ''} ${col.fuerte ? 'fuerte' : ''}`}>
                        {col.render ? col.render(leer(fila, col.key), fila) : textoCelda(col, fila) ?? '—'}
                      </td>
                    ))}
                    <td className="acciones">
                      <button className="btn btn-enlace btn-chico" onClick={() => setVer(fila)}>
                        <Icon name="eye" size={14} /> Ver
                      </button>
                      {c.editar !== false && puede('editar', fila) && (
                        <button className="btn btn-enlace btn-chico" onClick={() => setEdicion({ fila })}>
                          <Icon name="pencil" size={14} /> Editar
                        </button>
                      )}
                      {accionesDeFila(fila).map((a) =>
                        a.enlace ? (
                          <Link key={a.texto} className="btn btn-enlace btn-chico" to={a.enlace(fila)}>
                            <Icon name={a.icono || 'arrow'} size={14} /> {a.texto}
                          </Link>
                        ) : (
                          <button
                            key={a.texto}
                            className={`btn btn-enlace btn-chico ${a.peligro ? 'peligro' : ''}`}
                            onClick={() => (a.pdf ? abrirPdf(a.pdf(fila)) : setAccion({ def: a, fila }))}
                          >
                            <Icon name={a.icono || 'arrow'} size={14} /> {a.texto}
                          </button>
                        ),
                      )}
                      {c.eliminar !== false && puede('eliminar', fila) && (
                        <button className="btn btn-enlace btn-chico peligro" onClick={() => setBorrar(fila)}>
                          <Icon name="trash" size={14} /> Eliminar
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="pie-tabla">
          <span>
            Mostrando {filas.length === 0 ? 0 : (actual - 1) * POR_PAGINA + 1}–{Math.min(actual * POR_PAGINA, filas.length)} de {filas.length} {c.plural}
          </span>
          <div className="paginas">
            <button className="pagina-btn" disabled={actual <= 1} onClick={() => setPagina(actual - 1)}>‹</button>
            <button className="pagina-btn activo">{actual}</button>
            <span>de {totalPaginas}</span>
            <button className="pagina-btn" disabled={actual >= totalPaginas} onClick={() => setPagina(actual + 1)}>›</button>
          </div>
        </div>
      </div>

      {ver && (
        <Modal titulo={`${c.singular[0].toUpperCase() + c.singular.slice(1)} #${ver[c.id]}`} onCerrar={() => setVer(null)} pie={<button className="btn" onClick={() => setVer(null)}>Cerrar</button>}>
          <dl className="datos">
            {(c.detalle || c.columns).map((d) => {
              const clave = d.key
              const valor = leer(ver, clave)
              return (
                <div className="dato" key={clave}>
                  <dt>{d.label}</dt>
                  <dd>{d.render ? d.render(valor, ver) : (d.texto ? d.texto(valor, ver) : typeof valor === 'object' && valor !== null ? JSON.stringify(valor) : etiqueta(valor))}</dd>
                </div>
              )
            })}
          </dl>
          {c.detalleExtra && c.detalleExtra(ver)}
        </Modal>
      )}

      {edicion && (
        <FormularioEntidad
          config={c}
          fila={edicion.fila}
          onCerrar={() => setEdicion(null)}
          onGuardado={() => {
            setEdicion(null)
            limpiarCacheOpciones()
            recargar()
            avisar(edicion.fila ? 'Cambios guardados.' : `${c.singular[0].toUpperCase() + c.singular.slice(1)} creado.`)
          }}
        />
      )}

      {borrar && (
        <Confirmar
          titulo={`Eliminar ${c.singular}`}
          mensaje={`¿Eliminar ${c.singular} «${c.nombreFila ? c.nombreFila(borrar) : `#${borrar[c.id]}`}»? Esta acción no se puede deshacer.${c.avisoEliminar ? ' ' + c.avisoEliminar : ''}`}
          textoOk="Eliminar"
          peligro
          ocupado={ocupado}
          onOk={eliminar}
          onCancelar={() => setBorrar(null)}
        />
      )}

      {accion && <ModalAccion accion={accion} ocupado={ocupado} onOk={ejecutarAccion} onCancelar={() => setAccion(null)} />}
    </>
  )
}

/** Ventana de una acción de fila: confirmar, elegir un estado o escribir una fecha. */
function ModalAccion({ accion, ocupado, onOk, onCancelar }) {
  const a = accion.def
  const [valor, setValor] = useState(a.valorInicial ? a.valorInicial(accion.fila) : '')
  if (!a.tipo || a.tipo === 'confirmar') {
    return <Confirmar titulo={a.texto} mensaje={a.mensaje ? a.mensaje(accion.fila) : '¿Confirmas esta acción?'} textoOk={a.textoOk || 'Confirmar'} peligro={a.peligro} ocupado={ocupado} onOk={() => onOk()} onCancelar={onCancelar} />
  }
  return (
    <Modal
      titulo={a.texto}
      onCerrar={onCancelar}
      pie={
        <>
          <button className="btn" onClick={onCancelar}>Cancelar</button>
          <button className="btn btn-primario" disabled={!valor || ocupado} onClick={() => onOk(valor)}>
            {ocupado ? 'Guardando…' : 'Aplicar'}
          </button>
        </>
      }
    >
      <Campo def={{ name: 'v', label: a.etiqueta || 'Valor', type: a.tipo === 'fecha' ? 'datetime-local' : 'select', options: a.opciones ? { enum: a.opciones } : undefined }} valor={valor} onCambio={(_, v) => setValor(v)} />
    </Modal>
  )
}

function valoresIniciales(c, fila) {
  let v = {}
  c.fields.forEach((f) => {
    if (f.default !== undefined) v = escribir(v, f.name, f.default)
  })
  if (fila) v = c.desdeFila ? { ...v, ...c.desdeFila(fila) } : { ...v, ...fila }
  return v
}

function FormularioEntidad({ config: c, fila, onCerrar, onGuardado }) {
  const [valores, setValores] = useState(() => valoresIniciales(c, fila))
  const [errores, setErrores] = useState({})
  const [mensaje, setMensaje] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const editando = !!fila

  const campos = c.fields.filter((f) => !f.showIf || f.showIf(valores))

  function cambiar(nombre, valor) {
    setValores((v) => {
      let nuevo = escribir(v, nombre, valor)
      const def = c.fields.find((f) => f.name === nombre)
      if (def?.alCambiar) nuevo = def.alCambiar(nuevo, valor)
      return nuevo
    })
    setErrores((e) => ({ ...e, [nombre]: undefined }))
  }

  async function guardar(e) {
    e.preventDefault()
    setMensaje(null)
    const errs = validar(
      c.fields.map((f) => (editando && f.requeridoSoloAlCrear ? { ...f, required: false } : f)),
      valores,
    )
    if (Object.keys(errs).length) {
      setErrores(errs)
      return
    }
    setEnviando(true)
    try {
      const cuerpo = c.aBody(valores, { editando })
      if (editando) await api.put(`${c.endpoint}/${fila[c.id]}`, cuerpo)
      else await api.post(c.endpoint, cuerpo)
      onGuardado()
    } catch (err) {
      setErrores(erroresDeApi(err))
      setMensaje(textoError(err))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal
      titulo={`${editando ? 'Editar' : 'Crear'} ${c.singular}`}
      onCerrar={onCerrar}
      ancho={c.fields.length > 8}
      pie={
        <>
          <button className="btn" onClick={onCerrar} type="button">Cancelar</button>
          <button className="btn btn-primario" form="form-entidad" disabled={enviando}>
            {enviando ? 'Guardando…' : 'Guardar'}
          </button>
        </>
      }
    >
      <form id="form-entidad" onSubmit={guardar} noValidate>
        <Aviso>{mensaje}</Aviso>
        <div className="fila-campos">
          {campos.map((f) => (
            <div key={f.name} style={f.ancho ? { gridColumn: '1 / -1' } : undefined}>
              <Campo
                def={editando && f.requeridoSoloAlCrear ? { ...f, required: false } : f}
                valor={leer(valores, f.name)}
                onCambio={cambiar}
                error={errores[f.name]}
                deshabilitado={editando && f.soloAlCrear}
              />
            </div>
          ))}
        </div>
      </form>
    </Modal>
  )
}
