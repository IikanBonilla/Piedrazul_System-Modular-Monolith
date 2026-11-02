# Pendientes y cosas que no se dibujaron

Cada punto es algo pedido por el enunciado, o un patron que parece GoF, y que el codigo no sostiene lo bastante como para ponerlo en un diagrama.

## Identificadores HE-04 y HE-05

En el codigo aparecen `HE-01`, `HE-02` y `HE-03`. No aparece el texto `HE-04` ni `HE-05`.

Lo que el enunciado llama asi si esta implementado, con otros nombres:

- Exportar la jornada a CSV: `ExportAppointmentsCsvUseCase`, `AppointmentCsvWriter` y `GET /api/v1/appointments/doctor/{doctorId}/export`. El README lo llama HU-4.
- Historial por cedula y autocompletado: `SearchAppointmentsByPatientDocumentUseCase`, `SuggestPatientsByDocumentUseCase` y `PatientHistoryComponent`. El README lo llama HU-5.

No hay un documento de escenarios de aceptacion con ese titulo. Las pruebas de frontend comprueban el comportamiento que esas clases y pantallas ya tienen.

## No hay rol TERAPISTA

`Role` solo declara `SCHEDULER`, `PATIENT`, `DOCTOR` y `ADMINISTRATOR`. El realm describe al `DOCTOR` como "Medico o terapista". Las pantallas usan el mismo texto. No existe una clase, un rol ni una ruta de terapista distinta de `DOCTOR`.

## Despliegue

`docker-compose.yml` solo define `postgres` y `keycloak`. `piedrazul-app` y el frontend no son servicios de ese archivo. El puerto 4200 no esta en `frontend/angular.json`; el `start` de `frontend/package.json` es `ng serve`, y `http://localhost:4200` esta en `CorsConfig`, en `keycloak/import/piedrazul-realm.json` y en el README.

## Login

`AuthService.login` usa el password grant contra el client `piedrazul-web`. El realm tambien tiene `standardFlowEnabled` y PKCE `S256`, pero ninguna clase del frontend abre el flujo Authorization Code. Ese flujo no se dibujo.

## Relaciones que no existen

- `Appointment` guarda `patientId` y `doctorId` como `Long`. No hay `@ManyToOne` hacia `User` ni hacia `Doctor`.
- `User.doctorId` y `DoctorSchedulingConfig.doctorId` tambien son `Long`, no asociaciones JPA.
- `AppointmentRepositoryPort.existsActiveByDoctorAndDateTime` lo implementa `AppointmentRepositoryAdapter` y ningun caso de uso lo llama. `ScheduleAppointmentUseCase` calcula las franjas ocupadas con `findByDoctorAndDateRange`.
- `DoctorQueryPort.existsById` lo implementa `DoctorQueryAdapter`. `ConfigureDoctorSchedulingUseCase` y `GetDoctorSchedulingConfigUseCase` llaman `doctorRepository.existsById`, no al puerto.
- `ScheduleRules` solo lo usa su prueba. Ningun caso de uso de produccion lo referencia.
- `UserRepository` y los repositorios de availability extienden `JpaRepository` en el paquete de dominio. No tienen una clase adaptadora propia, aparte de `UserQueryAdapter`, `DoctorQueryAdapter` y `DoctorScheduleAdapter` para los puertos del shared-kernel.

## Patrones GoF que no se contaron

- Builder. `Appointment`, `User`, `Doctor`, `DoctorSchedulingConfig` y `AppointmentResponseDTO` llevan `@Builder` de Lombok. No hay una clase Builder escrita en el repositorio.
- Strategy. `AppointmentResponseEnricher` tiene una sola implementacion, `DefaultAppointmentResponseEnricher`. El comentario habla de poder agregar otras; en el codigo no hay una segunda.
- Factory Method. `DoctorSchedulingConfig.defaultFor` es un metodo estatico. No hay una subclase que lo redefina.
- Singleton. Los `@Service` y `@Component` usan el alcance de Spring. No hay una clase con instancia unica escrita a mano.
- Facade. Los controladores llaman casos de uso, pero ninguna clase se presenta como fachada de un subsistema.
- Decorator o Chain of Responsibility. `authInterceptor` envuelve `HttpHandler`, y `SecurityConfig` arma un `SecurityFilterChain`. Son mecanismos del framework, no una clase del proyecto que envuelva a otra con la misma interfaz.
- Proxy. `@Transactional` lo intercepta Spring. No hay una clase Proxy en el codigo.
- `KeycloakRealmRoleConverter` implementa `Converter<Jwt, AbstractAuthenticationToken>` de Spring. Adapta claims a autoridades, pero no implementa un puerto del proyecto. No se conto como Adapter para no estirar el patron.

## Pruebas y ZAP

No hay un informe OWASP ZAP en el repositorio. En backend no hay clase de prueba para `ExportAppointmentsCsvUseCase`, `SearchAppointmentsByPatientDocumentUseCase` ni `SuggestPatientsByDocumentUseCase`.
