En este proyecto los módulos están separados por responsabilidad, siguiendo los principios de la **Arquitectura Hexagonal (Puertos y Adaptadores)**:

```text
infraestructura → aplicación → dominio
```

## **1. Dominio**

Ruta:

```text
domain/model/
├── Usuario.java
├── Tienda.java
├── CentroDistribucion.java
└── Rol.java (Enum)
```

Contiene las reglas y modelos principales del negocio. Ningún modelo de dominio depende de Spring, JPA ni HTTP.

## **2. Aplicación**

Rutas:

```text
application/port/in/              # Casos de uso (Interfaces de entrada)
├── CrearUsuarioUseCase.java
├── ObtenerUsuariosUseCase.java
├── EliminarUsuarioUseCase.java
├── ObtenerUsuarioUseCase.java
├── CrearTiendaUseCase.java
├── ObtenerTiendasUseCase.java
├── CrearCentroDistribucionUseCase.java
└── ObtenerCentrosDistribucionUseCase.java

application/port/out/             # Contratos de salida (Persistencia y auth)
├── UsuarioRepositoryPort.java
├── TiendaRepositoryPort.java
├── CentroDistribucionRepositoryPort.java
└── AuthPort.java

application/service/              # Lógica de negocio y orquestación
├── UsuarioService.java
├── TiendaService.java
└── CentroDistribucionService.java
```

- `port/in`: Casos de uso que expone la aplicación para ser llamados desde adaptadores primarios (ej: controladores REST).
- `port/out`: Contratos que la aplicación necesita para interactuar con sistemas externos (base de datos relacional, servicio de autenticación Supabase).
- `service`: Implementa la lógica de negocio, validaciones y reglas de dominio, consumiendo los puertos de salida sin acoplarse a JPA.

## **3. Infraestructura**

Rutas:

```text
infrastructure/adapter/in/rest/
├── ApiExceptionHandler.java
├── UsuarioController.java
├── TiendaController.java
├── CentroDistribucionController.java
└── dto/
    └── CrearCentroDistribucionRequest.java

infrastructure/adapter/out/
├── auth/
│   └── SupabaseAuthAdapter.java
└── persistence/
    ├── UsuarioJpaEntity.java, UsuarioJpaRepository, UsuarioRepositoryAdapter
    ├── TiendaJpaEntity.java, TiendaJpaRepository, TiendaRepositoryAdapter
    └── CentroDistribucionJpaEntity.java, CentroDistribucionJpaRepository, CentroDistribucionRepositoryAdapter
```

- `adapter/in/rest`: Entrada HTTP. Controladores que reciben peticiones REST, validan el formato y delegan a los casos de uso correspondientes (`CrearUsuarioUseCase`, `CrearTiendaUseCase`, `ObtenerTiendasUseCase`, `CrearCentroDistribucionUseCase`, etc.).
- `adapter/out/auth`: Adaptadores a servicios externos de identidad (Supabase Admin API).
- `adapter/out/persistence`: Salida hacia PostgreSQL. Los adaptadores implementan los puertos de salida (`*RepositoryPort`), mapeando entidades de dominio a entidades JPA y encapsulando `JpaRepository`.

El flujo de crear usuario es:

```text
POST /api/usuarios
        ↓
UsuarioController            (infrastructure/adapter/in/rest/)
        ↓
CrearUsuarioUseCase          (application/port/in/)
        ↓
UsuarioService               (application/service/)
        ↓
UsuarioRepositoryPort        (application/port/out/)
        ↓
UsuarioRepositoryAdapter     (infrastructure/adapter/out/persistence/)
        ↓
UsuarioJpaRepository         (infrastructure/adapter/out/persistence/)
        ↓
PostgreSQL                   (Base de datos externa)
```

La idea central es que el dominio y la aplicación no dependan directamente de PostgreSQL ni de HTTP. Por eso existen interfaces como `CrearUsuarioUseCase` y `UsuarioRepositoryPort`: funcionan como contratos entre módulos.

# Modificar

## Como agregar una nueva funcionalidad

Supongamos que se necesita agregar un endpoint para crear productos. El cambio debe avanzar desde el centro hacia afuera:

