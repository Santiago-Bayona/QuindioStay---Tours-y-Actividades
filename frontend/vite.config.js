/* global process */
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// El proxy manda todo lo que empiece por /api al backend (Spring Boot, puerto 8081).
// Así React llama a "/api/..." sin preocuparse por CORS, y los enlaces a PDF
// (window.open('/api/reportes/1/pdf')) también funcionan.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_URL || 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
})
