import { createContext, useCallback, useContext, useState } from 'react'
import Icon from './Icon.jsx'

const ToastContext = createContext(() => {})

export function ToastProvider({ children }) {
  const [avisos, setAvisos] = useState([])

  const avisar = useCallback((mensaje, tipo = 'ok') => {
    const id = Date.now() + Math.random()
    setAvisos((a) => [...a, { id, mensaje, tipo }])
    setTimeout(() => setAvisos((a) => a.filter((x) => x.id !== id)), 4200)
  }, [])

  return (
    <ToastContext.Provider value={avisar}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {avisos.map((a) => (
          <div key={a.id} className={`toast toast-${a.tipo}`}>
            <Icon name={a.tipo === 'error' ? 'alert' : 'check'} size={16} />
            <span>{a.mensaje}</span>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

 
export function useToast() {
  return useContext(ToastContext)
}
