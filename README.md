# Cafe Don Bosco

Cafe Don Bosco es un sistema web para el manejo completo de una
cafeteria: por un lado esta la tienda, donde cualquier cliente puede
entrar sin crear cuenta, ver el menu, personalizar su pedido (tamano,
tipo de leche, extras, etc.), agregarlo al carrito y pagarlo; por otro
lado esta el panel del administrador, donde se controla el catalogo,
el inventario, las ventas del mostrador y el historial completo del
negocio. Ambos lados comparten la misma base de datos y las mismas
reglas de negocio, para que el stock y las ventas siempre cuadren sin
importar si la venta se hizo en linea o en el mostrador.

El proyecto esta hecho en Java puro con Jakarta EE (Servlets + JSP +
JDBC), sin frameworks como Spring. La idea de usar Servlets y JSP
"a mano" en vez de un framework fue justamente entender bien como
funciona el patron MVC por dentro: quien recibe la peticion HTTP, quien
decide la logica de negocio, quien habla con la base de datos y quien
arma finalmente el HTML que ve el usuario, sin que un framework lo
resuelva por debajo.

## Tecnologias usadas

- **Java 17**
- **Maven** para las dependencias y el empaquetado
- **Jakarta EE 10** (Servlets 6.0, JSP, JSTL)
- **MySQL 8** como base de datos
- **Apache Tomcat 10.1** como servidor de aplicaciones
- Librerias puntuales para tareas especificas: **jBCrypt** (cifrado de
  contrasenas), **Gson** (JSON), **Apache PDFBox** (generar el PDF del
  ticket), **Jakarta Mail** (enviar el ticket por correo), **JUnit 5 +
  Mockito** (pruebas)

---

## Como ejecutar el proyecto

Para correr este proyecto en una computadora nueva hacen falta dos
cosas ademas del codigo: una base de datos MySQL corriendo, y un
servidor Tomcat donde desplegar la aplicacion. Aqui explico como lo
hice yo, usando XAMPP para la base de datos y el plugin Smart Tomcat de
IntelliJ para no tener que compilar y desplegar a mano cada vez que
cambio algo.

### Paso 1: levantar la base de datos con XAMPP

XAMPP es un instalador que trae Apache, MySQL, PHP y phpMyAdmin ya
configurados juntos. De todo eso yo solo uso **MySQL** (para guardar
los datos) y **phpMyAdmin** (para administrar la base desde el
navegador, en vez de usar la consola). Apache y PHP no participan en
este proyecto porque quien sirve las paginas es Tomcat, no XAMPP.

1. Instala XAMPP desde https://www.apachefriends.org
2. Abre el panel de control de XAMPP y dale **Start** al modulo
   **MySQL** (no hace falta iniciar Apache).
3. Entra a phpMyAdmin, ya sea con el boton "Admin" del panel de XAMPP o
   yendo directo a `http://localhost/phpmyadmin`.
4. En la pestana **Import**, selecciona el archivo `db/schema.sql` de
   este proyecto y dale **Go**. Ese script crea la base de datos
   `cafe_don_bosco`, todas sus tablas y los datos con los que arranca
   el sistema: las categorias, cinco productos de ejemplo, un usuario
   administrador y las opciones de personalizacion (tamano, tipo de
   leche, nivel de azucar, extras).
5. Si todo salio bien, en el panel izquierdo de phpMyAdmin deberia
   aparecer la base `cafe_don_bosco` con sus tablas ya llenas.

Por defecto la aplicacion se conecta a
`jdbc:mysql://localhost:3306/cafe_don_bosco` con el usuario `root` y
sin contrasena, que es justo como viene MySQL en XAMPP por defecto. Si
tu instalacion usa otro usuario o contrasena, se puede cambiar sin
tocar el codigo definiendo estas variables de entorno antes de
desplegar (revisa `.env.example` para la lista completa):

```bash
export DB_URL="jdbc:mysql://localhost:3306/cafe_don_bosco?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USUARIO="root"
export DB_PASSWORD="tu_password"
```

### Paso 2: desplegar con Smart Tomcat en IntelliJ

En vez de compilar el proyecto, empaquetarlo en un `.war` y copiarlo a
mano dentro de la carpeta de Tomcat cada vez que hago un cambio, uso el
plugin **Smart Tomcat**: despliega el proyecto directo desde IntelliJ
contra una instalacion de Tomcat que ya tengo en el disco, y lo vuelve
a desplegar solo cuando reinicio la ejecucion.

