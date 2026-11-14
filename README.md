# Sistema Intermediario de Pólizas de Seguro (`mx.edu.uacm.is.slt.as.IDSISTEMA`)

Proyecto del curso de **Arquitectura de Software (2026-II)** — Licenciatura en Ingeniería de Software, **UACM San Lorenzo Tezonco**.

- **Equipo:** `404 Equipo no Encontrado`
- **Identificador del Producto:** `mx.edu.uacm.is.slt.as.IDSISTEMA`
- **Versión:** `1.0.0` (Proyecto Final)

---

## 1. Integrantes del Equipo

| Integrante | Matrícula |
|---|---|
| Jessica Medellin Magdaleno | 21-003-0787 |
| Fátima Daniela Jurado Laurrabaquio | 20-003-1007 |
| Angel De Jesus Vicencio | 17-003-0844 |
| Oscar Alan Cruz Lopez | 16-003-0645 |
| Rafael Garcia Mauricio | 16-002-0234 |
| Angel Granados Perez | 16-003-1630 |
| Hernandez Parada Mauricio | 14-003-1623 |
| Luis Enrique Vargas Loza | 14-003-1419 |

---

## 2. Arquitectura del Sistema

El sistema está diseñado como un **Monolito Modular con Arquitectura Limpia** sobre **Java 25** y **Spring Boot 4.1.1**, estructurado en tres contextos delimitados (`clientes`, `polizas`, `beneficiarios`) y un módulo transversal (`shared`):

- **Capa 1 (`domain`)**: Entidades de dominio con anotaciones Hibernate JPA (`Cliente`, `Poliza`, `Beneficiario`, `BeneficiarioId`) y excepciones de negocio, libres de dependencias hacia frameworks web o de infraestructura.
- **Capa 2 (`application`)**: Casos de uso (`port/in`), puertos de salida (`port/out`) y servicios de aplicación (`ClienteService`, `PolizaService`, `BeneficiarioService`).
- **Capa 3 (`adapter`)**:
  - **Entrada (`adapter/in/web`)**: Controladores REST (`ClienteController`, `PolizaController`, `BeneficiarioController`) y manejo centralizado de errores (`GlobalExceptionHandler`).
  - **Salida (`adapter/out/rest`)**: Adaptadores HTTP REST hacia los **17 endpoints** del Sistema Dueño (`http://nachintoch.mx:8080`, incluyendo los nuevos puntos de acceso `GET /clientes` y `GET /beneficiarios`) y adaptadores inter-módulo en memoria (`ClienteRestAdapter`, `PolizaRestAdapter`).
- **Capa 4 (`ui/web` y `config`)**: Interfaz de usuario Web renderizada en servidor con **Thymeleaf** (`HomeViewController` y vistas por módulo) y configuración de infraestructura (`HttpConfig`).

Las reglas de dependencia entre capas se verifican automáticamente mediante **ArchUnit** en `ArquitecturaLimpiaTest` y el estilo de código mediante **Checkstyle** (`config/checkstyle/checkstyle.xml`).

---

## 3. Requisitos e Instalación

### Requisitos Previos
- **Java SDK 25** instalado y configurado en `JAVA_HOME`.
- Conectividad de red hacia el Sistema Dueño (`http://nachintoch.mx:8080`) para operaciones en vivo.
- No requiere instalar Maven manualmente (incluye **Maven Wrapper** `./mvnw` y `mvnw.cmd`).

### Compilación y Verificación de Calidad (Tests + ArchUnit + Checkstyle)

En Linux / macOS / Git Bash:
```bash
./mvnw test checkstyle:check
```

En Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd test checkstyle:check
```

### Generación del Ejecutable (`.jar`) y Ejecución

1. Empaquetar la aplicación:
   ```bash
   ./mvnw clean package
   ```
2. Ejecutar la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```
   O bien ejecutando directamente el archivo `.jar` generado en `target/`:
   ```bash
   java -jar target/IDSISTEMA-0.0.1-SNAPSHOT.jar
   ```
3. Abrir en el navegador:
   - **Interfaz Web (Thymeleaf):** `http://localhost:8080/`
   - **API REST Propia:** `/clientes`, `/polizas`, `/polizas/{clave}/beneficiarios`

---

## 4. Documentación Arquitectónica y ERS (`docs/`)

Toda la documentación del proyecto se encuentra en el directorio [`docs/`](docs/README.md):

- [`docs/ERS_Sistema_Intermediario_Polizas.pdf`](docs/ERS_Sistema_Intermediario_Polizas.pdf) — Especificación de Requerimientos de Software (ERS).
- [`docs/catalogo-endpoints-y-hallazgos.md`](docs/catalogo-endpoints-y-hallazgos.md) — Catálogo de los **17 endpoints** (`11 GET, 3 POST, 2 PUT, 1 DELETE`) y **24 hallazgos** del Sistema Dueño.
- [`docs/hallazgos-sistema-dueno.pdf`](docs/hallazgos-sistema-dueno.pdf) — Evidencia en PDF de las pruebas al servidor remoto.
- [`docs/casos-de-uso/`](docs/casos-de-uso/) — Los 11 documentos `.docx` de especificación de casos de uso (`NRM`, `PRN` y `EX`).
- [`docs/arquitectura4.puml`](docs/arquitectura4.puml) / [`docs/arquitectura4.png`](docs/arquitectura4.png) — Diagrama de Arquitectura Limpia.
- [`docs/revision-practica2.md`](docs/revision-practica2.md) — Guía técnica y especificación de diseño.
