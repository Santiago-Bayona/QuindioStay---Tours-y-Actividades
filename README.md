# QuindíoStay — Tours y Actividades

Proyecto final de **Bases de Datos I** (grupo 02D) · Universidad del Quindío · 2026-2

Módulo de **Tours y actividades locales** de la plataforma QuindíoStay: publicación de tours, programación de salidas con guía, idioma y cupo, reservas con participantes, pagos y reembolsos, notificaciones a los clientes, 10 reportes exportables a PDF y documentos del negocio (comprobantes, factura y autorizaciones).

**Integrantes**

| Nombre  |
|---|---|
| Cesar Santiago Bayona Peñuela  |
| Sofia Santamaria Rendon  |

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Base de datos | MySQL 8.4 en Docker |
| Backend | Java 25, Spring Boot 4.1.2 (Web MVC, Data JPA, Validation), Hibernate 7, Lombok, BCrypt, OpenPDF 2.0.3 |
| Frontend | React 19, React Router 7, Vite 8 |

## Estructura

```
QuindioStay-TyA/
├── Backend/            API REST en Spring Boot (controller, service, repository, domain, dto, exception, config)
├── frontend/           Aplicación web en React
├── database/           Scripts SQL (se ejecutan en orden)
├── herramientas/       Generador de los datos de ejemplo
├── docker-compose.yml  MySQL 8.4 con los scripts de database/
└── .env.example        Variables de entorno necesarias
```

## Base de datos

| Archivo | Contenido | Orden |
|---|---|---|
| `01_DDL.sql` | Crea la base `tours_actividades`: 18 tablas, PK, FK, UNIQUE, CHECK e índices | 1 |
| `02_DML.sql` | Datos de ejemplo de 2025 y 2026 (150 clientes, 520 reservas, 708 pagos, 1.947 notificaciones) | 2 |
| `03_Reportes.sql` | Las 10 consultas de los reportes | 3 |
| `04_Verificacion.sql` | Conteo de registros y verificación de 19 reglas de negocio (todas deben dar 0) | 4 (opcional) |

Los datos de `02_DML.sql` los genera `herramientas/generar_datos.py` con una semilla fija, así que siempre salen iguales. Para regenerarlos: `pip install bcrypt` y `python generar_datos.py`.

---

## Instalación y ejecución

### Requisitos

- Docker Desktop
- JDK 25
- Node.js 20 o superior

### 1. Base de datos

En la raíz del proyecto, crea un archivo `.env` (puedes copiar `.env.example`) con:

```
DB_PASSWORD=tu_clave
```

Luego:

```bash
docker compose up -d
```

Docker crea MySQL en `localhost:3307` y ejecuta los scripts de `database/` en orden. Para recrear la base desde cero (por ejemplo, si cambian los scripts):

```bash
docker compose down -v
docker compose up -d
```

### 2. Backend

Define la variable de entorno `DB_PASSWORD` con la misma clave del `.env` (en IntelliJ: *Run › Edit Configurations › Environment variables*) y ejecuta `QuindioStayTyAApplication`, o desde `Backend/`:

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8081`. Hibernate solo **valida** el esquema (`ddl-auto=validate`); las tablas las crea el script DDL.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

La aplicación queda en `http://localhost:5173`. Vite redirige `/api` al backend.

> Después de cada `git pull`, si cambió `package.json`, vuelve a ejecutar `npm install`.

---

## Usuarios de prueba

Todos usan la contraseña **`Clave123*`**.

| Rol | Usuario |
|---|---|
| Administrador | `admin` |
| Encargado de prestador | `cocora.encargado` |
| Operador | `operador1` |
| Cliente | `valentina.rojas@gmail.com` |

`mirador.encargado` y `pijao.encargado` están bloqueados, para probar que el login los rechaza.

## Roles y permisos

El inicio de sesión valida usuario y contraseña (BCrypt) contra la base. El frontend envía el id del usuario en el header `X-Usuario-Id` y el backend comprueba el rol en cada operación (`AutorizacionService`): responde **401** si no hay sesión y **403** si el rol no tiene permiso.

| Operación | Admin | Encargado | Operador |
|---|---|---|---|
| Catálogos y usuarios del sistema | ✓ | — | — |
| Prestadores, planes y tours | Todos | Solo los de su prestador | — |
| Programar, cancelar y cerrar salidas | Todas | Solo las de su prestador | Todas |
| Guías, reservas, pagos, notificaciones y reportes | ✓ | ✓ | ✓ |

## Reportes

Se consultan en *Panel › Reportes* y se exportan a PDF (`GET /api/reportes/{n}/pdf`).

| N.º | Reporte | Nivel |
|---|---|---|
| 1 | Tours ofertados | Simple |
| 2 | Participación mensual por tour | Intermedio |
| 3 | Tours con más cancelaciones | Intermedio |
| 4 | Guías registrados | Simple |
| 5 | Guías con más salidas en un periodo | Intermedio |
| 6 | Guías por encima del promedio | Avanzado |
| 7 | Salidas programadas | Simple |
| 8 | Ocupación por categoría | Avanzado |
| 9 | Tours sin participantes | Avanzado |
| 10 | Ingresos por categoría y método de pago | Intermedio |

## Documentos en PDF

Comprobante de reserva, comprobante de pago o reembolso, factura de venta, autorización para menores de edad y autorización de tratamiento de datos personales (`/api/documentos/...`).
