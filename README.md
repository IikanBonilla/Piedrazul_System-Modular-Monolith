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

Edita `backend/piedrazul-app/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/TU_BASE_DE_DATOS
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
```

Si usas Docker, alinea esos valores con `docker-compose.yml`.

## Ejecucion

```bash
# Opcional: levantar PostgreSQL
docker-compose up -d

# Backend
cd backend/piedrazul-app
mvn spring-boot:run

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

Al primer arranque (BD vacia) se crean 1 medico, 1 paciente y 2 citas para **manana**.

## Historias de usuario cubiertas

| ID | Descripcion |
|----|-------------|
| HU-1.1 | Buscar citas por medico y fecha |
| HU-1.2 | Visualizar listado en tabla |
| HU-1.3 | Mostrar cantidad total de citas |

## Tu siguiente paso

Completa el `TODO` en `SearchAppointmentsByDoctorAndDateUseCaseTest.java`.
