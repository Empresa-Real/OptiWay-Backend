# OptiWay — Backend EBP06

<img alt="Java" src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" /> <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?logo=springboot&logoColor=white" /> <img alt="Spring Security" src="https://img.shields.io/badge/Spring%20Security-6-6DB33F?logo=springsecurity&logoColor=white" /> <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white" /> <img alt="Supabase" src="https://img.shields.io/badge/Supabase-Cloud-3ECF8E?logo=supabase&logoColor=white" /> <img alt="Apache Maven" src="https://img.shields.io/badge/Apache%20Maven-3.9+-C71A36?logo=apachemaven&logoColor=white" />


## Descripción General

Sistema backend para la plataforma **OptiWay**, diseñado para la gestión y optimización de la cadena de distribución logística, administración de centros de distribución (CD), gestión de tiendas comerciales, control de inventarios, registro de ingresos de mercancía y control de acceso granular basado en roles.

Desarrollado bajo los principios de **Arquitectura Hexagonal (Puertos y Adaptadores)** y **Clean Architecture** sobre el ecosistema Spring Boot y PostgreSQL (Supabase).


## Características

* **Gestión de Usuarios y Control de Acceso (RBAC):** Administración centralizada de usuarios con roles definidos (`ADMINISTRADOR`, `ENCARGADO_TIENDA`, `ENCARGADO_CD`, `PLANIFICADOR`), integrados con Supabase Auth y validación de tokens JWT mediante Spring Security.
* **Administración de Tiendas:** Registro de tiendas comerciales con asignación automática de códigos identificadores (`TND-XXXXXXXX`), consulta de listados y geolocalización.
* **Gestión de Centros de Distribución (CD):** Registro de centros de distribución con códigos únicos (`CD-XXXXXXXX`), cálculo y asignación de zonas de cobertura mediante relaciones con tiendas abastecidas (`@ManyToMany`).
* **Control y Consulta de Inventarios:** Monitoreo y filtros de stock por tienda, nombre, categoría y producto, con validación de accesos por rol y tienda asignada.
* **Ingreso y Recepción de Mercancía:** Trazabilidad de recepción de productos en centros de distribución con control de cantidades y origen.


## Tecnologías / Stack

* **Lenguaje:** Java 17
* **Framework Principal:** Spring Boot 3.4.3
* **Seguridad & Autenticación:** Spring Security 6 (OAuth2 Resource Server con validación JWT) y Supabase Admin Auth
* **Persistencia & ORM:** Spring Data JPA / Hibernate
* **Base de Datos:** PostgreSQL en la nube (Supabase)
* **Gestor de Construcción y Dependencias:** Apache Maven (con Maven Wrapper `mvnw`)


## Arquitectura

El proyecto organiza sus responsabilidades mediante el patrón de **Arquitectura Hexagonal (Puertos y Adaptadores)**:

```text
src/main/java/com/example/optiway/
├── domain/                  # Reglas de negocio puras e independientes de frameworks
│   └── model/               # Entidades de dominio (Usuario, Tienda, CentroDistribucion, Rol, Inventario, Producto)
│
├── application/             # Orquestación de casos de uso y lógica de aplicación
│   ├── port/
│   │   ├── in/              # Casos de uso / Puertos de entrada
│   │   └── out/             # Puertos de salida (persistencia y servicios externos)
│   └── service/             # Implementación de casos de uso y validaciones de negocio
│
└── infrastructure/          # Adaptadores y tecnologías externas
    ├── adapter/
    │   ├── in/rest/         # Controladores REST, DTOs de entrada y manejador global de excepciones
    │   └── out/
    │       ├── auth/        # Adaptador de autenticación externa (Supabase Admin API)
    │       └── persistence/ # Entidades JPA, repositorios Spring Data y adaptadores de persistencia
    └── config/              # Configuración del framework (Spring Security, Beans)
```

Para una explicación a fondo del flujo de capas, consulta [`docs/Hexagonal-Arquitecture.md`](docs/Hexagonal-Arquitecture.md).


## Endpoints

### Usuarios & Autenticación (`HU-01` & `HU-29`)
* `POST /api/usuarios`: Invitación y creación de usuarios (restringido a rol `ADMINISTRADOR`).
* `GET /api/usuarios`: Listado de todos los usuarios registrados.
* `GET /api/usuarios/{id}`: Consulta de usuario por identificador.
* `DELETE /api/usuarios/{id}`: Eliminación de usuario.
* `GET /api/usuarios/me`: Consulta de perfil y rol del usuario autenticado a partir del JWT.

### Tiendas (`HU-07`)
* `POST /api/tiendas`: Registro de nueva tienda (generación automática de código `TND-XXXXXXXX`).
* `GET /api/tiendas`: Consulta y listado de todas las tiendas disponibles.

### Centros de Distribución (`HU-09`)
* `POST /api/centros-distribucion`: Registro de nuevo CD (código `CD-XXXXXXXX`) y asignación de tiendas abastecidas.
* `GET /api/centros-distribucion`: Consulta y listado de todos los CDs con su información de cobertura.

### Inventario (`HU-11`)
* `GET /api/inventario`: Consulta de inventario por tienda (`tiendaId`) con filtros opcionales por `nombre`, `categoria` y `productoId`.

### Ingreso de Mercancía
* `POST /api/ingresos-mercancia`: Registro de ingreso de mercancía a un centro de distribución.


## Instalación y Ejecución

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

### Ejecutar el servidor Spring Boot
En Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd spring-boot:run
```

En Linux / macOS:
```bash
./mvnw spring-boot:run
```

La aplicación iniciará por defecto en `http://localhost:8080`.
