# Patrones GoF que si estan en el codigo

Solo entra el patron que se ve en una interfaz del proyecto y en una clase que la implementa para hablar con otra tecnologia. El resto de candidatos esta en `docs/architecture/pendientes.md`.

## Adapter

El caso de uso depende de un puerto. La clase adaptadora traduce ese puerto a JPA o a la API de Keycloak. El problema que resuelve es el mismo en los cinco casos: el nucleo no debe conocer el mecanismo concreto de persistencia o de identidad.

| Clase | Puerto que implementa | Archivo | Problema que resuelve |
|-------|------------------------|---------|------------------------|
| `AppointmentRepositoryAdapter` | `AppointmentRepositoryPort` | `backend/modules/appointment/src/main/java/com/groupsoft/piedrazul/appointment/infrastructure/persistence/AppointmentRepositoryAdapter.java` | `SearchAppointmentsByDoctorAndDateUseCase`, `ScheduleAppointmentUseCase`, `QueryAvailableSlotsUseCase` y `SearchAppointmentsByPatientDocumentUseCase` piden citas sin usar `AppointmentJpaRepository`. |
| `UserQueryAdapter` | `UserQueryPort` | `backend/modules/user/src/main/java/com/groupsoft/piedrazul/user/infrastructure/adapter/UserQueryAdapter.java` | El modulo de citas obtiene `UserSummary` sin depender de la entidad `User` ni de `UserRepository`. |
| `DoctorQueryAdapter` | `DoctorQueryPort` | `backend/modules/availability/src/main/java/com/groupsoft/piedrazul/availability/infrastructure/adapter/DoctorQueryAdapter.java` | El modulo de citas valida y nombra un medico sin depender de `DoctorRepository`. |
| `DoctorScheduleAdapter` | `DoctorSchedulePort` | `backend/modules/availability/src/main/java/com/groupsoft/piedrazul/availability/infrastructure/adapter/DoctorScheduleAdapter.java` | El modulo de citas pide franjas candidatas sin conocer `DoctorSchedulingConfig` ni `DoctorWorkingHours`. |
| `KeycloakIdentityProvisioner` | `IdentityProvisioner` | `backend/piedrazul-app/src/main/java/com/groupsoft/piedrazul/infrastructure/security/KeycloakIdentityProvisioner.java` | `RegisterUserUseCase` da de alta la identidad sin conocer la Admin API de Keycloak. El puerto esta en `backend/modules/user/src/main/java/com/groupsoft/piedrazul/user/application/port/IdentityProvisioner.java`. |
