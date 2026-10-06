# Mapeo inicial de 15 endpoints del Sistema Dueño

Base URL: `http://nachintoch.mx:8080`

> Documento inicial para consolidar integración. Completar/ajustar con el contrato real (OpenAPI/Swagger o evidencia de respuestas).

| # | Método | Endpoint | Parámetros principales | Respuesta esperada |
|---|--------|----------|------------------------|--------------------|
| 1 | GET | `/clientes` | `page`, `size` | Lista paginada de clientes |
| 2 | GET | `/clientes/{curp}` | `curp` | Cliente por CURP |
| 3 | POST | `/clientes` | Body cliente | Cliente creado |
| 4 | PUT | `/clientes/{curp}` | `curp`, Body cliente | Cliente actualizado |
| 5 | DELETE | `/clientes/{curp}` | `curp` | Confirmación de baja |
| 6 | GET | `/polizas` | `page`, `size`, `clienteCurp` | Lista de pólizas |
| 7 | GET | `/polizas/{id}` | `id` | Póliza por UUID |
| 8 | POST | `/polizas` | Body póliza | Póliza creada |
| 9 | PUT | `/polizas/{id}` | `id`, Body póliza | Póliza actualizada |
| 10 | DELETE | `/polizas/{id}` | `id` | Confirmación de cancelación |
| 11 | GET | `/beneficiarios` | `clavePoliza` | Beneficiarios por póliza |
| 12 | GET | `/beneficiarios/{clavePoliza}/{curp}` | `clavePoliza`, `curp` | Beneficiario específico |
| 13 | POST | `/beneficiarios` | Body beneficiario | Beneficiario creado |
| 14 | PUT | `/beneficiarios/{clavePoliza}/{curp}` | `clavePoliza`, `curp`, Body | Beneficiario actualizado |
| 15 | DELETE | `/beneficiarios/{clavePoliza}/{curp}` | `clavePoliza`, `curp` | Confirmación de eliminación |

## Pendientes de verificación

- Confirmar nombres reales de rutas.
- Confirmar estructura JSON de request/response y códigos HTTP exactos.
- Registrar ejemplos reales de respuesta para cada endpoint.
