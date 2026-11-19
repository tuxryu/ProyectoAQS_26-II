# Documentación del Proyecto (`docs/`)

Repositorio de documentación técnica y arquitectónica del **Sistema Intermediario de Pólizas de Seguro** (`mx.edu.uacm.is.slt.as.IDSISTEMA`) — Equipo **404 Equipo no Encontrado**.

## Índice de Documentos

| Archivo / Carpeta | Descripción |
|---|---|
| [`ERS_Sistema_Intermediario_Polizas.pdf`](ERS_Sistema_Intermediario_Polizas.pdf) | **Especificación de Requerimientos de Software (ERS v1.0.0)**: requisitos funcionales, de interfaz, de calidad en uso, técnicos y de usabilidad, con responsables asignados, glosario e historial de cambios (incluyendo los nuevos endpoints `GET /clientes` y `GET /beneficiarios`). |
| [`catalogo-endpoints-y-hallazgos.md`](catalogo-endpoints-y-hallazgos.md) | **Catálogo de los 17 Endpoints y 24 Hallazgos del Sistema Dueño (`http://nachintoch.mx:8080`)**: especificación detallada de las rutas remotas (`11 GET, 3 POST, 2 PUT, 1 DELETE`) y tabla de hallazgos experimentales con sus consecuencias de diseño. |
| [`hallazgos-sistema-dueno.pdf`](hallazgos-sistema-dueno.pdf) | Documento original en PDF con los 24 hallazgos del comportamiento del servidor remoto. |
| [`arquitectura4.puml`](arquitectura4.puml) / [`arquitectura4.png`](arquitectura4.png) | **Vista de Arquitectura Limpia (Monolito Modular)**: fuente PlantUML y diagrama renderizado con las 4 capas, inversión de dependencias y comunicación hacia los 17 endpoints del Sistema Dueño. |
| [`revision-practica2.md`](revision-practica2.md) | **Guía Técnica y Especificación de Diseño**: estructura de paquetes, reglas por caso de uso, mapa de códigos HTTP (`200`, `201`, `204`, `400`, `404`, `409`, `422`, `502`, `503`) y contratos de puertos. |
| [`casos-de-uso/`](casos-de-uso/) | **Especificación detallada de los Casos de Uso (11 documentos `.docx`)** listados a continuación. |

---

## Documentos de Casos de Uso (`docs/casos-de-uso/`)

### Casos de Uso Normales (`NRM`) y Principal (`PRN`)
1. [`PRN-GP_gestionar-poliza.docx`](casos-de-uso/PRN-GP_gestionar-poliza.docx) — Caso de uso principal: Gestión de Pólizas.
2. [`NRM-AC_alta-cliente.docx`](casos-de-uso/NRM-AC_alta-cliente.docx) — Alta de Cliente asegurado.
3. [`NRM-MC_modificacion-cliente.docx`](casos-de-uso/NRM-MC_modificacion-cliente.docx) — Modificación de Cliente asegurado.
4. [`NRM-AP_alta-poliza.docx`](casos-de-uso/NRM-AP_alta-poliza.docx) — Alta de Póliza de seguro.
5. [`NRM-MP_modificacion-poliza.docx`](casos-de-uso/NRM-MP_modificacion-poliza.docx) — Modificación de Póliza de seguro.
6. [`NRM-BP_baja-poliza.docx`](casos-de-uso/NRM-BP_baja-poliza.docx) — Baja de Póliza de seguro (con eliminación en cascada de beneficiarios).
7. [`NRM-CP_consulta-poliza.docx`](casos-de-uso/NRM-CP_consulta-poliza.docx) — Consulta de Pólizas con filtros y paginación.
8. [`NRM-AB_alta-beneficiario.docx`](casos-de-uso/NRM-AB_alta-beneficiario.docx) — Alta de Beneficiario (validando tope del `100%` por póliza mediante `GET /beneficiarios`).
9. [`NRM-CB_consulta-beneficiario.docx`](casos-de-uso/NRM-CB_consulta-beneficiario.docx) — Consulta de Beneficiarios.

### Cursos Alternativos / Excepciones (`EX`)
10. [`EX-CNR_cliente-noreg.docx`](casos-de-uso/EX-CNR_cliente-noreg.docx) — Excepción: Cliente no registrado.
11. [`EX-DND_sistemadueno-nodisponible.docx`](casos-de-uso/EX-DND_sistemadueno-nodisponible.docx) — Excepción: Sistema Dueño no disponible.