1. Descarga Apache Tomcat 10.1 (Core, en formato zip) desde
   https://tomcat.apache.org/download-10.cgi y descomprimelo en una
   carpeta cualquiera. Este proyecto necesita especificamente la
   version 10.1 porque usa Jakarta EE 10 (el paquete `jakarta.servlet`
   en vez del `javax.servlet` viejo de Tomcat 9 o anteriores).
2. En IntelliJ, ve a `File > Settings > Plugins`, busca en el
   Marketplace **Smart Tomcat**, instalalo y reinicia el IDE.
3. Abre este proyecto en IntelliJ (`File > Open` y selecciona la
   carpeta) y espera a que termine de descargar las dependencias de
   Maven.
4. Crea una configuracion de ejecucion nueva: `Run > Edit
   Configurations… > + > Smart Tomcat`.
   - En **Tomcat Server**, dale a `Configure…` y selecciona la carpeta
     donde descomprimiste Tomcat en el paso 1.
   - En **Deployment directory**, deja el modulo web del proyecto que
     IntelliJ detecta solo.
   - En **Context path** puedes escribir `/` para que el sitio quede
     directo en `http://localhost:8080/`, o dejarlo en blanco para que
     use el nombre del proyecto.
5. Presiona el boton de Run (▶) sobre esa configuracion. Cuando la
   consola muestre que el servidor arranco, la aplicacion ya esta
   corriendo.

### Paso 3: entrar al sistema

Con MySQL (XAMPP) y Tomcat (Smart Tomcat) corriendo al mismo tiempo,
entra a `http://localhost:8080/` (o a la ruta que hayas puesto como
context path). Ahi aparece la pantalla inicial con dos opciones: entrar
como administrador o entrar como consumidor.

Para probar el panel de administrador, este es el usuario que ya viene
cargado por `db/schema.sql`:

```
Correo:      admin@cafedonbosco.com
Contrasena:  Admin123!
```

(Esta contrasena es solo para pruebas locales; en un entorno real
habria que cambiarla.)

Si la pagina no carga o marca un error de conexion a la base de datos,
lo primero que reviso es que el modulo MySQL de XAMPP siga encendido:
se apaga solo si reinicias la computadora o cierras XAMPP.

---

## Como esta organizado el codigo

El proyecto sigue el patron **MVC** de forma bastante literal, dividido
en capas para que cada clase tenga una sola responsabilidad:

```
Servlet (controller)  ->  Service (reglas de negocio)  ->  DAO (JDBC)  ->  MySQL
                                                       \->  JSP (vista)
```

- **`model`**: las clases que representan las cosas del negocio
  (`Producto`, `Venta`, `Carrito`, `Usuario`, etc.).
- **`dto`**: en vez de mandar las entidades directo al cliente, se usan
  DTOs de entrada (`request`) y salida (`response`); asi, por ejemplo,
  la contrasena del usuario nunca puede terminar viajando por accidente
  en una respuesta JSON.
- **`dao`**: aqui vive todo el SQL, escrito a mano con
  `PreparedStatement` (sin ningun ORM de por medio).
- **`service`**: la capa mas importante para mi: aqui van todas las
  validaciones y las reglas de negocio (que el precio se calcule
  siempre del lado del servidor, que no se pueda vender mas stock del
  que hay, etc.), separadas de como se guardan los datos y de como se
  reciben las peticiones.
- **`controller`**: los Servlets que exponen la API en formato JSON
  bajo `/api/...`.
- **`controller.vista.admin`** y **`controller.vista.tienda`**: otro
  grupo de Servlets, que en vez de responder JSON arman la pagina y la
  mandan a una vista JSP con `RequestDispatcher.forward()`.
- **`filter`**: los filtros de seguridad (quien puede entrar a
  `/admin/*` o a `/api/admin/*`) y el manejo global de errores.
- **`scheduler`**: un hilo en segundo plano que cada 30 segundos revisa
  si algun pedido ya deberia estar listo (segun el tiempo de
  preparacion) y lo actualiza solo, sin que el administrador tenga que
  hacerlo a mano.
- **`util`**: utilidades compartidas como el manejo de la conexion a
  MySQL, el cifrado de contrasenas, la lectura/escritura de JSON, las
  validaciones comunes, etc.
