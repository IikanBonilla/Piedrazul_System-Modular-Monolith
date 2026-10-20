# Patrones de diseño y SOLID aplicados en el código

Cada entrada señala un archivo que existe. Si el patrón está a medias, el texto lo dice. No se listan patrones que el código no usa.

## Arquitectura hexagonal (puertos y adaptadores), parcial

El módulo de citas separa el caso de uso de JPA.

- Puerto: `backend/modules/appointment/src/main/java/com/groupsoft/piedrazul/appointment/domain/port/AppointmentRepositoryPort.java`
- Adaptador: `backend/modules/appointment/src/main/java/com/groupsoft/piedrazul/appointment/infrastructure/persistence/AppointmentRepositoryAdapter.java`
- Caso de uso que solo ve el puerto: `SearchAppointmentsByDoctorAndDateUseCase.java`

Entre módulos, el contrato compartido vive en el kernel y el adaptador vive en el módulo dueño del dato:

- `shared/port/UserQueryPort.java` lo implementa `user/infrastructure/adapter/UserQueryAdapter.java`
- `shared/port/DoctorQueryPort.java` lo implementa `availability/infrastructure/adapter/DoctorQueryAdapter.java`

Problema que resuelve: `appointment` puede enriquecer una cita con nombre de paciente y de médico sin depender del módulo `user` ni del módulo `availability`. Esos módulos no se referencian entre sí en el `pom.xml`; solo dependen de `shared-kernel`. Quien los ensambla es `piedrazul-app`.

No es hexagonal completo:

- `User`, `Doctor` y `Appointment` son entidades JPA dentro del paquete de dominio.
- `UserRepository` y `DoctorRepository` extienden `JpaRepository` y están en el paquete `domain`.
- `LoginUseCase`, `RegisterUserUseCase` y `DoctorQueryService` dependen de esos repositorios, no de un puerto.
- `RegisterAccountService` (`piedrazul-app`) inyecta `DoctorRepository` y `UserRepository` directamente.

## Repository

- `AppointmentRepositoryPort` más `AppointmentJpaRepository`: la búsqueda por médico y rango horario.
- `UserRepository`: búsqueda por usuario, correo y documento, y el `save` del registro.
- `DoctorRepository`: catálogo de médicos y el alta cuando alguien se registra como médico.

Problema que resuelve: los casos de uso no escriben SQL. La consulta de citas ni siquiera nombra Spring Data; las de usuario y médico sí, porque el repositorio es la interfaz de Spring Data.

## Use Case / Application Service

- `SearchAppointmentsByDoctorAndDateUseCase`: único responsable de HE-01.
- `LoginUseCase`: credenciales, usuario inactivo y `AuthenticatedUser`.
- `RegisterUserUseCase`: validar, rechazar duplicados y crear el `User`.
- `DoctorQueryService`: listar médicos activos para el combo. Es un servicio de aplicación, no un caso de uso con ese nombre.
- `RegisterAccountService`: después del alta, si el rol es `DOCTOR`, crea la fila `Doctor` y guarda `doctorId`. Está en `piedrazul-app` porque necesita los dos módulos.

Problema que resuelve: los controladores no contienen esas reglas. `AppointmentSearchController` y `AuthController` solo reciben HTTP y delegan.

## DTO

Objetos de entrada y salida, distintos de las entidades:

- `AppointmentSearchResultDTO` y `AppointmentResponseDTO`
- `DoctorResponseDTO`
- `LoginRequest`, `LoginResponse`, `AuthenticatedUser`
- `RegisterUserRequest`, `RegisterUserResponse`
- `UserSummary` y `DoctorSummary` (records del kernel, para no publicar la entidad al otro módulo)

En el front, las interfaces `AppointmentSearchResultDTO`, `DoctorDTO`, `SessionUser`, `LoginPayload` y `RegisterPayload` repiten ese contrato para `HttpClient`.

Problema que resuelve: la tabla del agendador recibe nombre y documento del paciente sin exponer `password` ni el resto de `User`.

## Builder

Lombok `@Builder` en `Appointment`, `Doctor`, `User`, `AppointmentResponseDTO`, `AppointmentSearchResultDTO` y `DoctorResponseDTO`. `AppointmentResponseDTO` usa `@Builder(toBuilder = true)` para que el enriquecedor complete campos sobre una copia.

Problema que resuelve: armar la cita de demostración, el usuario registrado y el DTO de respuesta sin un constructor de muchos argumentos sueltos.

## Dependency Injection

