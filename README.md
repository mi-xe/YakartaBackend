# Yakarta Backend — proyecto independiente

Este directorio contiene **únicamente el backend Spring Boot** de Yakarta. El frontend React se mantiene en otro proyecto y se comunica con este servicio exclusivamente mediante HTTP/JSON y el endpoint de carga de archivos. No se combinan ambos proyectos ni se necesita copiar el frontend dentro de esta carpeta.

## Requisitos

- Java 17 o superior.
- MySQL 8 o MariaDB compatible.
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`). La primera ejecución puede requerir conexión a Internet para descargar Maven y las dependencias.

## Configurar MySQL

1. Inicia MySQL (por ejemplo, mediante XAMPP).
2. Ejecuta `database/yakarta.sql` en phpMyAdmin o MySQL. Crea la base `yakarta` y la tabla `usuarios` si aún no existen; no borra cuentas.
3. Si estás reutilizando una base del backend PHP antiguo, sigue también las instrucciones de `database/migrate_legacy_php.sql`.
4. Configura credenciales mediante variables de entorno, o utiliza los valores locales predeterminados:

```properties
DB_URL=jdbc:mysql://localhost:3306/yakarta?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago
DB_USERNAME=root
DB_PASSWORD=
```

Spring Boot crea/actualiza las demás tablas mediante JPA. En una base existente, haz una copia de seguridad antes de iniciar la versión nueva.

### Campos nuevos del catálogo

La entidad `Producto` ahora persiste los siguientes campos adicionales:

- `descripcion`: texto personalizado del juego.
- `imagenesPreview`: arreglo JSON expuesto por la API con hasta 12 rutas/URL de capturas; en MySQL se guarda como texto JSON en `productos.imagenes_preview`.
- `plataformas`: arreglo JSON expuesto por la API con una o varias plataformas permitidas y guardado como texto JSON en `productos.plataformas`: `PC`, `PlayStation 3`, `PlayStation 4`, `PlayStation 5`, `Xbox Series S/X`, `Nintendo Switch`, `Nintendo Switch 2`.

Hibernate (`spring.jpa.hibernate.ddl-auto=update`) agrega las columnas al iniciar. Para instalaciones anteriores, selecciona en phpMyAdmin la misma base indicada por `DB_URL` y ejecuta `database/migrate_product_metadata.sql`; agrega las columnas faltantes y conserva los arrays JSON previos. El script se puede ejecutar más de una vez.

## Cargar imágenes desde el administrador

El endpoint coincide con el que invoca el frontend independiente:

```http
POST /api/archivos/imagenes
Content-Type: multipart/form-data
Cookie: JSESSIONID=...

file=<archivo de imagen>
```

La carga requiere una sesión de administrador. Se admiten JPG/JPEG, PNG, WEBP y GIF con un máximo de 8 MB por archivo. La respuesta es:

```json
{ "url": "/uploads/productos/<nombre-generado>" }
```

Los archivos se guardan en el directorio `uploads/productos/` del backend (configurable con `APP_UPLOAD_DIR`) y se sirven desde `/uploads/productos/...`. La base de datos guarda esas rutas dentro de `productos.imagen` y `productos.imagenes_preview`; los binarios de imagen no se guardan como BLOB. No borres esa carpeta: contiene los archivos subidos desde el panel.

Los productos continúan administrándose mediante `GET`, `POST`, `PUT` y `DELETE /api/productos`. Los endpoints de escritura requieren una sesión con rol `Administrador`.

## Iniciar el backend

En Windows PowerShell, desde la raíz de esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw spring-boot:run
```

La API queda disponible normalmente en `http://localhost:8080/api`. Comprueba `http://localhost:8080/api/health`; la documentación Swagger queda en `http://localhost:8080/swagger-ui/index.html`.

## Conectar el frontend independiente

En el proyecto React (otra carpeta/ZIP), configura el archivo `.env`:

```env
VITE_API_URL=http://localhost:8080/api
```

