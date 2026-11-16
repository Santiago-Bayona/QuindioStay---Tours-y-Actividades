// Cliente HTTP del frontend. Todo pasa por aquí para que haya un solo lugar donde:
//  - se manda el header X-Usuario-Id (la "sesión" que espera el backend),
//  - se convierten los errores del backend ({mensaje, errores}) en un ApiError.

const CLAVE = 'quindiostay.sesion'

export const sesionStore = {
  leer() {
    try {
      return JSON.parse(localStorage.getItem(CLAVE))
    } catch {
      return null
    }
  },
  guardar(sesion) {
    try {
      localStorage.setItem(CLAVE, JSON.stringify(sesion))
    } catch {
      /* sin almacenamiento: la sesión vive solo en memoria */
    }
  },
  borrar() {
    try {
      localStorage.removeItem(CLAVE)
    } catch {
      /* nada */
    }
  },
}

export class ApiError extends Error {
  constructor(estado, mensaje, errores) {
    super(mensaje)
    this.estado = estado
    this.errores = errores || null
  }
}

function armarUrl(ruta, params) {
  if (!params) return ruta
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.append(k, v)
  })
  const s = q.toString()
  return s ? `${ruta}?${s}` : ruta
}

async function pedir(metodo, ruta, { cuerpo, params } = {}) {
  const sesion = sesionStore.leer()
  const headers = {}
  if (cuerpo !== undefined) headers['Content-Type'] = 'application/json'
  // Solo los usuarios internos mandan el header: el id de un cliente podría
  // coincidir con el de un usuario y confundir al backend.
  if (sesion?.tipo === 'USUARIO') headers['X-Usuario-Id'] = String(sesion.id)

  let respuesta
  try {
    respuesta = await fetch(armarUrl(ruta, params), {
      method: metodo,
      headers,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    })
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. ¿Está encendido el backend (puerto 8081)?')
  }

  const texto = await respuesta.text()
  let datos = null
  if (texto) {
    try {
      datos = JSON.parse(texto)
    } catch {
      datos = null
    }
  }
  if (!respuesta.ok) {
    throw new ApiError(respuesta.status, datos?.mensaje || `Error ${respuesta.status}`, datos?.errores)
  }
  return datos
}

export const api = {
  get: (ruta, params) => pedir('GET', ruta, { params }),
  post: (ruta, cuerpo, params) => pedir('POST', ruta, { cuerpo: cuerpo ?? {}, params }),
  put: (ruta, cuerpo) => pedir('PUT', ruta, { cuerpo }),
  patch: (ruta, params, cuerpo) => pedir('PATCH', ruta, { params, cuerpo }),
  del: (ruta) => pedir('DELETE', ruta),
}

/** Abre un PDF del backend en otra pestaña. El navegador no manda headers, por eso va ?idUsuario. */
export function abrirPdf(ruta, params = {}, descargar = false) {
  const sesion = sesionStore.leer()
  const p = { ...params }
  if (sesion?.tipo === 'USUARIO' && ruta.startsWith('/api/reportes')) p.idUsuario = sesion.id
  if (descargar) p.descargar = true
  window.open(armarUrl(ruta, p), '_blank', 'noopener')
}
