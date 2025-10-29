# Electricista Cat III

Solución integral para diseñadores eléctricos categoría 3 en Córdoba, Argentina. Incluye backend en Java 21 (Spring Boot 3) y frontend en React + Vite. Permite gestionar proyectos, tableros, cálculos (IA/IN/IZ/ΔV), materiales, puesta a tierra y generación de reportes alineados a normas AEA/IRAM/IEC.

## Estructura

```
backend/   # API REST Spring Boot, Flyway, JPA, MapStruct
frontend/  # React + Tailwind + Zustand + React Hook Form + i18n
```

## Requisitos

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker 24+ (opcional para despliegue)

## Configuración de datos normativos

El backend carga tablas desde `backend/src/main/resources/standards/standards.json`. Sustituya o amplíe este archivo con datasets oficiales (ej.: AEA 90364/7, IRAM 2183, IEC 60364). Para mantener trazabilidad, agregue nuevos códigos de perfil y referencias correspondientes.

## Variables de entorno

Copie `.env.example` a `.env` y ajuste según su entorno:

```
APP_DB_HOST=localhost
APP_DB_PORT=5432
APP_DB_NAME=electricista
APP_DB_USER=postgres
APP_DB_PASSWORD=postgres
APP_ALLOWED_ORIGINS=http://localhost:5173
APP_PORT=8080
VITE_API_URL=http://localhost:8080/api
VITE_PORT=5173
```

## Backend

```bash
cd backend
mvn clean package
java -jar target/electricista-backend-0.0.1-SNAPSHOT.jar
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

Health checks: `/actuator/health`

### Migraciones y seed

Flyway crea las tablas en Postgres (`db/migration/V1__init.sql`). `CalcProfileInitializer` sincroniza perfiles a partir del dataset JSON.

### Pruebas

Las pruebas de integración (`ProjectWorkflowIT`) usan Testcontainers (PostgreSQL). Ejecute `mvn test`. Si el entorno restringe acceso a Maven Central, configure un mirror corporativo antes de ejecutar.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Aplicación disponible en `http://localhost:5173`.

### Scripts

- `npm run dev` – servidor Vite con hot reload
- `npm run build` – build de producción
- `npm run preview` – vista previa del build
- `npm run lint` / `npm run format`

## Docker Compose

```
docker compose up --build
```

Servicios:
- `db` – PostgreSQL 15
- `backend` – API Spring Boot
- `frontend` – Nginx sirviendo build de Vite

## Colección de estándares y reportes

- API `/api/standards/profiles` y `/api/standards/templates` exponen perfiles configurados.
- `/api/projects/{id}/report` genera PDF del expediente técnico (incluye cálculos, checklist, BOM, mediciones).
- `/api/projects/{id}/single-line` entrega SVG unifilar simplificado.

## Flujo sugerido

1. Crear proyecto (`POST /api/projects`).
2. Añadir paneles (`POST /api/projects/{id}/panels`).
3. Cargar circuitos con datos de cálculo (`POST /api/panels/{id}/circuits`).
4. Registrar materiales, checklist y mediciones de puesta a tierra.
5. Generar reporte técnico y exportar BOM.

## Extensiones futuras

- Incorporar autenticación (Keycloak/Spring Security).
- Plantillas adicionales (motores, bombas) agregando entradas en `standards.json`.
- Integrar validación contra límites específicos de AEA/IRAM según ambiente (temperatura, agrupamientos personalizados).

## Nota sobre dependencias externas

El proyecto requiere acceso a Maven Central y npm registry durante la fase de build. Si trabaja en un entorno sin salida a Internet, configure repositorios proxy (Nexus/Artifactory) para evitar errores `403 Forbidden` al resolver artefactos.
