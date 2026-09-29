# Cafe Don Bosco - Backend

Backend Java (Jakarta EE 10 / Servlets + JDBC + MySQL) para Cafe Don Bosco.
Expone una API REST compartida que atiende tanto el panel del
administrador como la tienda del consumidor, con una unica base de datos
e inventario.

## Arquitectura

```
controller (Servlets) -> service (reglas de negocio) -> dao (JDBC) -> MySQL
```

- **model**: entidades del dominio (`Usuario`, `Categoria`, `Producto`,
  `Inventario`, `Venta`, `DetalleVenta`, `Compra`, `DetalleCompra`,
  `Carrito`).
- **dto**: objetos de entrada (`request`) y salida (`response`) que no
  exponen directamente las entidades (por ejemplo, la contrasena nunca
  viaja en una respuesta).
- **dao / dao.impl**: acceso a datos con JDBC puro y `PreparedStatement`.
- **service / service.impl**: validaciones y logica de negocio, incluida
  la transaccion de venta (registro + descuento de stock atomico).
- **controller**: Servlets anotados con `@WebServlet` que exponen la API
  JSON bajo `/api/...`.
- **controller.vista.admin** / **controller.vista.tienda**: Servlets que
  hacen `forward()` a JSP para el panel del administrador y la tienda
  del consumidor respectivamente (ver secciones de abajo).
- **filter**: `CorsFilter` y `RolAdminFilter` (protegen `/api/admin/*`),
  y `SesionVistaFilter` (protege las pantallas JSP bajo `/admin/*`).
- **util**: `ConexionBD`, `PasswordUtil` (BCrypt), `JsonUtil` (Gson),
  `ValidacionUtil`, `SessionUtil`, `Constantes`.
- **exception**: `AppException` y subclases especificas, todas mapeadas a
  un codigo HTTP y un mensaje seguro para el cliente.

### Modelo de venta unificado

En vez de duplicar `Venta` y `Pedido`, se usa una sola entidad `Venta`
con `TipoVenta` (`PRESENCIAL` o `WEB`). Esto evita mantener dos historiales
y dos formas de descontar inventario: la venta del mostrador (POS del
administrador) y la compra del consumidor comparten exactamente la misma
transaccion (`VentaServiceImpl.registrarConTransaccion`).

## Requisitos

- Java 17+
- Maven 3.9+
- MySQL 8+
- Apache Tomcat 10.1+ (Jakarta EE 10 / Servlet 6.0)

## Configuracion de la base de datos

1. Ejecuta el script `db/schema.sql` en tu servidor MySQL:

   ```bash
   mysql -u root -p < db/schema.sql
   ```

   Esto crea la base `cafe_don_bosco`, todas las tablas y datos iniciales
   (categorias y un usuario administrador).

2. Por defecto, `ConexionBD` se conecta a
   `jdbc:mysql://localhost:3306/cafe_don_bosco` con el usuario `root` y
   contrasena vacia. Para otro entorno, define las variables de entorno
   antes de desplegar (no se necesita recompilar):

   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/cafe_don_bosco?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
   export DB_USUARIO="root"
   export DB_PASSWORD="tu_password"
   ```

## Compilar y desplegar

```bash
mvn clean package
```

Esto genera `target/CafeDonBosco.war`. Copialo a la carpeta `webapps` de
Tomcat (o despliegalo con el manager de Tomcat) y la aplicacion quedara
disponible en `http://localhost:8080/CafeDonBosco/`.

## Frontend JSP: portal, administrador y tienda

Ademas de la API JSON, la aplicacion sirve un frontend completo en
JSP/Servlet. `index.html` es un portal de entrada con dos caminos que
nunca se mezclan:

- **Administrador**: requiere iniciar sesion. Sin sesion con rol
  `ADMINISTRADOR`, `SesionVistaFilter` redirige cualquier ruta bajo
  `/admin/*` a `/login`.
- **Consumidor**: entra directo a la tienda sin cuenta ni contrasena.
  No existe ningun login para consumidores; el carrito y el checkout
  funcionan enteramente sobre la sesion HTTP como invitado.

### Panel del administrador (`/admin/*`, protegido)

| Ruta | Descripcion |
| --- | --- |
| `/login`, `/logout` | Inicio/cierre de sesion, exclusivo para `ADMINISTRADOR` |
| `/admin/dashboard` | KPIs del dia/mes, ventas recientes, accesos rapidos, stock bajo |
| `/admin/productos` | Catalogo administrativo con stock exacto (solo lectura) |
| `/admin/venta-nueva` | POS: arma una venta presencial reutilizando `CarritoService` y la registra con `VentaService.registrarVentaPresencial` |
| `/admin/historial-ventas` | Historial combinado de ventas presenciales y web |
| `/admin/ticket?id={id}` | Comprobante de cualquier venta, por id (el administrador ya esta autenticado) |

### Tienda del consumidor (`/tienda/*`, publico)

| Ruta | Descripcion |
| --- | --- |
| `/tienda` | Home: destacados y categorias |
| `/tienda/menu` | Catalogo completo (busqueda, filtro por categoria, orden) |
| `/tienda/producto?id={id}` | Detalle de producto con relacionados |
| `/tienda/carrito` | Carrito de sesion (agregar/quitar/vaciar) |
| `/tienda/checkout` | Datos de envio + metodo de pago, registra la venta WEB |
| `/tienda/confirmacion?token={token}` | Confirmacion inmediata tras la compra |
| `/tienda/ticket?token={token}` | Comprobante imprimible |
| `/tienda/nosotros` | Pagina institucional |