- **`exception`**: excepciones propias para cada tipo de error de
  negocio, de forma que el cliente siempre reciba un mensaje claro y
  nunca un error crudo de Java.

### Por que una sola tabla de ventas

Al principio pense en tener una tabla separada para "pedidos web" y
otra para "ventas de mostrador", pero eso hubiera significado
duplicar toda la logica de descontar inventario y de calcular totales
en dos lugares distintos. En vez de eso, hay una sola entidad `Venta`
con un campo `tipoVenta` (`PRESENCIAL` o `WEB`): las dos comparten
exactamente el mismo metodo para registrarse
(`VentaServiceImpl.registrarConTransaccion`), el mismo descuento de
stock y el mismo ciclo de estados
(`RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO`, o `CANCELADO` en
cualquier momento antes de entregarse).

### Personalizacion de productos

Un producto puede tener uno o varios grupos de opciones (por ejemplo,
un cafe tiene "Tamano", "Tipo de leche" y "Nivel de azucar"). Cada
grupo puede ser de una sola opcion o de varias, y puede ser obligatorio
o no. Mientras el cliente elige, el precio se recalcula al instante en
el navegador para que vea el total antes de agregar al carrito, pero
esa parte es solo visual: cuando el producto realmente se agrega al
carrito, y otra vez cuando se confirma la compra, el servidor vuelve a
buscar cada opcion en la base de datos y recalcula el precio el mismo,
sin confiar en ningun numero que haya mandado el navegador. Esto evita
que alguien manipule el precio desde las herramientas de desarrollador
del navegador.

Tambien me encargue de que dos configuraciones distintas del mismo
producto (un cafe pequeno y ese mismo cafe grande, por ejemplo) queden
como dos lineas separadas en el carrito en vez de mezclarse en una
sola.

### El carrito sin recargar la pagina

Agregar un producto al carrito se hace con `fetch` (AJAX) contra
`/api/carrito`, no con un formulario que recarga toda la pagina: el
numero del carrito en la parte de arriba se actualiza al instante y
aparece un mensaje confirmando que se agrego (o explicando el error,
si algo fallo, por ejemplo que ya no queda stock). Si por algun motivo
la peticion por red falla, el formulario cae de vuelta a un envio
normal para que el cliente igual pueda completar la compra.

---

## Rutas de la tienda

| Ruta | Que hace |
| --- | --- |
| `/tienda` | Pagina de inicio con productos destacados |
| `/tienda/menu` | Catalogo completo, con busqueda y filtro por categoria |
| `/tienda/producto?id={id}` | Ficha del producto con sus opciones de personalizacion |
| `/tienda/carrito` | Ver y editar el carrito de la sesion actual |
| `/tienda/checkout` | Datos de entrega y metodo de pago |
| `/tienda/confirmacion?token={token}` | Pantalla de confirmacion tras pagar |
| `/tienda/ticket?token={token}` | El comprobante: se puede imprimir, descargar en PDF o compartir por WhatsApp |
| `/tienda/nosotros` | Informacion sobre la cafeteria |

El consumidor nunca necesita iniciar sesion para comprar: el carrito
vive en su sesion de navegador como invitado. Si quiere, puede
registrarse (`/api/auth/registro`) para despues consultar su historial
de pedidos, pero es completamente opcional.

## Panel de administracion

| Ruta | Que hace |
| --- | --- |
| `/login` | Inicio de sesion del administrador |
| `/admin/dashboard` | Resumen del dia: ventas, productos con poco stock, accesos rapidos |
| `/admin/productos` | Catalogo con el stock real de cada producto |
| `/admin/venta-nueva` | Punto de venta para registrar una compra hecha en el mostrador |
| `/admin/historial-ventas` | Historial de todas las ventas, web y presenciales |
| `/admin/ticket?id={id}` | Comprobante de cualquier venta |

Ninguna de estas paginas se puede abrir sin haber iniciado sesion como
administrador: si se intenta, el sistema redirige automaticamente al
login.

Algunas tareas administrativas (crear o editar un producto, ajustar el
inventario, dar de alta un proveedor o registrar una compra) todavia
las hago a traves de la API directamente en vez de tener un formulario
en el panel; la logica y las validaciones ya estan completas del lado
del servidor, pero me falta construir esas pantallas.

---

## La API

Todas las respuestas de la API siguen el mismo formato:

```json
{ "exitoso": true, "mensaje": "...", "datos": { } }
```

Rutas publicas (no necesitan sesion):

