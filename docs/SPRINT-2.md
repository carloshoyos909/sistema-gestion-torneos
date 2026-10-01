# Sprint 2 — Persistencia e integración

## Objetivo

Implementar la persistencia de los datos del torneo mediante PostgreSQL e integrar la interfaz Vaadin con el backend para registrar y consultar equipos, jugadores y partidos.

## Alcance completado

- PostgreSQL configurado.
- Flyway configurado para versionar el esquema.
- Entidades JPA para torneo, equipo, jugador y partido.
- Relación Torneo -> Equipos.
- Relación Equipo -> Jugadores.
- Relación Partido -> Equipo local / visitante.
- Repositories con Spring Data JPA.
- Servicios transaccionales.
- Endpoints REST de registro y consulta.
- Integración de las vistas Vaadin con los servicios persistentes.
- Vista de consulta de equipos y jugadores.
- Vista de consulta del fixture.
- Validaciones de datos y reglas básicas de negocio.

## Criterios de aceptación

1. Al registrar un equipo, el equipo queda almacenado en PostgreSQL.
2. Los jugadores registrados desde la interfaz quedan asociados al equipo correcto.
3. Al reiniciar la aplicación, los equipos y jugadores siguen disponibles.
4. Los partidos registrados quedan almacenados en PostgreSQL.
5. Al reiniciar la aplicación, el fixture sigue disponible.
6. Los endpoints GET devuelven los datos persistidos.
7. No es posible registrar dos equipos con el mismo nombre dentro del mismo torneo.
8. No es posible programar un partido entre un equipo contra sí mismo.
9. No es posible programar un partido utilizando equipos que pertenezcan a otro torneo.
10. Las credenciales configurables de la base de datos no se almacenan en el repositorio.

## Evidencias recomendadas

- Captura de PostgreSQL con las cuatro tablas creadas.
- Captura de registro de torneo.
- Captura de registro de equipo con jugadores.
- Consulta SQL mostrando el equipo y sus jugadores.
- Captura de programación de partido.
- Consulta SQL mostrando el fixture.
- Capturas de `GET /api/torneos/{id}/equipos` y `GET /api/torneos/{id}/partidos`.
- Captura del Project de GitHub con las tareas del Sprint 2 en estado Complete.
- Enlace al Pull Request que integra Sprint 2.
