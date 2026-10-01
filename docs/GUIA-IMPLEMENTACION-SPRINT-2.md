# Guía paso a paso — Sprint 2

> Esta guía parte del proyecto del Sprint 1 entregado por el equipo.

## 0. Antes de empezar

Requisitos:

- JDK 17.
- Maven.
- Docker Desktop.
- Git.
- PostgreSQL se ejecutará mediante Docker.

Verificar:

```powershell
java -version
mvn -version
git --version
docker --version
docker compose version
```

## 1. Crear rama

Desde la raíz del repositorio:

```powershell
git checkout main
git pull origin main
git checkout -b sprint-2/persistencia-postgresql
```

## 2. Limpiar artefactos compilados rastreados

El proyecto original contiene archivos de `target/` rastreados por Git. No deben versionarse.

```powershell
git rm -r --cached target
```

El `.gitignore` ya contiene `target/`.

## 3. Copiar los cambios del Sprint 2

Conservar el contenido de los archivos del proyecto y aplicar los archivos de esta entrega.

Nuevos elementos principales:

- `docker-compose.yml`
- `.env.example`
- `src/main/resources/application.properties`
- `src/main/resources/db/migration/V1__crear_esquema_inicial.sql`
- `model/Jugador.java`
- repositorios JPA
- DTOs
- servicios persistentes
- controladores REST
- manejador de errores
- vistas `EquiposView` y `FixtureView`

## 4. Levantar PostgreSQL

```powershell
docker compose up -d
```

Verificar:

```powershell
docker ps
```

Debe aparecer `torneos-postgres` con el puerto `5432` publicado.

## 5. Compilar

```powershell
mvn clean compile
```

`clean` elimina `target/`; Maven lo vuelve a crear y el `.gitignore` impide que se versionen sus archivos.

## 6. Ejecutar

```powershell
mvn spring-boot:run
```

Esperar a que Spring Boot indique que la aplicación inició en el puerto 8080.

Flyway ejecutará `V1__crear_esquema_inicial.sql` automáticamente.

## 7. Verificar tablas

```powershell
docker exec -it torneos-postgres psql -U postgres -d torneos
```

Dentro de PostgreSQL:

```sql
\dt
SELECT * FROM torneos;
SELECT * FROM equipos;
SELECT * FROM jugadores;
SELECT * FROM partidos;
```

Salir:

```sql
\q
```

## 8. Probar API de torneo

PowerShell:

```powershell
$body = @{
  nombre = "Copa Ingeniería 2026"
  deporte = "Fútbol"
  categoria = "Universitaria"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/torneos" `
  -ContentType "application/json" `
  -Body $body
```

Consultar:

```powershell
Invoke-RestMethod "http://localhost:8080/api/torneos" | ConvertTo-Json -Depth 5
```

## 9. Registrar equipo y jugadores

Suponiendo que el torneo creado tenga ID 1:

```powershell
$body = @{
  nombre = "UPB FC"
  jugadores = @("Santiago Pérez", "Carlos Gómez", "Juan Rodríguez")
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/torneos/1/equipos" `
  -ContentType "application/json" `
  -Body $body
```

Consultar:

```powershell
Invoke-RestMethod `
  "http://localhost:8080/api/torneos/1/equipos" |
  ConvertTo-Json -Depth 5
