FROM node:22-alpine AS base
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./

FROM base AS development
EXPOSE 5173
CMD ["npm", "run", "dev"]

FROM base AS build
RUN npm run build

FROM nginx:stable-alpine AS runtime
COPY infra/nginx/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
