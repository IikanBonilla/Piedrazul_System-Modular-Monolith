# Arquitectura de Piedrazul

Diagramas del monolito modular y de la SPA, leídos del código de esta rama. Describen lo que hay hoy. Donde una historia del primer corte no tiene clases ni pantallas, el diagrama lo marca como **pendiente / no implementado**.

Login, roles y la consulta de citas **sí están implementados**. No aparecen como pendientes.

## Diagramas C4

| Archivo | Qué muestra |
|---|---|
| `c4/01-contexto.puml` | Administrador, médico, paciente y agendador frente a Piedrazul, y PostgreSQL. Las flechas dicen lo que cada rol hace hoy. |
| `c4/02-contenedores.puml` | SPA Angular, monolito Spring Boot y PostgreSQL. |
| `c4/03-componentes-backend.puml` | Módulos `piedrazul-app`, `user`, `availability`, `appointment` y `shared-kernel`, y qué tabla toca cada uno. |
| `c4/04-componentes-frontend.puml` | Rutas Angular, páginas, servicios, interceptor y guards. |
| `c4/05-nivel4-citas.puml` | Clases reales del módulo de citas: controlador, caso de uso, puertos y adaptador JPA. |

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

Las imágenes ya generadas para el video están en `c4/img/`: `contexto.png`, `contenedores.png`, `componentes.png` y `nivel4-citas.png`.

También se pueden pegar en el [servidor público de PlantUML](https://www.plantuml.com/plantuml/uml/). Ese servidor necesita salida a `raw.githubusercontent.com` para resolver los `!include` de C4-PlantUML.

## Qué abrir en el video de arquitectura (2 minutos)

1. `01-contexto.puml`: los cuatro actores y PostgreSQL.
2. `02-contenedores.puml`: Angular, Spring Boot y la base.
3. `03-componentes-backend.puml`: los módulos del monolito. `appointment` no abre las tablas de usuario ni de médico; usa los puertos del `shared-kernel`.
4. `05-nivel4-citas.puml`: el caso de uso, el puerto y el adaptador de la consulta de citas.
5. Una vista 4+1, la de escenarios: HE-01 está implementada.

No hace falta recorrer las cuatro secuencias en esos dos minutos. La de HE-01 sirve después, en el bloque de código.
