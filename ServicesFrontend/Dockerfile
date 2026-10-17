FROM node:22-alpine AS build
WORKDIR /app
COPY package.json ./
# El ZIP heredado no contiene package-lock.json; npm install genera el arbol.
# Una vez generado un lockfile verificado, migrar a npm ci y versionarlo.
RUN npm install --no-audit --no-fund
COPY . ./
# Las peticiones son del mismo origen; Nginx publica /api como proxy reverso.
ENV VITE_API_URL=/api
RUN npm run build

FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
