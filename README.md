# Piedrazul System

Monolito modular Spring Boot + Angular para **HE-01: Consulta de citas medicas**.

## Estructura

```
backend/
  piedrazul-app/              # Punto de entrada (puerto 8080)
  modules/
    shared-kernel/            # Contratos ISP entre modulos
    user/                     # Datos de pacientes (enriquecimiento)
    availability/             # Catalogo de medicos
    appointment/              # Caso de uso de busqueda HE-01
frontend/                     # Pantalla del agendador
```

## Configuracion de base de datos

PostgreSQL 16 en un **esquema unico** (`public`). No hay Flyway/Liquibase: Hibernate usa `ddl-auto=update`.

Credenciales de desarrollo (alineadas con `docker-compose.yml`):

| Campo | Valor |
|-------|--------|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `piedrazul_db` |
| Usuario | `piedrazul_user` |
| Password | `piedrazul_dev` |

Se pueden sobreescribir con `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER` y `POSTGRES_PASSWORD`.

## Ejecucion

```bash
# PostgreSQL
docker compose up -d

# Backend (desde la raiz del repo)
./mvnw -pl backend/piedrazul-app -am spring-boot:run

# Frontend (otra terminal)
cd frontend
npm install
npm start
```

- App: http://localhost:4200/agendador
- Swagger: http://localhost:8080/swagger-ui.html

## API HE-01

`GET /api/v1/appointments/doctor/{doctorId}?date=YYYY-MM-DD`

`GET /api/v1/doctors` — lista de medicos para el filtro

## Datos de demostracion

Al primer arranque (BD vacia) `DemoDataInitializer` crea:

- Medico: Dra. Maria Lopez (Medicina General)
- Paciente: Juan Perez, usuario `paciente`, documento `1234567890`
- 2 citas para **manana** (09:00 CONFIRMED, 10:30 PENDING)

## Historias de usuario cubiertas

| ID | Descripcion |
|----|-------------|
| HU-1.1 | Buscar citas por medico y fecha |
| HU-1.2 | Visualizar listado en tabla |
| HU-1.3 | Mostrar cantidad total de citas |

## Tu siguiente paso

Completa el `TODO` en `SearchAppointmentsByDoctorAndDateUseCaseTest.java`.
