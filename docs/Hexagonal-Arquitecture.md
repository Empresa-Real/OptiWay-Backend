# En este proyecto los “módulos” están separados por responsabilidad, siguiendo arquitectura hexagonal:

```text
infraestructura → aplicación → dominio
```

## **1. Dominio**

Ruta:

```text
domain/model/Usuario.java
```

Contiene las reglas y modelos principales del negocio. `Usuario` no debería depender de Spring, JPA ni HTTP.

## **2. Aplicación**

Rutas:

```text
application/port/in/
application/port/out/
application/service/
```

- `port/in`: casos de uso que puede ejecutar la aplicación:
  - crear usuario
  - obtener usuarios
  - eliminar usuario
- `port/out`: contratos que la aplicación necesita para hablar con sistemas externos, por ejemplo la base de datos.
- `service`: implementa la lógica de negocio. `UsuarioService` valida nombre, correo y rol, y utiliza el puerto de salida.

## **3. Infraestructura**

Rutas:

```text
infraestructure/adapter/in/rest/
infraestructure/adapter/out/persistence/
```

- `adapter/in/rest`: entrada HTTP. `UsuarioController` recibe peticiones REST y llama a los casos de uso.
- `adapter/out/persistence`: salida hacia PostgreSQL. `UsuarioRepositoryAdapter` convierte el modelo de dominio en una entidad JPA y usa `UsuarioJpaRepository`.

El flujo de crear usuario es:

```text
POST /api/usuarios
        ↓
UsuarioController
        ↓
CrearUsuarioUseCase
        ↓
UsuarioService
        ↓
UsuarioRepositoryPort
        ↓
UsuarioRepositoryAdapter
        ↓
UsuarioJpaRepository
        ↓
PostgreSQL
```

La idea central es que el dominio y la aplicación no dependan directamente de PostgreSQL ni de HTTP. Por eso existen interfaces como `CrearUsuarioUseCase` y `UsuarioRepositoryPort`: funcionan como contratos entre módulos.

# Modificar

## Como agregar una nueva funcionalidad

Supongamos que se necesita agregar un endpoint para crear productos. El cambio debe avanzar desde el centro hacia afuera:

```text
Producto
        ↓
CrearProductoUseCase
        ↓
ProductoService
        ↓
ProductoRepositoryPort
        ↓
ProductoRepositoryAdapter
        ↓
ProductoJpaRepository
        ↓
ProductoController
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

En `infraestructure/adapter/out/persistence/` se agregan:

- `ProductoJpaEntity`, con las anotaciones JPA.
- `ProductoJpaRepository`, que extiende `JpaRepository`.
- `ProductoRepositoryAdapter`, que convierte entre `Producto` y `ProductoJpaEntity`.

El adaptador implementa `ProductoRepositoryPort` y oculta los detalles de PostgreSQL.

### 6. Crear el endpoint

En `infraestructure/adapter/in/rest/ProductoController.java` se expone el caso de uso:

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