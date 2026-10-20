# Escenarios de calidad de seguridad

Dos escenarios del requisito RNF1. Los automatiza `SecurityAuthorizationTest` y `GlobalExceptionHandlerSecurityTest`.

## Escenario 1. Token ausente, invalido o vencido

**Dado** un cliente que llama un recurso protegido, por ejemplo `GET /api/v1/doctors`.

**Cuando** la peticion no trae `Authorization: Bearer`, el token no lo firmo el realm `piedrazul`, o el access token ya expiro.

**Entonces** la API responde `401` y no ejecuta el caso de uso.

- Sin token, el cuerpo es `{ "code": "UNAUTHORIZED", "message": "Debes iniciar sesión." }`.
- Con token vencido, el codigo es `SESSION_EXPIRED` y el mensaje pide ingresar de nuevo.
- El registro publico (`POST /api/v1/auth/register` y `POST /api/v1/patients/register`) sigue abierto y no entra en este escenario.

## Escenario 2. Token valido con un rol que no corresponde

**Dado** un access token vigente del realm `piedrazul` cuyo rol de negocio no es el que exige la ruta.

**Cuando** un paciente consulta `GET /api/v1/appointments/doctor/{id}`, un agendador hace `POST /api/v1/appointments`, o cualquiera que no sea administrador entra a `/api/v1/admin/**`.

**Entonces** la API responde `403` con `{ "code": "FORBIDDEN", "message": "No tienes permiso para acceder a esta función." }` y no ejecuta el caso de uso.

El mismo token, en una ruta de su rol, recibe la respuesta normal del caso de uso. El rol se lee del token (`realm_access.roles` o el claim `roles`), no de la tabla local.
