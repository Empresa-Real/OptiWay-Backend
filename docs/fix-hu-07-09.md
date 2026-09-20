# Informe de Auditoría y Refactorización de Arquitectura Hexagonal

**Proyecto:** OptiWay Backend  
**Rama auditada:** `feature/HU-07-Crear-Tienda`  
**Fecha:** 20/09/2026  
**Estado final:** ✅ **BUILD SUCCESS** (Arquitectura estandarizada y conflictos resueltos)

---

## 1. Resumen Ejecutivo

Durante la revisión y auditoría técnica de la rama se identificaron inconsistencias estructurales, conflictos de Git sin resolver y discrepancias entre la documentación y el código real. Se procedió a corregir la arquitectura para que todos los módulos (`Usuarios`, `Tiendas`, `Centros de Distribución`) sigan rigurosamente el mismo patrón de **Arquitectura Hexagonal (Puertos y Adaptadores)**.

---

## 2. Errores Identificados y Soluciones Aplicadas

### 🔴 Error 1: Marcadores de conflicto Git sin resolver en `application.properties`
* **Problema:** El archivo `src/main/resources/application.properties` contenía marcas de conflicto (`<<<<<<< HEAD`, `=======`, `>>>>>>>`) de un merge anterior. Adicionalmente, había un valor por defecto inseguro (`AQUI_TU_CONTRASEÑA_DE_POSTGRESQL`) y una clave de servicio expuesta.
* **Impacto:** Provocaba fallos al levantar el contexto de Spring Boot (`ServiceException` / Dialect de Hibernate no resuelto).
* **Solución:** Se limpiaron los marcadores de conflicto, centralizando la configuración con variables de entorno:
  * `spring.datasource.password=${DB_PASSWORD}`
  * `supabase.service-role-key=${SUPABASE_SERVICE_ROLE_KEY}`
  * `spring.security.oauth2.resourceserver.jwt.issuer-uri=https://${SUPABASE_PROJECT_REF:cehzzcvnkiuvkabglrzp}.supabase.co/auth/v1`

---

### 🟠 Error 2: Duplicidad tipográfica de paquetes (`infraestructure` vs `infrastructure`)
* **Problema:** Existían dos paquetes raíz paralelos:
  * `com.example.optiway.infraestructure` (escrito con `ae`) para Usuarios y Tiendas.
  * `com.example.optiway.infrastructure` (inglés correcto) para Centros de Distribución.
* **Impacto:** Desorden arquitectónico, conflicto de nombres de beans en Spring (`apiExceptionHandler`) y riesgo de imports cruzados erróneos.
* **Solución:** Se unificó todo bajo el estándar en inglés: `com.example.optiway.infrastructure`.

---

### 🟡 Error 3: Desalineación de la Arquitectura Hexagonal en Centros de Distribución
* **Problema:** Mientras que `Usuarios` y `Tiendas` seguían la convención hexagonal estándar:
  * Controladores en `infrastructure/adapter/in/rest/`
  * Entidades y Repositorios JPA en `infrastructure/adapter/out/persistence/`
  
  El módulo de `Centros de Distribución` tenía:
  * Controlador en `infrastructure/adapter/in/web/`
  * Adapter de persistencia huérfano suelto en `infrastructure/adapter/`
  * Repositorio Spring Data en `infrastructure/adapter/repository/SpringDataCentroDistribucionRepository.java`
* **Solución:** Se estandarizó la estructura completa:
  1. Controlador movido a: `infrastructure/adapter/in/rest/CentroDistribucionController.java`
  2. Request DTO movido a: `infrastructure/adapter/in/rest/dto/CrearCentroDistribucionRequest.java`
  3. Repositorio JPA renombrado a: `CentroDistribucionJpaRepository.java` y ubicado en `infrastructure/adapter/out/persistence/` (igual que `TiendaJpaRepository` y `UsuarioJpaRepository`).
  4. Adapter de persistencia ubicado en: `infrastructure/adapter/out/persistence/CentroDistribucionRepositoryAdapter.java`.

---

### 🔵 Error 4: Discrepancia entre la guía textual de HU-07 y el código Java real
* **Problema:** La guía `docs/HU-07_Crear_tiendas.md` documentaba un payload con `telefono` y `encargadoId`, pero la entidad `Tienda.java` y la validación en `TiendaService.java` solo aceptan y procesan `nombre`, `direccion` y `ciudad` (generando `codigo` y `estado` en backend).
* **Solución:** 
  1. Se preservó el documento original del autor sin alterar su guía conceptual.
  2. Se crearon los contratos y DTOs exactos en la carpeta `docs/dto/` con ejemplos JSON reales y tipado TypeScript para que el equipo de frontend no tenga discrepancias.

---

## 3. Estructura Hexagonal Estandarizada Actual

```text
src/main/java/com/example/optiway/
├── application/
│   ├── port/
│   │   ├── in/               # Casos de uso (Interfaces de entrada)
│   │   └── out/              # Puertos de persistencia y auth (Interfaces de salida)
│   └── service/              # Servicios de dominio y lógica de negocio
│
├── domain/
│   └── model/                # Entidades puras de dominio (Usuario, Tienda, CentroDistribucion, Rol)
│
└── infrastructure/           # Capa de infraestructura (Hexagonal)
    ├── adapter/
    │   ├── in/
    │   │   └── rest/         # Controladores REST y DTOs de entrada
    │   │       ├── ApiExceptionHandler.java
    │   │       ├── CentroDistribucionController.java
    │   │       ├── TiendaController.java
    │   │       ├── UsuarioController.java
    │   │       └── dto/
    │   │           └── CrearCentroDistribucionRequest.java
    │   │
    │   └── out/
    │       ├── auth/         # Adaptadores a servicios externos (Supabase Auth)
    │       │   └── SupabaseAuthAdapter.java
    │       └── persistence/  # Adaptadores de base de datos (JPA Entities, JpaRepositories, Adapters)
    │           ├── CentroDistribucionJpaEntity.java
    │           ├── CentroDistribucionJpaRepository.java
    │           ├── CentroDistribucionRepositoryAdapter.java
    │           ├── TiendaJpaEntity.java
    │           ├── TiendaJpaRepository.java
    │           ├── TiendaRepositoryAdapter.java
    │           ├── UsuarioJpaEntity.java
    │           ├── UsuarioJpaRepository.java
    │           └── UsuarioRepositoryAdapter.java
    │
    └── config/               # Configuraciones del framework (SecurityConfig)
```

---

## 4. Validación de Compilación

* Comando ejecutado: `.\mvnw.cmd clean compile -DskipTests`
* Resultado: **BUILD SUCCESS** (37 archivos fuente compilados sin advertencias ni errores).
