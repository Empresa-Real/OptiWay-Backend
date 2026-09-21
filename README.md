# OptiWay — Backend

Sistema backend para la plataforma **OptiWay**, diseñado para la optimización de distribución y ruteo logístico, gestión de tiendas comerciales, centros de distribución (CD) y control de acceso granular por roles.

Desarrollado bajo los principios de **Arquitectura Hexagonal (Puertos y Adaptadores)** y **Clean Architecture** sobre el ecosistema Spring Boot y PostgreSQL (Supabase).

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.4.3
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** PostgreSQL en la nube (Supabase)
* **Seguridad & Autenticación:** Spring Security 6 (OAuth2 Resource Server con validación JWT de Supabase Auth)
* **Gestor de Dependencias:** Apache Maven (con Maven Wrapper `mvnw`)

---

## 🏛️ Arquitectura Hexagonal

El proyecto organiza sus responsabilidades desacoplando el núcleo de negocio de las tecnologías externas:

```text
src/main/java/com/example/optiway/
├── domain/                  # Lógica pura y reglas de negocio independientes
│   └── model/               # Entidades de dominio (Usuario, Tienda, CentroDistribucion, Rol)
│
├── application/             # Capa de orquestación y casos de uso
│   ├── port/
│   │   ├── in/              # Casos de uso / Interfaces de entrada
│   │   └── out/             # Interfaces de salida (Puertos de persistencia y servicios)
│   └── service/             # Implementación de casos de uso y validaciones de negocio
│
└── infrastructure/          # Adaptadores hacia el mundo exterior
    ├── adapter/
    │   ├── in/rest/         # Controladores REST y DTOs de entrada HTTP
    │   └── out/
    │       ├── auth/        # Adaptadores de autenticación externa (Supabase Admin API)
    │       └── persistence/ # Entidades JPA, repositorios Spring Data y adaptadores DB
    └── config/              # Configuración del framework (Spring Security)
```

Para una explicación detallada, consulta [`docs/Hexagonal-Arquitecture.md`](docs/Hexagonal-Arquitecture.md).

---

## 🚀 Módulos y Endpoints Principales

### 1. Gestión de Usuarios & Autenticación (`HU-01` & `HU-29`)
* `POST /api/usuarios`: Invitación y creación de usuarios (restringido a rol `ADMINISTRADOR`).
* `GET /api/usuarios`: Listado de todos los usuarios registrados.
* `GET /api/usuarios/{id}`: Consulta de usuario por identificador.
* `DELETE /api/usuarios/{id}`: Eliminación de usuario.
* `GET /api/usuarios/me`: Obtiene el perfil y rol del usuario autenticado a partir del JWT.
* **Roles soportados:** `ADMINISTRADOR`, `ENCARGADO_TIENDA`, `ENCARGADO_CD`, `PLANIFICADOR`.

### 2. Tiendas (`HU-07`)
* `POST /api/tiendas`: Registro de nueva tienda (genera automáticamente código `TND-XXXXXXXX`).
* `GET /api/tiendas`: Consulta y listado de todas las tiendas disponibles.

### 3. Centros de Distribución (`HU-09`)
* `POST /api/centros-distribucion`: Registro de nuevo CD (genera código `CD-XXXXXXXX`). Permite asociar la zona de cobertura mediante las tiendas abastecidas (`@ManyToMany`).
* `GET /api/centros-distribucion`: Consulta y listado de todos los CDs con su información de cobertura.

---

## ⚙️ Variables de Entorno Requeridas

Antes de ejecutar la aplicación, define las siguientes variables en tu entorno o en la configuración de ejecución de tu IDE:

| Variable | Descripción | Ejemplo |
| :--- | :--- | :--- |
| `DB_PASSWORD` | Contraseña de conexión a la base de datos PostgreSQL | `mi_password_segura` |
| `SUPABASE_PROJECT_REF` | Identificador del proyecto en Supabase | `cehzzcvnkiuvkabglrzp` |
| `SUPABASE_SERVICE_ROLE_KEY` | Clave secreta administrativa para invitar usuarios | `eyJhbGciOi...` |
| `SUPABASE_INVITE_REDIRECT_URL` | URL de redirección tras aceptar invitación | `http://localhost:8080/set-password.html` |

---

## 💻 Compilación y Ejecución Local

### Requisitos previos
* JDK 17 o superior instalado y configurado en el `PATH`.

### Compilar el proyecto
En Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd clean compile
```

En Linux / macOS:
```bash
./mvnw clean compile
```

### Iniciar el servidor Spring Boot
```powershell
.\mvnw.cmd spring-boot:run
```
La aplicación iniciará por defecto en `http://localhost:8080`.

---

## 🖥️ Pantallas Web de Prueba Estáticas

Para facilitar pruebas manuales de extremo a extremo sin depender del frontend completo, se incluyen vistas HTML estándar (sin librerías pesadas):

* **Login y Sesión:** `http://localhost:8080/` (o `index.html`)
* **Panel de Administración (Usuarios):** `http://localhost:8080/admin.html`
* **Registro y Consulta de Tiendas:** `http://localhost:8080/tiendas.html`
* **Registro y Cobertura de Centros de Distribución:** `http://localhost:8080/centros-distribucion.html`

---

## 📚 Estructura de Documentación

Toda la documentación técnica del proyecto se encuentra centralizada en la carpeta [`docs/`](docs/):

* [`docs/CHANGELOG.md`](docs/CHANGELOG.md): Registro cronológico de cambios, adiciones y correcciones.
* [`docs/dto/`](docs/dto/): Contratos de API (JSON Schemas e interfaces TypeScript) para integración frontend.
* [`docs/Historias de Usuario/`](docs/Historias%20de%20Usuario/): Guías funcionales y técnicas de cada Historia de Usuario (`HU-01`, `HU-07`, `HU-09`, `HU-29`).
* [`docs/Fix´s/`](docs/Fix´s/): Informes formales de auditoría y refactorización técnica (`fix1-hu-07-09.md`, `fix2-hu-07-09.md`).
* [`docs/How-Work-DB.md`](docs/How-Work-DB.md): Esquema de tablas relacionales, integridad referencial y enlaces con Supabase Auth.
* [`docs/Hexagonal-Arquitecture.md`](docs/Hexagonal-Arquitecture.md): Guía de buenas prácticas y flujo de capas en la arquitectura hexagonal.