- Backend: constructores generados por `@RequiredArgsConstructor` en casos de uso, adaptadores, controladores y `AuthFilter`. El `PasswordEncoder` es un `@Bean` de `PasswordEncoderConfig`.
- Frontend: servicios con `providedIn: 'root'`. `app.config.ts` registra el router, `HttpClient` y `authInterceptor`. Las páginas piden `AuthService`, `AppointmentService` y `AvailabilityService` por constructor o `inject()`.

Problema que resuelve: el caso de uso de búsqueda se puede probar sustituyendo los puertos, como hace `SearchAppointmentsByDoctorAndDateUseCaseTest`. La pantalla no instancia `HttpClient` a mano.

## Adapter

Los tres adaptadores de arriba (`AppointmentRepositoryAdapter`, `UserQueryAdapter`, `DoctorQueryAdapter`) traducen un contrato estrecho a JPA.

`AuthFilter` y `authInterceptor` no son el patrón Adapter. Son el mecanismo de sesión y están descritos aparte, más abajo.

## Strategy

`AppointmentResponseEnricher` es la estrategia. `AppointmentAssembler` depende de la interfaz. `DefaultAppointmentResponseEnricher` es la única implementación y Spring la inyecta.

Problema que resuelve: agregar otro enriquecedor (la especialidad o la sala, como dice el comentario de la interfaz) no obliga a modificar el caso de uso de búsqueda.

Hoy no hay una segunda estrategia ni un selector. El patrón está preparado, con una sola variante.

## Filtro e interceptor de autenticación

- `AuthFilter` extiende `OncePerRequestFilter`. Deja pasar `OPTIONS`, `/api/v1/auth/login`, `/api/v1/auth/register` y Swagger. El resto de `/api/` exige `Bearer`.
- `authInterceptor` (`frontend/src/app/core/auth/auth.interceptor.ts`) copia el token de `AuthService` al encabezado y, ante un 401 que no sea el login, limpia la sesión.

Problema que resuelve: las pantallas y los controladores de citas no repiten la comprobación del token. El paciente no necesita conocer la cabecera HTTP.

No es la cadena de filtros de Spring Security. La dependencia de seguridad del módulo `user` es solo `spring-security-crypto`, para BCrypt.

## Guard de rutas

`roleGuard` y `homeRedirectGuard` en `frontend/src/app/core/auth/auth.guard.ts`. Las rutas de `app.routes.ts` los usan.

Problema que resuelve: un paciente que escribe `/agendador` no ve la agenda; va a `/acceso-denegado`. Un anónimo va a `/login`.

## Carga perezosa de rutas

`loadComponent` y `loadChildren` en `app.routes.ts` y `scheduler/routes.ts`.

Problema que resuelve: el paquete inicial no incluye el formulario de registro ni la tabla de citas hasta que la ruta se abre.

## SOLID que el código sostiene

**Responsabilidad única.** `SearchAppointmentsByDoctorAndDateUseCase` busca y cuenta. `RegistrationRules` valida. `RegisterUserUseCase` persiste. `GlobalExceptionHandler` traduce excepciones a HTTP. `AuthTokenService` solo emite y resuelve tokens.

**Abierto/cerrado.** El punto real es `AppointmentResponseEnricher`: el ensamblador queda cerrado a un enriquecedor nuevo. No hay otro ejemplo claro.

**Segregación de interfaces.** `UserQueryPort` solo tiene `findById`. `DoctorQueryPort` solo tiene `existsById` y `findById`. El comentario de ambos archivos lo declara. El caso de uso de citas no recibe `UserRepository`, que además sabe buscar por correo y documento.

**Inversión de dependencias.** Se cumple en la búsqueda: el caso de uso depende de `AppointmentRepositoryPort`, `DoctorQueryPort` y `AppointmentResponseEnricher`, y los adaptadores implementan esos contratos. No se cumple en login, registro ni en `DoctorQueryService`, que dependen de `JpaRepository`.

**Sustitución de Liskov.** No hay dos implementaciones intercambiables de un mismo puerto en producción. Las pruebas sustituyen colaboradores con mocks, pero eso no demuestra el principio en el código de producción. No se reclama como aplicado.

## Mecanismos que no se presentan como patrón de diseño

- `DemoDataInitializer` siembra datos. No es una fábrica de dominio.
- `validators.ts` repite, en el navegador, reglas que también están en `RegistrationRules`. Es duplicación a propósito para fallar antes de la red, no un patrón compartido. `timeRangeError` en ese mismo archivo tampoco tiene pantalla: solo lo llama `validators.spec.ts`, igual que `ScheduleRules` en el backend.
- `ScheduleRules` no participa en ningún flujo. Existe la clase y su prueba, nada más.