Si frontend y backend se ejecutan en el mismo equipo y el frontend se abre mediante `localhost` o `127.0.0.1`, también puedes dejar `VITE_API_URL=` vacío; el cliente React usa automáticamente el mismo hostname y el puerto `8080`. Si el backend está en otra máquina, coloca aquí la URL completa de su API.

Inicia React por separado con `npm install` y `npm run dev`. El backend admite los puertos variables de Vite en `localhost`, `127.0.0.1` y `::1`, con credenciales/cookies de sesión.

## Restaurar administradores por defecto

La versión original del proyecto incluía estas dos cuentas administrativas:

- `admin@yakarta.com` — contraseña inicial: `trescuatro` (Cristian Orlando).
- `mixin@yakarta.com` — contraseña inicial: `mixin2` (Mixin 123).

Si no aparecen en la tabla `usuarios`, selecciona la base `yakarta` en phpMyAdmin y ejecuta `database/restaurar_administradores_por_defecto.sql`. El script solo inserta las cuentas faltantes, almacena contraseñas PBKDF2 y no sobrescribe contraseñas, roles ni saldos de cuentas existentes. Después de ingresar, cambia ambas contraseñas desde un entorno administrativo seguro.

## Cuentas y permisos

- El registro público envía `POST /api/auth/registro`.
- El administrador puede administrar usuarios, productos, categorías y pedidos.
- La autenticación utiliza una cookie de sesión `HttpOnly`; el frontend no debe guardar credenciales ni sesiones en `localStorage`.

## Verificación

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

**Importante:** este ZIP es exclusivamente el backend. No incluye React, `node_modules` ni una copia del frontend. El frontend debe ejecutarse en su propio directorio y conectarse mediante `VITE_API_URL`.

### Confirmar la persistencia de la descripción

La entidad define explícitamente `@Column(name = "descripcion", columnDefinition = "TEXT")`, de modo que el nombre físico de la columna coincide con el contrato JSON `descripcion`. Después de sustituir una versión antigua del backend, detén el proceso anterior y vuelve a iniciar este proyecto para que `spring.jpa.hibernate.ddl-auto=update` aplique los cambios.

Si la base de datos ya tenía la tabla `productos` y la columna sigue ausente, ejecuta `database/migrate_product_metadata.sql` sobre la base `yakarta`. Puedes comprobar la persistencia con:

```sql
SELECT id, nombre, descripcion
FROM productos
ORDER BY id DESC;
```

La API devuelve la misma propiedad en `GET /api/productos` y `GET /api/productos/{id}`.

### Diagnóstico de galería

Después de guardar un juego, ejecuta `database/diagnostico_galeria_productos.sql` sobre la misma base seleccionada en phpMyAdmin. La columna `galeria_guardada` debe mostrar un array JSON con rutas como `/uploads/productos/....webp`. Si la lista está vacía, revisa primero la respuesta de `POST /api/archivos/imagenes` y después la petición `POST` o `PUT /api/productos` en la pestaña Red/Network del navegador.

El backend persiste las listas como texto JSON en columnas `TEXT` mediante campos básicos `String` de JPA y las expone como arrays JSON en la API. Esto evita depender de un mapeo especial de `List<String>` a tipos JSON nativos del driver.


## Inventario y compras

La creación de un pedido descuenta el inventario en la misma transacción que registra el pedido y cobra el saldo. Cada producto se bloquea para escritura durante la validación y el descuento; las compras simultáneas no pueden vender unidades por encima del stock disponible. Los `UPDATE productos.stock` se fuerzan con `flush` antes de devolver una respuesta 201.

Para verificar el inventario directamente en MySQL, ejecuta `database/diagnostico_stock_productos.sql`. La columna utilizada es `productos.stock`.


## Inventario y confirmación de compra

`POST /api/pedidos` valida cada línea del carrito contra el inventario actual, bloquea los registros de producto para escritura (en orden de ID), descuenta las unidades dentro de la misma transacción y fuerza el `flush` de `productos.stock` antes de responder. Si cualquier línea no tiene unidades suficientes, la compra devuelve HTTP 409 y no registra el pedido ni descuenta saldo. La respuesta 201 incluye `stockActualizado` con el inventario restante por producto.

