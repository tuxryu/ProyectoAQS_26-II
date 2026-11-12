# Catálogo de los 17 Endpoints y Hallazgos del Sistema Dueño (`http://nachintoch.mx:8080`)

Documento técnico de referencia del equipo **404 Equipo no Encontrado** que concentra el catálogo completo de los **17 puntos de acceso** (`11 GET, 3 POST, 2 PUT, 1 DELETE`) expuestos por el Sistema Dueño de las Pólizas (`http://nachintoch.mx:8080`) y los **24 hallazgos experimentales** obtenidos durante las pruebas de integración.

---

## 1. Catálogo de los 17 Endpoints del Sistema Dueño

> **Nota de actualización (Entrega Final — v1.0.0):** Al catálogo original de 15 endpoints se incorporaron dos puntos de acceso adicionales de consulta general: **`GET /clientes`** y **`GET /beneficiarios`**, alcanzando un total de **17 endpoints** (`11 GET, 3 POST, 2 PUT, 1 DELETE`).

### 1.1 Módulo de Clientes (6 endpoints: 4 `GET`, 1 `POST`, 1 `PUT`)

| # | Método | Ruta en `http://nachintoch.mx:8080` | Descripción | Respuesta / Observaciones |
|---|---|---|---|---|
| 1 | `GET` | `/cliente/{curp}` | Consulta un cliente por su CURP exacta (sensible a mayúsculas/minúsculas). | `200 OK` con JSON `ClienteRemotoDto` o `200` con cuerpo vacío si no existe. |
| 2 | `GET` | `/clientes` | **(Nuevo)** Lista todos los clientes registrados en el Sistema Dueño. | `200 OK` con arreglo JSON `[...]`. Puede incluir registros corruptos de otros equipos (se filtran fila por fila). |
| 3 | `GET` | `/clientes/{nombres}/{primerApellido}` | Busca clientes por nombre y primer apellido. | `200 OK` con arreglo JSON `[...]`. |
| 4 | `GET` | `/clientes/{nombres}/{primerApellido}/{segundoApellido}` | Busca clientes por nombre, primer apellido y segundo apellido. | `200 OK` con arreglo JSON `[...]`. |
| 5 | `POST` | `/cliente/{curp}/{direccion}/{fecha}/{nombres}/{primerApellido}[/{segundoApellido}]` | Registra o sobrescribe (*upsert*) un cliente. | `200 OK` con JSON del cliente o `200` vacío si es rechazado. También se usa para actualizar cuando no hay segundo apellido (Hallazgo 8). |
| 6 | `PUT` | `/cliente/{curp}/{direccion}/{fecha}/{nombres}/{primerApellido}/{segundoApellido}` | Actualiza un cliente existente con segundo apellido. | `200 OK` con JSON del cliente. Falla si se omite el segundo apellido (Hallazgo 8). |

### 1.2 Módulo de Pólizas (8 endpoints: 5 `GET`, 1 `POST`, 1 `PUT`, 1 `DELETE`)

| # | Método | Ruta en `http://nachintoch.mx:8080` | Descripción | Respuesta / Observaciones |
|---|---|---|---|---|
| 7 | `GET` | `/poliza/{clave}` | Consulta una póliza por su clave `UUID`. | `200 OK` con JSON `PolizaRemotaDto` o `200` con cuerpo vacío si no existe. |
| 8 | `GET` | `/polizas` | Lista todas las pólizas registradas en el Sistema Dueño. | `200 OK` con arreglo JSON `[...]` (`[]` si no hay resultados). |
| 9 | `GET` | `/polizas/{curp}` | Lista las pólizas asociadas a la CURP indicada (distingue mayúsculas/minúsculas). | `200 OK` con arreglo JSON `[...]`. |
| 10 | `GET` | `/polizas/{tipo}` | Lista las pólizas por tipo (`0` = Auto, `1` = Vida, `2` = Médico). | `200 OK` con arreglo JSON `[...]`. |
| 11 | `GET` | `/polizas/c/{nombres}/{primerApellido}[/{segundoApellido}]` | Lista las pólizas cuyo cliente titular coincide con el nombre indicado. | `200 OK` con arreglo JSON `[...]`. |
| 12 | `GET` | `/polizas/b/{nombres}/{primerApellido}[/{segundoApellido}]` | Lista las pólizas que tienen un beneficiario con el nombre indicado. | `200 OK` con arreglo JSON `[...]`. |
| 13 | `POST` | `/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}` | Crea o sobrescribe (*upsert*) una póliza. | `200 OK` con JSON o `200` vacío / `500` si el titular no existe o el monto excede capacidad. |
| 14 | `PUT` | `/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curpCliente}` | Actualiza una póliza existente. | `200 OK` con JSON, `200` vacío si la póliza no existe, o `500` si el titular no existe. |
| 15 | `DELETE` | `/poliza/{clave}` | Elimina físicamente una póliza y borra en cascada a sus beneficiarios. | `200 OK` vacío incluso si la póliza no existía (Hallazgo 11). |