El carrito del consumidor y el "carrito" del POS del administrador usan
la misma clase `Carrito`/`CarritoService`, pero se guardan en atributos
de sesion distintos (`SESSION_CARRITO` vs `SESSION_CARRITO_ADMIN`) para
que probar ambos flujos en el mismo navegador no mezcle una venta de
mostrador con una compra web.

Todas las vistas viven en `WEB-INF/views/` (solo alcanzables por
`RequestDispatcher.forward()`, nunca por URL directa) y usan JSTL en vez
de scriptlets Java. Los tickets de admin y de la tienda comparten el
mismo fragmento `_ticket-contenido.jspf`.

Este flujo completo fue probado de punta a punta contra un Tomcat 10 y
un MySQL 8 reales: login correcto/fallido, proteccion de `/admin/*` sin
sesion, una venta POS con dos productos (verificando el descuento de
stock y el total en el dashboard/historial), navegacion del catalogo con
filtros, agregar/quitar del carrito, un intento de open-redirect en el
parametro `volver` (rechazado), validacion de checkout con entrega a
domicilio sin direccion, una compra web completa con confirmacion y
ticket, un token de ticket inventado (rechazado) y logout. El log del
servidor no registro ningun error ni advertencia durante toda la prueba.

## Endpoints principales de la API

| Metodo | Ruta | Acceso | Descripcion |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Publico | Login compartido admin/consumidor |
| POST | `/api/auth/registro` | Publico | Registro de consumidor |
| GET | `/api/auth/sesion` | Publico | Usuario de la sesion actual |
| POST | `/api/auth/logout` | Publico | Cierra la sesion |
| GET | `/api/categorias` | Publico | Categorias activas |
| GET | `/api/productos` | Publico | Catalogo (filtros: `categoria`, `buscar`, `orden`) |
| GET | `/api/productos/{id}` | Publico | Detalle de producto |
| GET | `/api/productos/{id}/relacionados` | Publico | Productos de la misma categoria |
| GET/POST/PUT/DELETE | `/api/carrito`, `/api/carrito/items[/{id}]` | Publico (sesion) | Carrito de compras |
| POST | `/api/checkout` | Publico (sesion) | Registra la venta WEB y descuenta stock |
| GET | `/api/tickets/{token}` | Publico | Ticket de una venta por token aleatorio |
| GET/POST | `/api/admin/categorias` | Admin | Listar/crear categorias |
| PUT | `/api/admin/categorias/{id}` | Admin | Editar categoria |
| GET/POST | `/api/admin/productos` | Admin | Listar/crear productos (con stock) |
| PUT | `/api/admin/productos/{id}` | Admin | Editar producto |
| PUT | `/api/admin/productos/{id}/estado` | Admin | Activar/desactivar producto |
| GET | `/api/admin/inventario` | Admin | Inventario con stock exacto |
| PUT | `/api/admin/inventario` | Admin | Ajustar stock/stock minimo |
| GET/POST | `/api/admin/ventas` | Admin | Historial / registrar venta presencial (POS) |
| GET | `/api/admin/tickets/{id}` | Admin | Ticket de cualquier venta por id |
| GET/POST | `/api/admin/compras` | Admin | Historial / registrar compra a proveedor |
| GET | `/api/admin/dashboard` | Admin | KPIs, ventas recientes, stock bajo |

Todas las respuestas usan el sobre `ApiResponse`:

```json
{ "exitoso": true, "mensaje": "...", "datos": { } }
```

## Seguridad implementada

- Contrasenas con BCrypt (`jbcrypt`), nunca se devuelven en las respuestas.
- `PreparedStatement` en todo el acceso a datos.
- El precio y el stock se revalidan en el servidor durante el checkout;
  nunca se confia en lo que envia el navegador.
- El descuento de stock usa `UPDATE ... WHERE cantidad >= ?` dentro de una
  transaccion JDBC, para evitar sobreventa con solicitudes concurrentes.
- El ticket del consumidor se consulta por un token aleatorio
  (`UUID`), no por el id incremental de la venta.
- `RolAdminFilter` protege toda la seccion `/api/admin/*`.

## Nota tecnica: registro del driver JDBC en Tomcat

`ConexionBD` carga explicitamente `com.mysql.cj.jdbc.Driver` con
`Class.forName(...)` en un bloque estatico. En un classpath plano el
driver se auto-registra via `ServiceLoader`, pero dentro de un servlet
container el JAR vive en `WEB-INF/lib` bajo el classloader propio de la
aplicacion, y ese registro automatico no siempre se dispara: sin este
`Class.forName`, `DriverManager.getConnection()` falla con
`No suitable driver found`, algo que solo aparece al desplegar en un
Tomcat real (no en `mvn compile`/`package`, que no ejecutan el codigo).

## Usuario administrador de prueba

El script `db/schema.sql` crea `admin@cafedonbosco.com`. Cambia esa
contrasena (o genera un nuevo hash con `PasswordUtil.hashear(...)`) antes
de usar el sistema en un entorno real.

## Pendiente para siguientes fases

- Formularios de alta/edicion de productos y categorias en el panel de
  administrador (hoy `/admin/productos` es de solo lectura; crear/editar
  ya existe en la API `/api/admin/productos` pero sin vista JSP propia).
- Registro y "Mis pedidos" para un consumidor que si quiera crear cuenta
  (la API ya soporta `/api/auth/registro`; el consumidor de la tienda
  siempre compra como invitado, sin login).
- Generacion de PDF real del ticket (hoy "Imprimir / Descargar PDF" usa
  `window.print()`, que en cualquier navegador permite guardar como PDF
  desde el dialogo de impresion).
