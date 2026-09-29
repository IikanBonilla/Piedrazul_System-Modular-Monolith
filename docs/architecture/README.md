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
| `vistas-4+1/vista-escenarios.puml` | Casos de uso: login, registro, búsqueda con tabla y total, perfil. Agendar y configurar horario quedan en `<<pendiente>>`. |
| `vistas-4+1/vista-logica.puml` | Clases `User`, `Role`, `Doctor`, `Appointment` y `AppointmentStatus`, más los tres puertos. |
| `vistas-4+1/vista-procesos.puml` | Dos secuencias: buscar citas (HE-01) e iniciar sesión. |
| `vistas-4+1/vista-desarrollo.puml` | Carpetas de Angular y módulos Maven, y que `piedrazul-app` es quien une los módulos. |
| `vistas-4+1/vista-fisica.puml` | Navegador, `ng serve` en el 4200, JVM en el 8080 y PostgreSQL en `localhost:5432`. |

El detalle de patrones está en `patrones-de-diseno.md`. Los escenarios de usabilidad y seguridad están en `escenarios-de-calidad.md`.

## Cómo renderizarlos

Hace falta [PlantUML](https://plantuml.com/) y Graphviz (`dot`). Los C4 descargan la librería al renderizar:

```bash
sudo apt-get update && sudo apt-get install -y plantuml graphviz
plantuml -tpng docs/architecture/c4/*.puml docs/architecture/vistas-4+1/*.puml
```

`vista-procesos.puml` y `vista-desarrollo.puml` generan más de una imagen porque contienen varios bloques `@startuml`.

Las imágenes ya generadas para el video están en `c4/img/` (C4) y en `vistas-4+1/img/` (casos de uso, lógica, procesos de búsqueda, login, desarrollo y física).

También se pueden pegar en el [servidor público de PlantUML](https://www.plantuml.com/plantuml/uml/). Ese servidor necesita salida a `raw.githubusercontent.com` para resolver los `!include` de C4-PlantUML.

## Qué abrir en el video de arquitectura (2 minutos)

1. `01-contexto.puml`: los cuatro actores y PostgreSQL.
2. `02-contenedores.puml`: Angular, Spring Boot y la base.
3. `03-componentes-backend.puml`: los módulos del monolito. `appointment` no abre las tablas de usuario ni de médico; usa los puertos del `shared-kernel`.
4. `05-nivel4-citas.puml`: el caso de uso, el puerto y el adaptador de la consulta de citas.
5. Una vista 4+1, la de escenarios: HE-01 está implementada.

No hace falta recorrer las cuatro secuencias en esos dos minutos. La de HE-01 sirve después, en el bloque de código.
