# Escenarios de calidad

Atributos prioritarios del primer corte: **usabilidad** y **seguridad**. Cada escenario usa el código actual. La seguridad dice qué cubre la implementación y qué falta.

Los actores de demostración que crea `DemoDataInitializer` son `admin` / `admin123`, `paciente` / `paciente123` y `medico` / `medico123`. No hay usuario con rol `SCHEDULER`.

## Usabilidad

### Escenario U1. Ver el listado y la cantidad sin salir de la pantalla

**Contexto.** Un administrador, un médico o un agendador ya inició sesión y está en `AppointmentListComponent` (`/agendador` o `/medico`). El combo de médicos sale de `GET /api/v1/doctors`.

**Estímulo.** Elige un médico, elige una fecha y pulsa «Buscar citas».

**Respuesta.** El componente valida el médico y la fecha con `appointmentDateError` antes de llamar a la red. Si faltan, el mensaje queda bajo el campo (`doctor-error`, `date-error`) y no hay petición. Si la búsqueda responde, la misma pantalla muestra la tabla y el badge `Total: N citas`. Si la lista viene vacía, muestra el texto de que no hay citas para ese profesional en esa fecha. Mientras espera, el botón cambia a «Buscando...» y aparece el indicador de estado.

**Medición de calidad.** La tarea termina en una sola vista: no hay una pantalla intermedia de resultados. El total es `AppointmentSearchResultDTO.total`, el tamaño de la lista que arma el caso de uso, y se ve sin contar filas a mano. Un criterio local inválido no produce una llamada HTTP.

**Resultado esperado.** Con la doctora de demostración y la fecha de mañana, la tabla muestra dos citas y el total es 2. Con otra fecha, el total es 0 y se ve el estado vacío. Con el médico o la fecha en blanco, el error aparece junto al control.

Esto cubre la historia del agendador. No cubre la usabilidad de agendar: no existe esa pantalla.

### Escenario U2. Corregir el registro sin perder el formulario

**Contexto.** Una persona anónima está en `/registro`. El formulario solo ofrece paciente o médico. `RegistrationRules` rechaza administrador y agendador en el alta pública.

**Estímulo.** Envía el formulario con un teléfono que no cumple `3` y nueve dígitos más, o con una clave de menos de 8 caracteres, sin letra o sin número.

**Respuesta.** `validators.ts` escribe el mensaje en el campo y la página no trata el envío como exitoso. Si el navegador deja pasar el dato y el backend lo rechaza, `GlobalExceptionHandler` responde 400 con `fields`, y la pantalla muestra ese mapa por campo. Un usuario, correo o documento repetido responde 409.

**Medición de calidad.** El mensaje nombra la regla incumplida (longitud, formato o duplicado) en el control correspondiente. La persona no navega a otra ruta para entender el error. Las reglas del cliente y las de `RegistrationRules` coinciden en teléfono, clave, documento, correo y nombre.

**Resultado esperado.** Un teléfono `3001234567` y una clave con letra y número superan esa validación. Un teléfono con espacios o que no empieza por 3 se queda en el formulario, con el texto de error visible.

## Seguridad

Hoy hay sesión y roles. No hay un módulo de seguridad a medias que haya que marcar entero como pendiente. Lo pendiente es el alcance: los tokens no son JWT, no viajan a la base, y no existe autorización para HE-02 ni HE-03 porque esas funciones no existen.

### Escenario S1. Llamar a la agenda sin sesión

**Contexto.** El cliente no envía `Authorization`, o envía un token que `AuthTokenService` no tiene o que ya superó las 8 horas. Las rutas públicas son solo `POST /api/v1/auth/login` y `POST /api/v1/auth/register`, además de Swagger.

**Estímulo.** `GET /api/v1/doctors` o `GET /api/v1/appointments/doctor/1?date=2026-09-29` sin `Bearer` válido.

**Respuesta.** `AuthFilter` corta la cadena. Sin encabezado responde 401 y cuerpo `UNAUTHORIZED`. Con token vencido o desconocido responde 401 y cuerpo `SESSION_EXPIRED`. No se ejecuta el caso de uso. En el navegador, `authInterceptor` borra `sessionStorage` y abre `/login?reason=sesion`.

**Medición de calidad.** La respuesta es 401, el cuerpo trae `code` y `message`, y el JSON no incluye citas ni médicos. `LoginUseCaseTest` y `AuthTokenServiceTest` cubren credenciales y el ciclo del token en memoria.

**Resultado esperado.** Sin sesión no se lee la agenda. Tras reiniciar la JVM el mapa de tokens queda vacío, así que un `Bearer` anterior también recibe 401.

**Qué cubre el código.** El filtro está activo sobre `/api/`. Las contraseñas nuevas y las de demostración se guardan con `BCryptPasswordEncoder`. `LoginUseCase` no distingue «usuario inexistente» de «clave incorrecta»: ambos lanzan `INVALID_CREDENTIALS`. Un usuario con `active = false` recibe 403 `USER_INACTIVE`.

**Qué falta.** El token es un UUID en un `ConcurrentHashMap`. No está firmado, no se revoca en el servidor al pulsar «Salir» (el cierre solo borra `sessionStorage`) y no sobrevive a un segundo proceso. No hay límite de intentos de login. CORS refleja cualquier encabezado, aunque el origen permitido es solo `http://localhost:4200`. No hay TLS en `application.properties`. `LoginUseCase` todavía acepta una clave guardada en texto plano y, si coincide, la reemplaza por BCrypt; ese camino no debería usarse con datos reales.

### Escenario S2. Un paciente intenta ver citas de otras personas

**Contexto.** El paciente inició sesión. Su token lleva el rol `PATIENT`. La única pantalla autenticada de ese rol es `/paciente`, que muestra los datos de la propia sesión y declara que desde ahí no se consultan agendas de otros.

**Estímulo.** Escribe `/agendador` o `/medico`, o llama a `GET /api/v1/appointments/doctor/{id}` con su `Bearer`.

**Respuesta.** `roleGuard` no incluye `PATIENT` en esas rutas y redirige a `/acceso-denegado`. Si la petición llega a la API, `AuthFilter.isAllowed` exige `ADMINISTRATOR`, `SCHEDULER` o `DOCTOR` para `/api/v1/appointments` y `/api/v1/doctors`, y responde 403 `FORBIDDEN`.

**Medición de calidad.** La ruta del navegador termina en `/acceso-denegado`. La API responde 403 y no devuelve la lista. Un médico, un administrador o un agendador con el mismo endpoint reciben 200.

**Resultado esperado.** El paciente ve su perfil y no la tabla de citas. El control ocurre en el navegador y otra vez en el servidor; ocultar el menú no es la única barrera.

**Qué cubre el código.** Los cuatro roles existen en `Role` y en el tipo `UserRole` del front. El registro público no puede crear `ADMINISTRATOR` ni `SCHEDULER`. El médico de demostración entra a `/medico` y reutiliza la misma pantalla de consulta, no la de paciente.

**Qué falta.** No hay dueño de la cita en la consulta: cualquier rol de staff ve todas las citas del médico y la fecha, no solo las suyas. No hay HE-02, así que no hay una regla de «un paciente solo crea citas para sí mismo». No hay HE-03, así que no hay una regla de «solo el administrador cambia la agenda del médico». El rol `SCHEDULER` está autorizado en filtro y guard, pero nadie puede registrarlo y la semilla no lo crea: en la práctica hay que insertarlo fuera de la aplicación. Swagger queda fuera del filtro, así que la documentación de la API es pública en ese proceso.