### 1.3 Módulo de Beneficiarios (3 endpoints: 2 `GET`, 1 `POST`)

| # | Método | Ruta en `http://nachintoch.mx:8080` | Descripción | Respuesta / Observaciones |
|---|---|---|---|---|
| 16 | `GET` | `/beneficiario/{fecha}/{clavePoliza}/{nombres}/{primerApellido}[/{segundoApellido}]` | Consulta un beneficiario específico por su llave compuesta completa. | `200 OK` con JSON anidado `{"id":{...},"porcentaje":...}` o `200` vacío si no existe. |
| 17 | `GET` | `/beneficiarios` | **(Nuevo)** Lista todos los beneficiarios registrados en el Sistema Dueño. | `200 OK` con arreglo JSON `[...]`. Permite filtrar por `clavePoliza` y validar que la suma de porcentajes no exceda el `100%`. |
| 18* | `POST` | `/beneficiario/{fecha}/{clavePoliza}/{nombres}/{primerApellido}[/{segundoApellido}]/{porcentaje}` | Registra o sobrescribe (*upsert*) un beneficiario en una póliza. | `200 OK` con JSON anidado o `500` si la póliza no existe. |

*(Nota: Las variantes con y sin `{segundoApellido}` corresponden al mismo punto de acceso lógico, sumando los **17 endpoints** oficiales: 11 de lectura `GET`, 3 de alta `POST`, 2 de actualización `PUT` y 1 de borrado `DELETE`).*

---

## 2. Tabla de Hallazgos del Comportamiento del Servidor (Pruebas Experimentales)

El archivo original en PDF también se encuentra disponible en [`docs/hallazgos-sistema-dueno.pdf`](hallazgos-sistema-dueno.pdf).

