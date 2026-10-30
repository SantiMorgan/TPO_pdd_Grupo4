# ADR-000 — Base ejecutable del Hito 0

Estado: aceptada para el andamiaje.

## Contexto

El repositorio contiene un prototipo Java de consola en `src/`. El README define una aplicación web
con MVC y Facade, Spring Boot, React y PostgreSQL que debe arrancar usando únicamente Docker.

## Decisión

- Incorporar `backend/` y `frontend/` como builds separados, con Compose como punto de arranque.
- Conservar el prototipo de consola como referencia, fuera del build de Spring Boot.
- Preparar los paquetes de la arquitectura sin implementar anticipadamente los casos de uso pendientes.
- Publicar `/api` para identificar la aplicación y `/api/health` para verificar también PostgreSQL.
- Crear el esquema `barstock` con Flyway V1. Incorporar las tablas del dominio en nuevas migraciones.
- Usar Vite como proxy en desarrollo y nginx al compilar el frontend.
- Ofrecer recarga de React y recompilación de Java con DevTools dentro de Docker.
- Mantener las acciones de Make también en un script PowerShell para Windows.

## Consecuencias

Los roles de la pantalla inicial son una selección de interfaz, todavía sin autenticación. Las clases,
tablas, controladores de negocio y reglas del prototipo se integrarán al backend en los siguientes hitos.
Las decisiones abiertas del README sobre descuento de stock y anulación siguen pendientes.

## Referencias de herramientas

- [Requisitos de Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
- [Requisitos de Vite 7](https://v7.vite.dev/guide/).
- [Override de atributos en Docker Compose](https://docs.docker.com/reference/compose-file/merge/#replace-value).