| Metodo | Ruta | Que hace |
| --- | --- | --- |
| POST | `/api/auth/login` | Inicia sesion |
| POST | `/api/auth/registro` | Crea una cuenta de consumidor (opcional) |
| GET | `/api/categorias` | Lista las categorias |
| GET | `/api/productos` | Lista el catalogo (se puede filtrar por categoria, texto y orden) |
| GET | `/api/productos/{id}` | Detalle de un producto |
| GET | `/api/productos/{id}/opciones` | Grupos de personalizacion de ese producto |
| GET/POST/PUT/DELETE | `/api/carrito`, `/api/carrito/items/{clave}` | Manejo del carrito |
| POST | `/api/checkout` | Confirma la compra |
| GET | `/api/tickets/{token}` | Consulta un ticket por su codigo |
| GET | `/api/tickets/{token}/pdf` | Descarga el ticket en PDF |
| GET | `/api/tickets/{token}/whatsapp-link` | Genera un enlace de WhatsApp con el pedido ya redactado |

Rutas que requieren sesion de administrador (verificadas por
`RolAdminFilter`):

| Metodo | Ruta | Que hace |
| --- | --- | --- |
| GET/POST/PUT | `/api/admin/productos` | Ver, crear y editar productos |
| GET/PUT | `/api/admin/inventario` | Ver y ajustar el stock |
| GET/POST | `/api/admin/ventas` | Historial y registro de ventas presenciales |
| PATCH | `/api/admin/ventas/{id}` | Cambiar el estado de un pedido |
| GET/POST | `/api/admin/compras` | Registrar compras a proveedores |
| GET/POST/PUT | `/api/admin/proveedores` | Gestion de proveedores |
| GET | `/api/admin/dashboard` | Estadisticas para el panel principal |

---

## Seguridad

Algunas decisiones que tome pensando en que esto pudiera usarse de
verdad y no solo para la demo:

- Las contrasenas se guardan cifradas con BCrypt, nunca en texto
  plano, y nunca se incluyen en ninguna respuesta de la API.
- Despues de 5 intentos fallidos de inicio de sesion seguidos con el
  mismo correo, el sistema bloquea ese correo por 15 minutos, para
  dificultar que alguien intente adivinar una contrasena a la fuerza.
- El precio y el stock de cada producto siempre se vuelven a revisar
  en el servidor antes de agregar algo al carrito y antes de confirmar
  la compra, sin importar que datos haya mandado el navegador.
- El descuento del inventario se hace con una condicion en la misma
  consulta SQL (`UPDATE ... WHERE cantidad >= ?`) para que, si dos
  personas compran el ultimo producto casi al mismo tiempo, no se
  pueda vender mas de lo que realmente hay.
- Un consumidor solo puede ver su propio historial de pedidos y su
  propia cuenta; el sistema siempre compara el pedido contra el
  usuario que inicio sesion, no solo contra si hay o no una sesion
  activa.
- El ticket de una compra se busca por un codigo aleatorio, no por el
  numero consecutivo de la venta, para que nadie pueda ver el ticket de
  otra persona con solo cambiar un numero en la URL.
- Todo el texto que un usuario escribe y que despues se vuelve a
  mostrar en una pagina (su nombre, sus notas del pedido) se escapa
  antes de mostrarlo, para evitar que alguien intente inyectar codigo
  a traves de esos campos.

## Un detalle que aprendi al desplegar en Tomcat

La clase `ConexionBD` carga el driver de MySQL a mano con
`Class.forName("com.mysql.cj.jdbc.Driver")`. En un programa Java normal
esto no hace falta porque el driver se auto-registra, pero dentro de
Tomcat el JAR del driver vive en el classloader propio de la
aplicacion, y ese auto-registro no siempre se dispara. Sin esta linea,
la aplicacion compila y pasa las pruebas sin ningun problema, pero al
desplegarla en un Tomcat real falla con `No suitable driver found` en
cuanto intenta hablar con la base de datos — un error que solo aparece
en el servidor real, nunca en `mvn test`.

## Pruebas

```bash
mvn test
```

El proyecto tiene 83 pruebas unitarias con JUnit 5 y Mockito, sobre
todo para la capa de servicios: validaciones de datos, el carrito
(incluyendo que dos personalizaciones del mismo producto no se
mezclen), el bloqueo de intentos fallidos de login, la maquina de
estados de una venta y el horario de atencion.
