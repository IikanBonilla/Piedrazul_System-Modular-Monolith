# Decision de alcance: migracion a Keycloak

Registro de la decision RNF1 para implementar la autenticacion fuera del monolito. No sustituye el acta de la reunion de equipo: si el grupo ya firmo otro alcance, este documento se ajusta a ese acuerdo.

## Decision

Keycloak es el unico emisor de identidad. Piedrazul queda como OAuth2 Resource Server: valida la firma, el issuer y la expiracion del access token, y autoriza con los roles que vienen dentro del token.

## Se conserva

- Los cuatro roles que ya usa el codigo: `PATIENT`, `DOCTOR`, `SCHEDULER` y `ADMINISTRATOR`.
- La ficha clinica en PostgreSQL (`app_users`, citas, medicos). El `id` local, el `doctorId` y los datos de contacto no se mueven a Keycloak.
- Las reglas de acceso que ya tenia el filtro local:
  - agendador consulta citas;
  - paciente consulta franjas y agenda;
  - administrador configura horarios;
  - el catalogo de medicos lo comparten agendador, paciente y administrador;
  - registro publico de paciente y medico.
- La pantalla de usuario y contraseña. El formulario pide el token al client publico; no redirige a la pagina hospedada de Keycloak.

## Se retira

- `AuthFilter`, `AuthTokenService` y el token opaco en memoria.
- `LoginUseCase` como verificador de contraseñas. Una contraseña correcta en la base local ya no abre sesion.
- El endpoint `POST /api/v1/auth/login`.

## Se adapta

- `RegisterUserUseCase` sigue validando y guardando la ficha. Antes de persistirla crea el usuario en el realm y le asigna el rol. Si la base falla, borra ese usuario de Keycloak.
- `GET /api/v1/auth/me` arma la sesion de la interfaz con el JWT mas la ficha local.

## Fuera de este alcance

- HTTPS, secretos de produccion y una base externa para Keycloak. En local, Keycloak usa su almacenamiento de `start-dev`.
- Login redirigido a la pagina de Keycloak (Authorization Code en el navegador). El client publico ya lo permite; la pantalla actual usa password grant para no cambiar el formulario.
- Sincronizar cambios de rol hechos solo en la consola de Keycloak hacia la columna `role` de `app_users`. La autorizacion de la API lee el token, no esa columna.
