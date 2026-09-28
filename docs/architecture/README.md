# Arquitectura de Piedrazul

Diagramas del monolito modular y de la SPA, leídos del código de esta rama. Describen lo que hay hoy. Donde una historia del primer corte no tiene clases ni pantallas, el diagrama lo marca como **pendiente / no implementado**.

Login, roles y la consulta de citas **sí están implementados**. No aparecen como pendientes.

## Diagramas C4

| Archivo | Qué muestra |
|---|---|
| `c4/01-contexto.puml` | Actores que el código distingue (agendador, administrador, paciente y médico) y PostgreSQL. HE-02 y HE-03 no salen como flujos porque no existen. |
| `c4/02-contenedores.puml` | Navegador, SPA en el puerto 4200, `piedrazul-app` en el 8080 y PostgreSQL en `localhost:5432`. La sesión vive en memoria de la JVM. |
| `c4/03-componentes-backend.puml` | Módulos `shared-kernel`, `user`, `availability`, `appointment` y `piedrazul-app`, con los puertos y adaptadores reales. En ámbar, las dependencias directas a JPA. |
| `c4/04-componentes-frontend.puml` | Rutas Angular, páginas, `AuthService`, `AppointmentService`, `AvailabilityService`, el interceptor y los guards. |

## Vistas 4+1

| Archivo | Qué muestra |
|---|---|
| `vistas-4+1/vista-escenarios.puml` | Casos de uso de HE-01, HE-02 y HE-03. HE-01 incluye la tabla y el total. HE-02 y HE-03 quedan en estereotipo pendiente. El registro sí está, como prerrequisito, y no crea citas. |
| `vistas-4+1/vista-logica.puml` | Clases de `User`, `Doctor` y `Appointment`, los tres casos de uso y los puertos `UserQueryPort`, `DoctorQueryPort` y `AppointmentRepositoryPort`. |
| `vistas-4+1/vista-procesos.puml` | Cuatro secuencias en el mismo archivo: búsqueda HE-01, el hueco de HE-02, el registro que sí existe, y el hueco de HE-03. |
| `vistas-4+1/vista-desarrollo.puml` | Dos diagramas: dependencias Maven (los módulos de negocio no se referencian entre sí; los une `piedrazul-app`) y el corte hexagonal por paquetes. |
| `vistas-4+1/vista-fisica.puml` | Navegador, `ng serve`, JVM de Spring Boot y PostgreSQL local. El nodo Docker está dibujado en rojo porque el repositorio no tiene `Dockerfile` ni `docker-compose.yml`. |

El detalle de patrones está en `patrones-de-diseno.md`. Los escenarios de usabilidad y seguridad están en `escenarios-de-calidad.md`.

## Cómo renderizarlos

Hace falta [PlantUML](https://plantuml.com/) y Graphviz (`dot`). Los C4 descargan la librería al renderizar:

```bash
sudo apt-get update && sudo apt-get install -y plantuml graphviz
plantuml -tpng docs/architecture/c4/*.puml docs/architecture/vistas-4+1/*.puml
```

`vista-procesos.puml` y `vista-desarrollo.puml` generan más de una imagen porque contienen varios bloques `@startuml`.

También se pueden pegar en el [servidor público de PlantUML](https://www.plantuml.com/plantuml/uml/). Ese servidor necesita salida a `raw.githubusercontent.com` para resolver los `!include` de C4-PlantUML.

## Qué abrir en el video de arquitectura (2 minutos)

1. `01-contexto.puml`: quién usa el sistema y que la base es PostgreSQL.
2. `02-contenedores.puml`: Angular, el monolito y la base, en un solo proceso de backend.
3. `03-componentes-backend.puml`: el caso de uso de búsqueda depende de puertos; `UserQueryAdapter` y `DoctorQueryAdapter` los implementan.
4. `vista-escenarios.puml`: HE-01 está; HE-02 y HE-03 están marcados como pendientes.
5. Una frase de la vista de desarrollo: `user`, `availability` y `appointment` solo dependen de `shared-kernel` por Maven.

No hace falta recorrer las cuatro secuencias en esos dos minutos. La de HE-01 sirve después, en el bloque de código.
