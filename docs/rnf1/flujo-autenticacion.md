# Flujo de autenticacion

Diagrama del camino que queda despues de retirar el token local. La captura de pantalla del realm o del login se toma en la maquina de quien sustenta, con Keycloak y la app en ejecucion; este diagrama es la referencia del flujo.

```mermaid
sequenceDiagram
    actor Persona
    participant Web as Angular (client publico piedrazul-web)
    participant Kc as Keycloak realm piedrazul
    participant Api as API Spring Resource Server
    participant Db as PostgreSQL

    Persona->>Web: usuario y contraseña
    Web->>Kc: POST /realms/piedrazul/protocol/openid-connect/token
    Note over Web,Kc: grant_type=password, client_id=piedrazul-web
    Kc-->>Web: access token JWT (roles del realm)
    Web->>Api: GET /api/v1/auth/me con Authorization Bearer
    Api->>Kc: lee el JWKS del issuer y valida firma, issuer y expiracion
    Api->>Api: mapea los roles del token a ROLE_*
    Api->>Db: busca la ficha por preferred_username
    Db-->>Api: id, doctorId y datos de contacto
    Api-->>Web: perfil de sesion
    Web->>Api: operacion de negocio con el mismo Bearer

    alt sin token, firma invalida o token vencido
        Api-->>Web: 401 UNAUTHORIZED o SESSION_EXPIRED
    else token valido y rol insuficiente
        Api-->>Web: 403 FORBIDDEN
    else token valido y rol permitido
        Api-->>Web: respuesta del caso de uso
    end
```

Registro de una cuenta nueva:

```mermaid
sequenceDiagram
    actor Persona
    participant Web as Angular
    participant Api as API
    participant Kc as Keycloak
    participant Db as PostgreSQL

    Persona->>Web: formulario de paciente o medico
    Web->>Api: POST /api/v1/auth/register
    Api->>Kc: Admin API crea el usuario y asigna PATIENT o DOCTOR
    Api->>Db: guarda la ficha clinica
    Api-->>Web: 201 Cuenta creada
    Persona->>Web: inicia sesion con esa misma contraseña
```