| # | Hallazgo (prueba) | Consecuencia en nuestra Arquitectura Limpia |
|---|---|---|
| **1** | Los JSON usan `camelCase` (`primerApellido`, `curpCliente`, `clavePoliza`), no `snake_case` (A2, B1). | Los DTOs remotos (`*RemotoDto`) mapean propiedades en `camelCase`. |
| **2** | Las fechas de cliente y beneficiario vienen como timestamp ISO-8601: `1990-01-01T06:00:00.000+00:00` (A2, D7). En verano de 2003 llegó `T05:00`, así que el servidor guarda medianoche de la Ciudad de México. | `FechasSistemaDueno` convierte timestamps UTC a `LocalDate` en la zona `America/Mexico_City` (soportando horario de verano y desfase histórico de 1920). |
| **3** | *"No existe"* es `200` con cuerpo vacío, nunca `404` (A1, B3, D3). Las listas vacías son `[]` (C3, C4, C10). | En lecturas individuales, cuerpo vacío se traduce a `Optional.empty()` → `404 Not Found`. Un `404` remoto real indicaría ruta equivocada. |
| **4** | `POST` es *upsert*: repetir la CURP, la clave de póliza o el beneficiario sobrescribe en silencio (A4, B4, D9). | Antes de crear, cada caso de uso verifica que el registro no exista previamente (`409 Conflict`). |
| **5** | Un rechazo del servidor en escritura es `200` con cuerpo vacío: póliza con cliente inexistente (B6) o fecha inválida (A11). | En escrituras (`POST`/`PUT`), recibir `200` con cuerpo vacío lanza `SistemaDuenoRechazoException` (`502 Bad Gateway`). |
| **6** | El servidor casi no valida: acepta CURP de 9 caracteres (`T404CORTA`, A10), `tipo` 7 (B8), monto negativo (B10), monto con 3 decimales (B9), porcentajes `0` y `101` (D7, D8) y suma de `140%` (D6). | Todas las invariantes de negocio se validan estrictamente en nuestras entidades de dominio (Capa 1) y servicios de aplicación (Capa 2). |
| **7** | El `POST` devuelve lo enviado, no necesariamente lo almacenado: respondió `100.555` y el listado mostró `100.56` (B9 contra C1). | Toda operación de escritura realiza una relectura inmediata con `GET` para confirmar la consistencia de los datos persistidos. |
| **8** | `PUT /cliente` falla cuando el cliente no tiene segundo apellido. | `ClienteSistemaDuenoAdapter.actualizar(...)` utiliza `PUT` cuando hay segundo apellido y `POST` cuando el segundo apellido es `null` (que sí limpia el campo). |
| **9** | `PUT /poliza` sí funciona (B16). Si la póliza no existe responde `200` vacío y no la crea (B18, B19). | Se verifica la existencia previa de la póliza (`404`) antes de enviar la actualización. |
| **10** | Hay respuestas HTTP `500` por datos inválidos: monto de más de 9 dígitos (B11) y beneficiario sobre póliza inexistente (D11). | `TraductorExcepcionesSistemaDueno` distingue `500` → `SistemaDuenoRechazoException` (`502`) de `502`/`503`/`504`/timeout/red → `SistemaDuenoNoDisponibleException` (`503`). |
| **11** | `DELETE /poliza/{clave}` devuelve `200` aunque no exista (E4) y borra en cascada a los beneficiarios (E1 a E3). | Se exige `?confirmar=true` (`400`), se verifica antes que la póliza exista (`404`) y se comprueba con `GET` después del `DELETE` que ya no esté registrada. |
| **12** | Una diagonal `/` en un dato (`%2F`) produce `400` en HTML de Tomcat (B12). Acentos y `ñ` funcionan si van codificados en UTF-8 (A6, B13). | Se prohíben `/` y `\` en todos los textos que viajan como segmentos de ruta antes de invocar al servidor. |
| **13** | Omitir el segundo apellido funciona. En clientes llega `null` y en beneficiarios llega `""`. Enviar la palabra `"null"` guarda el texto literal `"null"` (A8). | Nunca se envía `"null"` en la ruta; se usa la variante de ruta sin el segmento final y se normaliza `""` o `"   "` a `null`. |
| **14** | El beneficiario viene anidado en JSON: `{"id":{...},"porcentaje":60}` (D1). | `BeneficiarioRemotoDto` utiliza el registro anidado `BeneficiarioIdRemotoDto`. |
| **15** | Hay datos sembrados con CURP en minúsculas (`broe550101hdfabc00`) y descripciones como `[CONFIDENCIAL]`. | En lecturas y validaciones de existencia se aceptan CURP en minúsculas; en altas nuevas se exige formato válido de 18 caracteres. |
| **16** | Un registro válido puede convivir con filas basura de otros equipos (como `tipo=7` o `T404CORTA`). | Al listar clientes, pólizas o beneficiarios (`GET /clientes`, `GET /polizas`, `GET /beneficiarios`), cada fila se convierte en un bloque `try/catch`, omitiendo y registrando en `LOG.warn` las filas corruptas sin abortar la consulta. |
| **17** | *(Actualizado en Entrega Final)* Inicialmente no se conocía endpoint para listar beneficiarios de una póliza (D12); posteriormente se habilitaron **`GET /beneficiarios`** y **`GET /clientes`**. | Con `GET /beneficiarios` se listan los beneficiarios filtrando por `clavePoliza` y se valida en `BeneficiarioService` que la suma de porcentajes no rebase el `100%`. Con `GET /clientes` se ofrece listado paginado de clientes. |
| **18** | Tiempos de respuesta: normalmente de `0.16` a `0.2 s`, con picos de `1` a `2.07 s`. | El timeout de lectura configurado en `application.yml` (`5 s`) cubre con holgura los picos observados. |
| **19** | `GET /cliente/{curp}` y `GET /polizas/{curp}` distinguen mayúsculas y minúsculas. | Las consultas envían la CURP tal cual sin normalizarla a mayúsculas. |
| **20** | `PUT /poliza` con titular inexistente responde `500` y no modifica nada; el `POST` equivalente responde `200` vacío. | El adaptador y el servicio validan previamente al titular (`ClienteServicioPort`) y manejan ambas formas de rechazo. |
| **21** | Los caracteres `%` y `?` en la descripción viajan correctamente si se codifican como segmentos de URI. | `RestClient` codifica las variables de plantilla URI automáticamente. |
| **22** | Las búsquedas por nombre son exactas, sensibles a mayúsculas, y el segundo apellido forma parte de la coincidencia. | La interfaz solicita el segundo apellido de manera opcional cuando se conozca. |
| **23** | `POST /poliza` con monto `9999999.99` devuelve `500`. El límite real de capacidad es menor al supuesto inicialmente. | Evidencia empírica de que las validaciones de capacidad y errores `500` deben probarse y manejarse en el adaptador. |
| **24** | `GET /beneficiario` y `GET /polizas/b/...` exigen coincidencia exacta del segundo apellido cuando el registro fue creado con segundo apellido. | Se respeta la distinción entre rutas con y sin segundo apellido en todos los adaptadores REST. |
