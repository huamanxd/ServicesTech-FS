# ServicesFrontend

React 19 + Vite + Tailwind CSS 4. La documentación completa (arquitectura, API, despliegue) está en el [README principal](../README.md).

```bash
npm ci
npm run dev       # http://localhost:5173 — /api se reenvía a http://127.0.0.1:8080
npm run lint
npm test          # Vitest + Testing Library
npm run build     # genera dist/
```

| Carpeta | Contenido |
|---|---|
| `src/api/` | `http.js` (Axios: token, sesión vencida, mensajes de error) y `portal.js` (un método por endpoint) |
| `src/contexto/` | Sesión del usuario (`ProveedorAutenticacion`) |
| `src/componentes/` | Cabecera, pie, ruta protegida por rol, piezas de UI, detalle e historial de órdenes |
| `src/pantallas/` | Páginas públicas y los paneles `cliente/`, `tecnico/` y `admin/` |
| `src/util/` | Validaciones (las mismas reglas que el backend), formato y roles |
| `tests/` | Pruebas |

La API siempre se llama por el mismo origen (`/api`): Vite en desarrollo, Nginx en Docker (`nginx.conf`) y un *rewrite* en Vercel (`vercel.json`).