El carrito también valida stock al añadir y cambiar cantidades. El campo de base de datos es `productos.stock`. Usa `database/diagnostico_stock_productos.sql` desde la base configurada en `DB_URL` para revisar el inventario directamente en MySQL.


## Inicio de sesión con Google (OAuth 2.0)

Este backend implementa el flujo OAuth2 de servidor de Spring Security. El Client Secret se guarda exclusivamente en el backend, nunca en React. La integración es opcional: el backend sigue arrancando con login de contraseña si las credenciales no están definidas.

1. En Google Cloud crea un cliente OAuth de tipo **Web application**.
2. Añade el origen JavaScript `http://localhost:5173` (o el puerto que realmente utilice Vite).
3. Añade exactamente este URI de redirección: `http://localhost:8080/login/oauth2/code/google`.
4. En el backend copia `.env.example` con el nombre `.env` y completa `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` con los valores de Google Cloud. El backend carga ese archivo automáticamente al iniciar Spring Boot. También puedes definir esas variables en el entorno de Windows; por ejemplo en PowerShell:

```powershell
$env:GOOGLE_CLIENT_ID="TU_CLIENT_ID.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET="TU_CLIENT_SECRET"
$env:GOOGLE_REDIRECT_URI="http://localhost:8080/login/oauth2/code/google"
$env:FRONTEND_URL="http://localhost:5173"
.\mvnw.cmd spring-boot:run
```

No escribas el secreto en React, en variables `VITE_*`, en `application.properties` versionado ni en repositorios públicos. El archivo `.env` está excluido de Git y no se incluye en el ZIP.

5. Ejecuta una vez `database/migrate_google_oauth.sql` en la misma base de datos MySQL antes de probar.
6. En Google Auth Platform deja la aplicación en **Testing** y agrega tu Gmail en **Test users** mientras haces las pruebas.
7. Inicia sesión desde el botón **Continuar con Google** del frontend. El backend recibe el callback y vuelve a `/oauth/callback` en React.

Las cuentas nuevas se crean como `Cliente`, con saldo cero y un identificador estable `google_sub`. Si ya existe una cuenta Yakarta con el mismo correo, no se vincula automáticamente: primero inicia sesión con tu contraseña Yakarta y luego vuelve a iniciar el flujo de Google desde esa sesión. Así se evita vincular una cuenta preexistente sin demostrar primero que controlas la cuenta Yakarta. La vinculación conserva rol, saldo y contraseña. Las cuentas existentes siguen pudiendo ingresar con correo y contraseña.

Si no existen ambas credenciales, el backend muestra en el frontend un aviso de que Google todavía no está configurado y el login tradicional continúa funcionando.

## Checkout de cuentas Google

- El pedido se confirma con `POST /api/pedidos` usando la cookie de sesión Yakarta (`credentials: include` desde React).
- El backend valida dirección, inventario y saldo. Las cuentas Cliente pagan con el saldo Yakarta; una cuenta Google recién creada inicia con saldo `0.00`, por lo que debe agregar fondos en `/fondos` antes de comprar.
- `PedidoController` normaliza un saldo histórico NULL a cero, valida líneas de carrito/productos antes de calcular el total y fuerza las escrituras de saldo, stock y pedido dentro de la transacción.
- Los errores inesperados del endpoint de pedidos devuelven un código `reference`; busca `Error de checkout. reference=...` en la consola del backend para encontrar la excepción completa.
- Para revisar cuenta Google, dirección, carrito y columnas SQL sin cambiar datos, ejecuta `database/diagnostico_checkout_google.sql` y reemplaza `CORREO_GOOGLE_AQUI`.

### Compatibilidad con MariaDB en las compras

El inventario se bloquea dentro de la transacción con `SELECT * FROM productos WHERE id = ? FOR UPDATE`.
Esta consulta evita la sintaxis `FOR UPDATE OF alias`, no compatible con MariaDB.
El dialecto JPA también está configurado explícitamente como `org.hibernate.dialect.MariaDBDialect`.
No requiere migración SQL: reinicia el backend para aplicar el cambio.

