# Etapa 1: compila React con Vite usando el package-lock.json versionado
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . ./
# Mismo origen: Nginx publica /api como proxy inverso hacia el backend
ENV VITE_API_URL=/api
RUN npm run build

# Etapa 2: Nginx sirve los archivos estáticos y reenvía /api
FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
