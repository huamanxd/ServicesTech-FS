import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'

// En desarrollo, /api se reenvia a Spring Boot.
// React siempre usa su propio origen: funciona tambien si Vite elige el puerto 5174.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        // El proxy local comparte origen con el navegador. Evita que una cabecera
        // Origin de Vite (p. ej. 5175) active CORS innecesariamente en Spring.
        configure(proxy) {
          proxy.on('proxyReq', (proxyReq) => proxyReq.removeHeader('origin'))
        },
      },
    },
  },
})