## Copia de boleta por correo (solo sesión Google)

La sesión recibe `yakartaAuthProvider=GOOGLE` únicamente después de completar el callback OAuth2 y validar el correo de Google. El login tradicional marca `YAKARTA`. `GET /api/auth/me` expone `authProvider` para que React muestre la opción únicamente a sesiones Google; el backend vuelve a validar el marcador de sesión y la propiedad del pedido en `POST /api/pedidos/{id}/boleta/email`. El destinatario siempre se obtiene del correo de la cuenta Google en la base de datos; no se acepta un email escrito por el navegador.

El endpoint recibe el PDF de la boleta como `multipart/form-data` en el campo `receipt`, valida la firma `%PDF-`, limita su tamaño a 8 MB y lo envía como adjunto. React genera el PDF con el logo Yakarta centrado y las miniaturas que se muestran en el detalle del pedido. La dirección de correo no está disponible para cuentas con sesión `YAKARTA`, aunque el usuario haya vinculado Google en otro momento; debe haber entrado a la sesión actual mediante el botón Google.

Para habilitar SMTP añade estas variables al `.env` del backend:

```properties
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=cuenta-emisora@gmail.com
MAIL_PASSWORD=CONTRASENA_DE_APLICACION
MAIL_FROM=cuenta-emisora@gmail.com
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

`MAIL_USERNAME` y `MAIL_PASSWORD` son credenciales de la cuenta emisora, no son el Client ID ni el Client Secret de Google OAuth. Para Gmail utiliza una contraseña de aplicación de la cuenta emisora con verificación en dos pasos. El envío se deja deshabilitado funcionalmente hasta completar esos valores; el resto del backend inicia sin requerir SMTP.

## Miniaturas históricas en las boletas

La entidad `PedidoDetalle` guarda `imagen_producto` como instantánea de la portada cuando se confirma una compra. Hibernate `ddl-auto=update` agrega la columna al iniciar; también puedes ejecutar `database/migrate_receipt_thumbnails.sql` una vez en la base configurada. Para pedidos anteriores, el API recupera la imagen actual del producto como respaldo si la relación aún existe.

## Configuración de correo para boletas (SMTP opcional)

El backend puede iniciar aunque todavía no se haya configurado SMTP. El servicio de boletas obtiene `JavaMailSender` de forma diferida; si falta la configuración, solo el endpoint de envío de correo devolverá un mensaje indicando qué variable revisar.

En el archivo `.env`, ubicado junto a `pom.xml`, configura:

```properties
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-cuenta-emisora@gmail.com
MAIL_PASSWORD=tu-contrasena-de-aplicacion-sin-espacios
MAIL_FROM=tu-cuenta-emisora@gmail.com
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

`pom.xml` ya incluye `spring-boot-starter-mail`. No añadas dependencias duplicadas. Si el backend arranca, pero el envío falla por falta de configuración, verifica que `MAIL_HOST` esté definido y que el `.env` esté en la raíz del proyecto. No compartas el `.env` ni subas credenciales al repositorio.


### Diagnóstico de entrega de boletas

El endpoint devuelve `acceptedBySmtp: true` y un `messageId` únicamente después de que `JavaMailSender.send(...)` finaliza sin excepción. Esto confirma que el servidor SMTP aceptó el mensaje, pero no garantiza que llegue a la bandeja Recibidos: el proveedor puede filtrarlo en Spam/Promociones o generar un rebote posterior. Revisa también la carpeta Enviados de `MAIL_USERNAME`. Para Gmail, `MAIL_FROM` debe coincidir con `MAIL_USERNAME` o ser un alias de envío previamente verificado en esa cuenta. El backend registra el pedido, destinatario y `messageId` (con correos enmascarados), nunca la contraseña.

### Corrección de compilación del remitente de correo

`ReceiptEmailService` ahora captura `UnsupportedEncodingException` al establecer el nombre visible del remitente y la envuelve como `MessagingException`. Esto resuelve el error de compilación de Java en `helper.setFrom(from, "Yakarta Gaming")` sin cambiar la configuración SMTP ni el comportamiento de envío.
