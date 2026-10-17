# ServicesFrontend — ServicesTech original ampliado

El frontend sigue utilizando `src/pantallas/Home.jsx`, `Login.jsx`, `Signup.jsx`, `Catalogo.jsx`, `Servicios.jsx` y `src/componentes/Cabecera.jsx`, `Cuerpo.jsx`, `Piedepagina.jsx`.

Se añadió React Router, Axios y autenticación por rol sin reemplazar esta estructura. Consulta [README de la raíz](../README.md) para PostgreSQL, API, registro, rutas, precios, pruebas e instalación.

```bash
npm install
npm run dev
npm test
```

En desarrollo utiliza `VITE_API_URL=/api` (proxy Vite). Para producción puedes configurarla con una URL externa y autorizar el origen en Spring Security.

## Comunicacion Axios / CORS en desarrollo

`vite.config.js` reenvia las solicitudes a `/api` hacia `http://127.0.0.1:8080`,
mientras `src/api/http.js` utiliza `baseURL: '/api'` por defecto. Esto evita CORS
incluso si Vite se inicia en `localhost:5174` o `127.0.0.1:5173`.

Para probar, inicia Spring Boot en una terminal y Vite en otra:

```bash
# Desde ServicesFrontend:
cp .env.example .env  # si no existia .env
npm run dev
```

Si ya tenias `.env`, asegurate de que incluya `VITE_API_URL=/api` y reinicia
Vite. En el navegador, abre `http://localhost:5173/api/servicios` (o el puerto
que Vite muestre). Debe responder con el mismo JSON que `http://localhost:8080/api/servicios`.
Si obtienes `502` o `ECONNREFUSED`, revisa que Spring Boot siga encendido en `8080`.

Para dominios separados en produccion, configura un proxy `/api` en tu servidor web
o utiliza `VITE_API_URL=https://api.tudominio.com/api`, y autoriza el origen
exacto en Spring Boot con `FRONTEND_ORIGINS=https://tudominio.com` (separado por comas
si hay varios). No uses `*` para el acceso autenticado.
