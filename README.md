# Piedrazul System

Monolito modular Spring Boot y SPA Angular para agendar citas. La API valida el JWT de Keycloak y persiste en un unico esquema de PostgreSQL. El codigo nombra HE-01, HE-02 y HE-03. La exportacion CSV y el historial por cedula estan implementados; este README los sigue llamando HU-4 y HU-5 porque el texto HE-04 y HE-05 no aparece en el codigo.

## Estructura

```
backend/
  piedrazul-app/              # Arranque, controladores y seguridad. Puerto 8080
  modules/
    shared-kernel/            # Puertos ISP y DTOs compartidos. Sin dependencias Maven
    user/                     # artifact user-module. Ficha, registro y sugerencias
    availability/             # artifact availability-module. Medicos y horario
    appointment/              # artifact appointment-module. Consulta, agenda, CSV e historial
frontend/                     # Angular. ng serve, origen http://localhost:4200
keycloak/import/              # Realm que Keycloak importa al arrancar
docker-compose.yml            # Solo PostgreSQL y Keycloak
docs/architecture/            # C4, vistas 4+1, patrones GoF y pendientes
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

Keycloak tiene que estar arriba antes que el backend: la API descarga las llaves del issuer al arrancar.

```bash
# PostgreSQL y Keycloak
docker compose up -d

# Backend (desde la raiz del repo)
./mvnw -pl backend/piedrazul-app -am spring-boot:run

# Frontend (otra terminal)
cd frontend
npm install
npm start
```

- App: http://localhost:4200
- Swagger: http://localhost:8080/swagger-ui.html
- Keycloak: http://localhost:8081 (admin / admin)

## Autenticacion con Keycloak (RNF1)

La contraseña ya no se valida en el monolito. Keycloak emite el JWT y Spring Security lo valida como OAuth2 Resource Server.

| Dato | Valor |
|------|--------|
| Realm | `piedrazul` |
| Issuer | `http://localhost:8081/realms/piedrazul` |
| Client publico | `piedrazul-web` (sin secreto, Authorization Code + PKCE y password grant) |
| Origen del frontend | `http://localhost:4200` |
| Consola de administracion | `http://localhost:8081` con `admin` / `admin` |
| Roles de realm | `PATIENT`, `DOCTOR`, `SCHEDULER`, `ADMINISTRATOR` |

El realm, los roles, el client y los usuarios semilla se importan desde `keycloak/import/piedrazul-realm.json` al arrancar el contenedor. Esos usuarios coinciden con la ficha clinica que crea `DemoDataInitializer`.

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `paciente` | `paciente123` | `PATIENT` |
| `medico` | `medico123` | `DOCTOR` |
| `agendador` | `agendador123` | `SCHEDULER` |
| `admin` | `admin123` | `ADMINISTRATOR` |

El frontend pide el token directo al client publico y luego llama `GET /api/v1/auth/me` para completar la ficha local. El registro publico sigue en `POST /api/v1/auth/register`: guarda la ficha clinica y crea el usuario en Keycloak con el mismo rol.

Si el realm se creo vacio, la coleccion `docs/rnf1/postman/piedrazul-keycloak-seed.postman_collection.json` crea los cuatro usuarios por la Admin API y deja resuelta la asignacion del rol `PATIENT`. Para los otros tres se repite la busqueda del usuario y el `role-mapping` con `DOCTOR`, `SCHEDULER` y `ADMINISTRATOR`.

Decision de alcance, escenarios de calidad y diagrama:

- `docs/rnf1/decision-migracion-keycloak.md`
- `docs/rnf1/escenarios-calidad-seguridad.md`
- `docs/rnf1/flujo-autenticacion.md`

## API de citas

| Metodo | Ruta | Quien | Para que |
|--------|------|-------|----------|
| GET | `/api/v1/appointments/doctor/{doctorId}?date=YYYY-MM-DD` | Agendador, medico | Listado y cantidad de citas de esa jornada |
| GET | `/api/v1/appointments/doctor/{doctorId}/export?date=YYYY-MM-DD` | Agendador, medico | CSV (separador `;`, UTF-8) para la jornada |
| GET | `/api/v1/appointments/patient?document={cedula}` | Agendador, medico | Historial de citas del paciente |
| GET | `/api/v1/patients/suggest?document={prefijo}` | Agendador, medico | Autocompletado desde el tercer digito de la cedula |
| GET | `/api/v1/doctors` | Agendador, medico, paciente, administrador | Catalogo para los filtros |
| GET | `/api/v1/doctors/{doctorId}/slots?date=YYYY-MM-DD` | Paciente | Franjas libres |
| POST | `/api/v1/appointments` | Paciente | Agendar una cita |
| GET/PUT | `/api/v1/admin/doctors/{doctorId}/scheduling-config` | Administrador | Ventana en semanas, dias, franja e intervalo |

## Datos de demostracion

Al primer arranque (BD vacia) `DemoDataInitializer` crea:

- Medico: Dra. Maria Lopez (Medicina General)
- Paciente: Juan Perez, usuario `paciente`, documento `1234567890`
- 2 citas para **manana** (09:00 CONFIRMED, 10:30 PENDING)

## Historias HE-01 a HE-05

Los ids HE-01, HE-02 y HE-03 estan en los controladores y en los casos de uso. HU-4 y HU-5 son los nombres de este README para la exportacion y el historial: el codigo no usa HE-04 ni HE-05. No hay un rol `TERAPISTA`; el realm describe el rol `DOCTOR` como medico o terapista.

| ID en el repositorio | Donde esta | Que cubre |
|----------------------|------------|-----------|
| HE-01 | `SearchAppointmentsByDoctorAndDateUseCase` | HU-1.1 buscar por medico y fecha, HU-1.2 listado, HU-1.3 total |
| HE-02 | `ScheduleAppointmentUseCase`, `PatientRegistrationController`, `QueryAvailableSlotsUseCase` | HU-2.1 agendar, HU-2.2 registrar paciente, HU-2.3 franjas |
| HE-03 | `ConfigureDoctorSchedulingUseCase`, `GetDoctorSchedulingConfigUseCase`, `DoctorWorkingHours` | HU-3.1 a HU-3.5: ventana, dias, franja e intervalo |
| HU-4 | `ExportAppointmentsCsvUseCase` y `AppointmentListComponent.exportCsv` | Medico o agendador exporta la jornada a CSV |
| HU-5 | `SearchAppointmentsByPatientDocumentUseCase`, `SuggestPatientsByDocumentUseCase` y `PatientHistoryComponent` | Medico o agendador consulta el historial por cedula, con autocompletado desde el tercer digito |

Quien puede llamar cada ruta esta en `SecurityConfig`. El registro publico (`POST /api/v1/auth/register`) no exige rol y `RegistrationRules` solo acepta `PATIENT` y `DOCTOR`.

No hay informe OWASP ZAP en el repositorio. En backend no hay clase de prueba para `ExportAppointmentsCsvUseCase`, `SearchAppointmentsByPatientDocumentUseCase` ni `SuggestPatientsByDocumentUseCase`.

## Documentacion de arquitectura

- `docs/architecture/c4/`
- `docs/architecture/vistas-4+1/`
- `docs/architecture/patrones-gof.md`
- `docs/architecture/pendientes.md`