```text
Producto                     (domain/model/)
        ↓
CrearProductoUseCase         (application/port/in/)
        ↓
ProductoService              (application/service/)
        ↓
ProductoRepositoryPort       (application/port/out/)
        ↓
ProductoRepositoryAdapter    (infrastructure/adapter/out/persistence/)
        ↓
ProductoJpaRepository        (infrastructure/adapter/out/persistence/)
        ↓
ProductoController           (infrastructure/adapter/in/rest/)
```

### 1. Crear el modelo de dominio

Agregar `domain/model/Producto.java` con los datos propios del negocio. Esta clase no debe importar anotaciones de Spring o JPA.

Si existe una regla como "el precio no puede ser negativo", esa regla pertenece al dominio o al servicio de aplicación, no al controlador.

### 2. Crear el caso de uso de entrada

En `application/port/in/` se define el contrato:

```java
public interface CrearProductoUseCase {
                Producto crearProducto(Producto producto);
}
```

El puerto describe qué se puede hacer, no cómo se hace.

### 3. Implementar la lógica en el servicio

En `application/service/ProductoService.java` se implementa el caso de uso y se aplican las reglas de negocio:

```java
@Service
public class ProductoService implements CrearProductoUseCase {

                private final ProductoRepositoryPort productoRepositoryPort;

                public ProductoService(ProductoRepositoryPort productoRepositoryPort) {
                                this.productoRepositoryPort = productoRepositoryPort;
                }

                @Override
                public Producto crearProducto(Producto producto) {
                                // Validaciones y reglas de negocio
                                return productoRepositoryPort.guardar(producto);
                }
}
```

### 4. Crear el puerto de salida

En `application/port/out/` se define lo que el servicio necesita de la persistencia:

```java
public interface ProductoRepositoryPort {
                Producto guardar(Producto producto);
}
```

El servicio depende de esta interfaz, no de `JpaRepository`.

### 5. Crear el adaptador de persistencia

En `infrastructure/adapter/out/persistence/` se agregan:

- `ProductoJpaEntity`, con las anotaciones JPA.
- `ProductoJpaRepository`, que extiende `JpaRepository`.
- `ProductoRepositoryAdapter`, que convierte entre `Producto` y `ProductoJpaEntity`.

El adaptador implementa `ProductoRepositoryPort` y oculta los detalles de PostgreSQL.

### 6. Crear el endpoint

En `infrastructure/adapter/in/rest/ProductoController.java` se expone el caso de uso:

```java
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

                private final CrearProductoUseCase crearProductoUseCase;

                public ProductoController(CrearProductoUseCase crearProductoUseCase) {
                                this.crearProductoUseCase = crearProductoUseCase;
                }

                @PostMapping
                public Producto crearProducto(@RequestBody Producto producto) {
                                return crearProductoUseCase.crearProducto(producto);
                }
}
```

### 7. Probar y documentar

Después de implementar la funcionalidad:

1. Agregar pruebas del servicio y del controlador.
2. Compilar con `./mvnw test` o `mvnw.cmd test` en Windows.
3. Probar el endpoint desde Postman.
4. Actualizar `CHANGELOG.md` con la fecha y el cambio.

La regla práctica es: el controlador recibe la petición, el servicio decide, el puerto define el contrato y el adaptador se conecta con el sistema externo.

## Flujo de autenticacion y rol

La autenticacion pertenece a infraestructura, pero la decision de negocio se mantiene en aplicación:

```text
Supabase Auth                (Servicio de Auth externo / adapter out)
        ↓ JWT validado por Spring Security (infrastructure/config/SecurityConfig.java)
UsuarioController            (infrastructure/adapter/in/rest/)
        ↓ jwt.getSubject()
ObtenerUsuarioUseCase        (application/port/in/)
        ↓
UsuarioService               (application/service/)
        ↓
UsuarioRepositoryPort        (application/port/out/)
        ↓
UsuarioRepositoryAdapter     (infrastructure/adapter/out/persistence/)
        ↓
usuarios.auth_user_id y rol  (PostgreSQL - tabla public.usuarios)
```

El controlador no autoriza usando el email del body. El claim `sub` del JWT identifica al usuario de `auth.users`; el servicio busca ese UUID en la tabla local y aplica la regla `rol == ADMINISTRADOR` cuando corresponde.