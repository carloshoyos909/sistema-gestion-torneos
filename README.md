# Sistema de Gestión de Torneos

Sistema para gestión de torneos deportivos y estadísticas.

## Tecnologías

- Java 17
- Spring Boot 3.2.0
- Vaadin 24.3.0
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Flyway
- Maven

## Ejecución local

### 1. Base de datos

Requiere Docker Desktop.

```bash
docker compose up -d
```

La base queda disponible en `localhost:5432`, con base `torneos`.

### 2. Backend

```bash
mvn clean spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

Flyway crea y versiona el esquema automáticamente.

### 3. Configuración

La aplicación permite sobrescribir las variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`

El archivo `.env` no debe subirse al repositorio.

## Funcionalidades del Sprint 2

- Persistencia de torneos.
- Registro persistente de equipos.
- Asociación persistente de jugadores a equipos.
- Persistencia del fixture.
- Consulta de equipos y jugadores.
- Consulta de partidos.
- API REST para registro y consulta.

## API

La documentación de endpoints se encuentra en [`docs/API.md`](docs/API.md).

## Arquitectura

La documentación técnica se encuentra en [`docs/ARQUITECTURA-SPRINT-2.md`](docs/ARQUITECTURA-SPRINT-2.md).

## Documentación del Sprint

Ver [`docs/SPRINT-2.md`](docs/SPRINT-2.md).