```

Registrar un segundo equipo para poder crear un partido.

## 10. Verificar persistencia real

Cerrar la aplicación Spring Boot con `Ctrl+C`.

Volver a ejecutar:

```powershell
mvn spring-boot:run
```

Consultar nuevamente:

```powershell
Invoke-RestMethod "http://localhost:8080/api/torneos/1/equipos" | ConvertTo-Json -Depth 5
```

Los registros deben seguir presentes porque están en PostgreSQL, no en una lista Java en memoria.

## 11. Programar partido

Primero consultar los equipos y tomar sus IDs.

```powershell
$body = @{
  equipoLocalId = 1
  equipoVisitanteId = 2
  fecha = "2026-10-15"
  hora = "19:00:00"
  cancha = "Cancha Principal"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/torneos/1/partidos" `
  -ContentType "application/json" `
  -Body $body
```

Consultar fixture:

```powershell
Invoke-RestMethod `
  "http://localhost:8080/api/torneos/1/partidos" |
  ConvertTo-Json -Depth 5
```

## 12. Probar interfaz

Abrir:

`http://localhost:8080`

Secuencia recomendada:

1. Crear torneo.
2. Registrar equipo y varios jugadores.
3. Registrar un segundo equipo.
4. Programar partido.
5. Abrir Consultar Equipos.
6. Abrir Consultar Fixture.
7. Reiniciar Spring Boot.
8. Volver a consultar equipos y fixture.

## 13. Probar reglas de negocio

### Equipo duplicado

Intentar registrar dos veces el mismo nombre en el mismo torneo.

Resultado esperado: mensaje de error.

### Partido contra sí mismo

Enviar:

```json
{
  "equipoLocalId": 1,
  "equipoVisitanteId": 1,
  "fecha": "2026-10-15",
  "hora": "19:00:00",
  "cancha": "Cancha Principal"
}
```

Resultado esperado: `400 Bad Request`.

### Equipos de otro torneo

Usar el ID de un equipo que no pertenezca al torneo indicado en la URL.

Resultado esperado: `400 Bad Request`.

## 14. Revisar Git

```powershell
git status
git diff --stat
git diff
```

No deben aparecer credenciales reales.

No deben aparecer `target/`, `node_modules/` ni `frontend/generated/` como archivos a subir.

## 15. Commit por bloques

```powershell
git add .gitignore pom.xml docker-compose.yml .env.example src/main/resources/application.properties src/main/resources/db/migration

git commit -m "chore: configurar PostgreSQL y Flyway"
```

Después:

```powershell
git add src/main/java/com/torneos/model src/main/java/com/torneos/repository src/main/java/com/torneos/service

git commit -m "feat: implementar persistencia de torneo equipos jugadores y partidos"
```

Después:

```powershell
git add src/main/java/com/torneos/dto src/main/java/com/torneos/controller

git commit -m "feat: exponer API REST para equipos y fixture"
```

Después:

```powershell
git add src/main/java/com/torneos/view

git commit -m "feat: integrar interfaz con backend persistente"
```

Finalmente:

```powershell
git add README.md docs

git commit -m "docs: documentar sprint 2 y API"
```

## 16. Push

```powershell
git push -u origin sprint-2/persistencia-postgresql
```

## 17. Pull Request

Crear un Pull Request desde:

`sprint-2/persistencia-postgresql` -> `main`

Título recomendado:

`feat: implementa Sprint 2 - persistencia e integración`

Descripción recomendada:

```markdown
## Objetivo
Implementar la persistencia de datos del torneo en PostgreSQL e integrar la interfaz con el backend.

## Cambios
- PostgreSQL + Flyway.
- Entidades JPA para torneo, equipo, jugador y partido.
- Repositories Spring Data JPA.
- Servicios transaccionales.
- API REST de registro y consulta.
- Integración de vistas Vaadin.
- Consulta de equipos y fixture.
- Validaciones de negocio.
- Documentación técnica.

## Validaciones realizadas
- Registro de torneo.
- Registro de equipo con jugadores.
- Consulta de equipos.
- Programación de partido.
- Consulta de fixture.
- Persistencia después de reiniciar la aplicación.
- Validación de equipo duplicado.
- Validación de partido contra sí mismo.

## Evidencias
Agregar capturas de PostgreSQL, API, interfaz y GitHub Project.

## Issue
Closes #<NUMERO_ISSUE_SPRINT_2>
```

## 18. Project de GitHub

Crear una agrupación de trabajo para Sprint 2 y las siguientes tareas:

1. Configurar PostgreSQL + Flyway.
2. Crear entidades JPA.
3. Crear repositories.
4. Persistir equipos y jugadores.
5. Persistir partidos.
6. Crear endpoints REST.
7. Integrar vistas Vaadin.
8. Implementar consultas de equipos y fixture.
9. Probar persistencia.
10. Documentar Sprint 2.

Moverlas:

`TO DO -> In Progress -> Complete`

Cuando el PR sea aprobado y mergeado, mover las tareas correspondientes a `Complete`.

## 19. Criterio final de terminado

El Sprint 2 se considera terminado cuando:

- PostgreSQL está funcionando.
- Flyway crea el esquema.
- Los datos se guardan mediante JPA.
- Los datos sobreviven al reinicio de la aplicación.
- Los jugadores quedan asociados a equipos.
- Los partidos quedan asociados al torneo y a sus dos equipos.
- GET de equipos devuelve registros persistidos.
- GET de partidos devuelve el fixture persistido.
- La interfaz permite registrar y consultar.
- El repositorio no contiene secretos ni artefactos compilados.
- Los commits están en GitHub.
- Existe PR y está mergeado.
- GitHub Project refleja el estado final.
- README y documentación del Sprint 2 están actualizados.
