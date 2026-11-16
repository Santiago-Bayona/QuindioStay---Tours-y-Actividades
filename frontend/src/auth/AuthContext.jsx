import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { api, ApiError, sesionStore } from '../api/client.js'

const AuthContext = createContext(null)

/** Traduce el nombre del rol que devuelve el backend a una clave corta. */
function claveRol(sesion) {
  if (!sesion) return null
  if (sesion.tipo === 'CLIENTE') return 'cliente'
  const r = (sesion.rol || '').toLowerCase()
  if (r.startsWith('administrador')) return 'admin'
  if (r.startsWith('encargado')) return 'encargado'
  return 'operador'
}

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(() => sesionStore.leer())

  /**
   * Un solo formulario para todos: si lo escrito parece un correo se prueba primero
   * como cliente, si no como usuario interno; si falla se prueba el otro.
   */
  const iniciarSesion = useCallback(async (usuario, contrasena) => {
    const cuerpo = { usuario: usuario.trim(), contrasena }
    const orden = cuerpo.usuario.includes('@') ? ['cliente', 'usuario'] : ['usuario', 'cliente']
    let ultimo = null
    for (const tipo of orden) {
      try {
        const datos = await api.post(`/api/auth/login/${tipo}`, cuerpo)
        sesionStore.guardar(datos)
        setSesion(datos)
        return datos
      } catch (e) {
        if (!(e instanceof ApiError) || (e.estado !== 401 && e.estado !== 400)) throw e
        // El mensaje "bloqueado" es más útil que el genérico: se conserva.
        if (!ultimo || /bloquead/i.test(e.message)) ultimo = e
      }
    }
    throw ultimo
  }, [])

  const cerrarSesion = useCallback(() => {
    sesionStore.borrar()
    setSesion(null)
  }, [])

  const valor = useMemo(() => {
    const rol = claveRol(sesion)
    return {
      sesion,
      rol,
      esStaff: sesion?.tipo === 'USUARIO',
      esCliente: sesion?.tipo === 'CLIENTE',
      iniciarSesion,
      cerrarSesion,
    }
  }, [sesion, iniciarSesion, cerrarSesion])

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

 
export function useAuth() {
  return useContext(AuthContext)
}
