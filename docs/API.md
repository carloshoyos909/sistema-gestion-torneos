# API REST — Sprint 2

Base URL local: `http://localhost:8080`

## Crear torneo

`POST /api/torneos`

```json
{
  "nombre": "Copa Ingeniería 2026",
  "deporte": "Fútbol",
  "categoria": "Universitaria"
}
```

Respuesta esperada: `201 Created`.

## Consultar torneos

`GET /api/torneos`

## Registrar equipo y jugadores

`POST /api/torneos/1/equipos`

```json
{
  "nombre": "UPB FC",
  "jugadores": [
    "Santiago Pérez",
    "Carlos Gómez",
    "Juan Rodríguez"
  ]
}
```

Respuesta esperada: `201 Created`.

## Consultar equipos

`GET /api/torneos/1/equipos`

La respuesta contiene el ID del equipo y la lista de jugadores asociados.

## Programar partido

`POST /api/torneos/1/partidos`

```json
{
  "equipoLocalId": 1,
  "equipoVisitanteId": 2,
  "fecha": "2026-10-15",
  "hora": "19:00:00",
  "cancha": "Cancha Principal"
}
```

Respuesta esperada: `201 Created`.

## Consultar fixture

`GET /api/torneos/1/partidos`

Los partidos se devuelven ordenados por fecha y hora.

## Errores

Los datos inválidos o conflictos de negocio devuelven `400 Bad Request` con un JSON de la forma:

```json
{
  "error": "mensaje descriptivo"
}
```
