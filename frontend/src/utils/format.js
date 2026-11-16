// Formato de dinero, fechas y textos de los enums del backend.

const cop = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })
export const dinero = (v) => (v === null || v === undefined || v === '' ? '—' : cop.format(Number(v)))

const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

/** '2026-11-20' -> '20 nov 2026' (sin pasar por Date para no tener problemas de zona horaria) */
export function fecha(v) {
  if (!v) return '—'
  const m = String(v).match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (!m) return String(v)
  return `${Number(m[3])} ${MESES[Number(m[2]) - 1]} ${m[1]}`
}

export function fechaHora(v) {
  if (!v) return '—'
  const m = String(v).match(/T(\d{2}):(\d{2})/)
  return m ? `${fecha(v)}, ${m[1]}:${m[2]}` : fecha(v)
}

export const hora = (v) => (v ? String(v).slice(0, 5) : '—')

const ETIQUETAS = {
  CEDULA_CIUDADANIA: 'Cédula de ciudadanía',
  CEDULA_EXTRANJERIA: 'Cédula de extranjería',
  PASAPORTE: 'Pasaporte',
  TARJETA_IDENTIDAD: 'Tarjeta de identidad',
  ESPANOL: 'Español',
  INGLES: 'Inglés',
  APP_MOVIL: 'App móvil',
  TELEFONO: 'Teléfono',
  AGENCIA_INTERMEDIARIA: 'Agencia intermediaria',
  TARJETA_CREDITO: 'Tarjeta de crédito',
  TARJETA_DEBITO: 'Tarjeta débito',
  PSE: 'PSE',
  SMS: 'SMS',
  WHATSAPP: 'WhatsApp',
  TODO_INCLUIDO: 'Todo incluido',
  BASICO: 'Básico',
  ESTANDAR: 'Estándar',
  RECORDATORIO_RESERVA: 'Recordatorio de reserva',
  CONFIRMACION_RESERVA: 'Confirmación de reserva',
  CONFIRMACION_PAGO: 'Confirmación de pago',
  CAMBIO_ESTADO: 'Cambio de estado',
  PROMOCION: 'Promoción',
}

/** 'EN_CURSO' -> 'En curso', con excepciones con tilde en ETIQUETAS. */
export function etiqueta(v) {
  if (v === null || v === undefined || v === '') return '—'
  if (typeof v === 'boolean') return v ? 'Sí' : 'No'
  if (ETIQUETAS[v]) return ETIQUETAS[v]
  const s = String(v)
  if (!/^[A-Z_]+$/.test(s)) return s
  const t = s.toLowerCase().replace(/_/g, ' ')
  return t.charAt(0).toUpperCase() + t.slice(1)
}

export function edadEn(nacimiento, referencia) {
  if (!nacimiento) return null
  const n = new Date(nacimiento + 'T00:00:00')
  const r = referencia ? new Date(referencia + 'T00:00:00') : new Date()
  let e = r.getFullYear() - n.getFullYear()
  const m = r.getMonth() - n.getMonth()
  if (m < 0 || (m === 0 && r.getDate() < n.getDate())) e--
  return e
}

export const hoyISO = () => new Date().toISOString().slice(0, 10)
