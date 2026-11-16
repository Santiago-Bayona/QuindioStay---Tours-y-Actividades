import { useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, abrirPdf } from '../api/client.js'
import Icon from '../components/Icon.jsx'
import { Aviso, Cargando, EncabezadoPagina, Migas, Vacio, textoError, useCarga } from '../components/ui.jsx'
import { dinero, etiqueta, fecha, fechaHora, hora } from '../utils/format.js'

const MESES = ['', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']
const CON_PERIODO = [5, 10]
const COLORES = ['#2f5a3f', '#b08968', '#3d5f7a', '#a9a46a', '#8a6a1f', '#7a5a6e']

const nombreColumna = (c) => {
  const t = c.replace(/_/g, ' ')
  return t.charAt(0).toUpperCase() + t.slice(1)
}
const esDinero = (c) => /total_recibido|precio|ingreso|valor|monto/.test(c)

function celda(col, valor) {
  if (valor === null || valor === undefined) return '—'
  if (esDinero(col)) return dinero(valor)
  if (/^fecha/.test(col)) return fecha(valor)
  if (/^hora/.test(col)) return hora(valor)
  if (col === 'ocupacion_promedio' || col === 'promedio_general') return Number(valor).toLocaleString('es-CO', { maximumFractionDigits: 2 })
  if (/^(estado|metodo_pago)$/.test(col)) return etiqueta(valor)
  return String(valor)
}

/** Menú con los 10 reportes de la entrega. */
export function ReportesMenu() {
  const { datos, cargando, error } = useCarga(() => api.get('/api/reportes'), [])
  return (
    <>
      <Migas items={[{ texto: 'Administración', to: '/admin' }, { texto: 'Reportes' }]} />
      <EncabezadoPagina icono="chart" titulo="Reportes" subtitulo="Los 10 reportes de la entrega. Todos se pueden exportar a PDF." />
      {error && <Aviso>{textoError(error)}</Aviso>}
      {cargando ? <Cargando /> : (
        <div className="rep-grid">
          {(datos || []).map((r) => (
            <Link key={r.numero} className="tarjeta rep-card" to={`/admin/reportes/${r.numero}`}>
              <span className="num">Reporte {r.numero}</span>
              <h3>{r.titulo}</h3>
              <p>{r.descripcion}</p>
            </Link>
          ))}
        </div>
      )}
    </>
  )
}

/** Un reporte: filtros de período (5 y 10), tabla y exportación a PDF. El 10 tiene la vista del prototipo. */
export function ReporteDetalle() {
  const { numero } = useParams()
  const n = Number(numero)
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const [aplicado, setAplicado] = useState({ desde: '', hasta: '' })
  const { datos, cargando, error } = useCarga(() => api.get(`/api/reportes/${n}`, aplicado), [n, aplicado.desde, aplicado.hasta])
  const periodo = CON_PERIODO.includes(n)
  const invalido = desde && hasta && desde > hasta

  return (
    <>
      <Migas items={[{ texto: 'Administración', to: '/admin' }, { texto: 'Reportes', to: '/admin/reportes' }, { texto: `Reporte ${n}` }]} />
      <EncabezadoPagina icono="chart" titulo={datos?.titulo || `Reporte ${n}`} subtitulo={datos?.descripcion}>
        <button className="btn btn-primario" onClick={() => abrirPdf(`/api/reportes/${n}/pdf`, periodo ? aplicado : {}, true)}>
          <Icon name="download" size={16} /> Exportar reporte
        </button>
        <button className="btn" onClick={() => abrirPdf(`/api/reportes/${n}/pdf`, periodo ? aplicado : {})}>
          <Icon name="eye" size={16} /> Ver PDF
        </button>
      </EncabezadoPagina>

      {periodo && (
        <div className="tarjeta filtros">
          <div className="campo"><label>Desde</label><input type="date" className="input" value={desde} onChange={(e) => setDesde(e.target.value)} /></div>
          <div className="campo"><label>Hasta</label><input type="date" className="input" value={hasta} onChange={(e) => setHasta(e.target.value)} /></div>
          <button className="btn btn-primario" disabled={!!invalido} onClick={() => setAplicado({ desde, hasta })}>Aplicar filtros</button>
          <button className="btn" onClick={() => { setDesde(''); setHasta(''); setAplicado({ desde: '', hasta: '' }) }}>Limpiar</button>
          {invalido && <span style={{ color: 'var(--peligro)', fontSize: 12.5 }}>La fecha inicial no puede ser posterior a la final.</span>}
        </div>
      )}

      {error && <Aviso>{textoError(error)}</Aviso>}
      {cargando ? <Cargando /> : datos && (n === 10 ? <Reporte10 datos={datos} /> : <TablaReporte datos={datos} />)}
      {datos && <p className="texto-suave" style={{ fontSize: 12 }}>Generado el {fechaHora(datos.generadoEn)}</p>}
    </>
  )
}

function TablaReporte({ datos }) {
  if (!datos.filas.length) return <div className="tarjeta"><Vacio texto="El reporte no arrojó resultados para este período." /></div>
  return (
    <div className="tarjeta">
      <div className="barra-tabla"><b style={{ fontSize: 13 }}>RESULTADOS</b><span className="contador">{datos.filas.length} filas</span></div>
      <div className="tabla-contenedor">
        <table className="tabla">
          <thead><tr>{datos.columnas.map((c) => <th key={c} className={esDinero(c) || /^(cupo|salidas|participantes|reservas|ocupacion)/.test(c) ? 'num' : ''}>{nombreColumna(c)}</th>)}</tr></thead>
          <tbody>
            {datos.filas.map((f, i) => (
              <tr key={i}>
                {datos.columnas.map((c) => <td key={c} className={esDinero(c) || /^(cupo|salidas|participantes|reservas|ocupacion)/.test(c) ? 'num' : ''}>{celda(c, f[c])}</td>)}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

/** Reporte 10: ingresos por categoría y método de pago (tarjetas, barras mensuales y desglose). */
function Reporte10({ datos }) {
  const [categoria, setCategoria] = useState('')
  const [metodo, setMetodo] = useState('')
  const categorias = useMemo(() => [...new Set(datos.filas.map((f) => f.categoria))].sort(), [datos])
  const metodos = useMemo(() => [...new Set(datos.filas.map((f) => f.metodo_pago))].sort(), [datos])

  const filas = useMemo(
    () => datos.filas.filter((f) => (!categoria || f.categoria === categoria) && (!metodo || f.metodo_pago === metodo)),
    [datos, categoria, metodo],
  )
  const total = filas.reduce((s, f) => s + Number(f.total_recibido || 0), 0)

  const sumar = (clave) => {
    const m = new Map()
    filas.forEach((f) => m.set(f[clave], (m.get(f[clave]) || 0) + Number(f.total_recibido || 0)))
    return [...m.entries()]
  }
  const porMetodo = sumar('metodo_pago').sort((a, b) => b[1] - a[1])
  const porCategoria = sumar('categoria').sort((a, b) => b[1] - a[1])
  const porMes = useMemo(() => {
    const m = new Map()
    filas.forEach((f) => {
      const cat = f.categoria
      const mes = String(f.mes)
      if (!m.has(mes)) m.set(mes, new Map())
      m.get(mes).set(cat, (m.get(mes).get(cat) || 0) + Number(f.total_recibido || 0))
    })
    return [...m.entries()].sort((a, b) => a[0].localeCompare(b[0]))
  }, [filas])
  const catsOrden = porCategoria.map(([c]) => c)
  const colorDe = (c) => COLORES[Math.max(0, categorias.indexOf(c)) % COLORES.length]
  const maxMes = Math.max(1, ...porMes.map(([, mm]) => [...mm.values()].reduce((a, b) => a + b, 0)))
  const maxMetodo = Math.max(1, ...porMetodo.map(([, v]) => v))
  const etiquetaMes = (m) => {
    const p = m.match(/^(\d{4})-(\d{2})/)
    return p ? `${MESES[Number(p[2])].slice(0, 3)} ${p[1]}` : m
  }

  return (
    <>
      <div className="tarjeta filtros">
        <div className="campo">
          <label>Categoría</label>
          <select className="input" value={categoria} onChange={(e) => setCategoria(e.target.value)}>
            <option value="">Todas las categorías</option>
            {categorias.map((c) => <option key={c}>{c}</option>)}
          </select>
        </div>
        <div className="campo">
          <label>Método de pago</label>
          <select className="input" value={metodo} onChange={(e) => setMetodo(e.target.value)}>
            <option value="">Todos los métodos</option>
            {metodos.map((m) => <option key={m} value={m}>{etiqueta(m)}</option>)}
          </select>
        </div>
        {(categoria || metodo) && <button className="btn" onClick={() => { setCategoria(''); setMetodo('') }}>Limpiar filtros</button>}
      </div>

      <div className="metricas">
        <div className="tarjeta metrica oscura"><div className="etq">Total recibido</div><div className="val">{dinero(total)}</div><div className="nota">{filas.length} combinaciones mes · categoría · método</div></div>
        <div className="tarjeta metrica"><div className="etq">Categoría líder</div><div className="val" style={{ fontSize: 24 }}>{porCategoria[0]?.[0] ?? '—'}</div><div className="nota">{porCategoria[0] ? dinero(porCategoria[0][1]) : ''}</div></div>
        <div className="tarjeta metrica"><div className="etq">Método más usado</div><div className="val" style={{ fontSize: 24 }}>{porMetodo[0] ? etiqueta(porMetodo[0][0]) : '—'}</div><div className="nota">{porMetodo[0] ? dinero(porMetodo[0][1]) : ''}</div></div>
      </div>

      {filas.length === 0 ? (
        <div className="tarjeta"><Vacio texto="No hay ingresos para esos filtros." /></div>
      ) : (
        <>
          <div className="rejilla rejilla-2">
            <div className="tarjeta tarjeta-pad">
              <div className="seccion-titulo"><h3>Ingresos mensuales por categoría</h3></div>
              <div className="leyenda">{catsOrden.map((c) => <span key={c}><i style={{ background: colorDe(c) }} />{c}</span>)}</div>
              <div className="barras">
                {porMes.map(([mes, cats]) => {
                  const totalMes = [...cats.values()].reduce((a, b) => a + b, 0)
                  return (
                    <div className="barra-fila" key={mes}>
                      <span>{etiquetaMes(mes)}</span>
                      <div className="barra-pista">
                        {catsOrden.filter((c) => cats.has(c)).map((c) => (
                          <div key={c} className="barra-seg" title={`${c}: ${dinero(cats.get(c))}`} style={{ width: `${(cats.get(c) / maxMes) * 100}%`, background: colorDe(c) }} />
                        ))}
                      </div>
                      <span className="val">{dinero(totalMes)}</span>
                    </div>
                  )
                })}
              </div>
            </div>
            <div className="tarjeta tarjeta-pad">
              <div className="seccion-titulo"><h3>Ingresos por método de pago</h3></div>
              <div className="barras">
                {porMetodo.map(([m, v]) => (
                  <div className="barra-fila" key={m}>
                    <span>{etiqueta(m)}</span>
                    <div className="barra-pista"><div className="barra-seg" style={{ width: `${(v / maxMetodo) * 100}%`, background: 'var(--verde)' }} /></div>
                    <span className="val">{dinero(v)}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="espacio-v" />
          <div className="tarjeta">
            <div className="barra-tabla"><b style={{ fontSize: 13 }}>DESGLOSE</b><span className="contador">{filas.length} filas</span></div>
            <div className="tabla-contenedor">
              <table className="tabla">
                <thead><tr><th>Mes</th><th>Categoría</th><th>Método de pago</th><th className="num">Total recibido</th></tr></thead>
                <tbody>
                  {filas.map((f, i) => (
                    <tr key={i}><td>{etiquetaMes(String(f.mes))}</td><td>{f.categoria}</td><td>{etiqueta(f.metodo_pago)}</td><td className="num">{dinero(f.total_recibido)}</td></tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </>
  )
}
