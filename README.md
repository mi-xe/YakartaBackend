# Yakarta Backend (Spring Boot + MySQL)

Este proyecto es el **backend independiente** de Yakarta. Expone una API REST bajo `/api` y es el único proyecto que se conecta directamente a MySQL. El frontend React se entrega y ejecuta por separado.

## Requisitos

- Java 17 o superior.
- MySQL 8 o MariaDB compatible.
- El wrapper Maven incluido (`mvnw` / `mvnw.cmd`).

## Configurar MySQL

1. Inicia MySQL (por ejemplo, MySQL de XAMPP).
2. Ejecuta `database/yakarta.sql` en phpMyAdmin o MySQL. Crea la base `yakarta` y la tabla `usuarios` si aún no existen; no borra cuentas.
3. Si estás reutilizando la base creada por el backend PHP antiguo, ejecuta también `database/migrate_legacy_php.sql` una sola vez. Normaliza las columnas `rol` y `saldo`, crea/migra el carrito y conserva los usuarios.
4. Configura usuario y contraseña mediante variables de entorno, o usa los valores locales predeterminados:

```properties
DB_URL=jdbc:mysql://localhost:3306/yakarta?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago
DB_USERNAME=root
DB_PASSWORD=
```

Las variables corresponden a `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. En XAMPP normalmente el usuario es `root` y la contraseña queda vacía; si tu MySQL usa otro puerto o contraseña, actualiza esas variables. Spring Boot crea/actualiza las demás tablas con JPA y carga las categorías y el catálogo de muestra cuando corresponde.

### Base de datos que venía del antiguo backend PHP

Si estás reutilizando la base `yakarta` que se usaba con el backend PHP anterior, conserva sus tablas y datos. Al iniciar sesión, el backend nuevo reconoce las contraseñas antiguas guardadas en texto plano o mediante `password_hash()` de PHP (bcrypt) y las reemplaza automáticamente por hashes PBKDF2. Las cuentas creadas desde el frontend o desde el panel administrativo usan PBKDF2 desde el primer momento.

La semilla SQL antigua del proyecto frontend incluía estas cuentas administrativas (solo estarán disponibles si ya existen en tu base): `admin@yakarta.com` / `trescuatro` y `mixin@yakarta.com` / `mixin2`. Después de iniciar sesión, cambia esas contraseñas desde una herramienta administrativa.

Al iniciar, el backend también copia los artículos de `carritos` / `carrito_detalles` al nuevo almacenamiento `carrito_items`. La migración es idempotente. También se conservan las columnas antiguas `pedidos.usuario_nombre`, `pedidos.usuario_email`, `pedido_detalles.nombre` y `pedido_detalles.precio` para que los pedidos existentes sigan siendo legibles y los nuevos pedidos se guarden correctamente.

El script `database/migrate_legacy_php.sql` normaliza `rol` y `saldo` para que coincidan con la entidad Java y también permite copiar los carritos antiguos. El backend ejecuta además una copia automática del carrito al iniciar; ambas operaciones usan inserciones idempotentes. Haz una copia de seguridad de MySQL antes de migrar una base de datos existente.

## Iniciar el backend

En Windows PowerShell, desde la raíz de esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw spring-boot:run
```

La API queda disponible normalmente en `http://localhost:8080/api`. Comprueba `http://localhost:8080/api/health`: debe mostrar `ok: true`, `database: "yakarta"`, `usersTableReady: true` y `usersCount` con la cantidad de cuentas almacenadas. Si devuelve error o `usersTableReady: false`, el registro no debe intentarse hasta corregir la conexión o ejecutar el SQL. Abre la documentación Swagger en `http://localhost:8080/swagger-ui/index.html`.

## Conectar el frontend independiente

En la carpeta del frontend, deja `VITE_API_URL=` vacío para que React use automáticamente el mismo hostname con el puerto `8080`. Si el backend está en otra máquina, configura su URL completa. Inicia React por separado con `npm install` y `npm run dev`. El backend admite los puertos locales variables de Vite en `localhost`, `127.0.0.1` y `::1`, con cookies de sesión y credenciales.

## Cuentas y permisos

- El registro público envía `POST /api/auth/registro`; el backend valida el correo, aplica PBKDF2 y usa `saveAndFlush` para confirmar el INSERT en MySQL antes de responder HTTP 201.
- `GET /api/health` verifica la conexión real a la base seleccionada y la existencia de `usuarios`, e informa la cantidad de cuentas que ve el backend.
- El registro público crea usuarios con rol `Cliente`.
- El administrador puede crear usuarios desde `/api/usuarios`; esas contraseñas también se guardan con PBKDF2.
- Las cuentas heredadas de la base anterior se pueden seguir usando con su contraseña actual; tras el primer inicio de sesión se migran al formato nuevo.
- Los endpoints de administración de usuarios, productos, categorías y pedidos requieren una sesión con rol `Administrador`.

## Productos y boletas

`GET /api/productos` consulta el catálogo persistido en MySQL. El administrador puede crear, modificar y eliminar productos desde la API; el frontend consulta ese mismo endpoint para mostrar la tienda. La imagen puede ser una ruta pública del frontend (por ejemplo `imgtienda/juego.jpg`) o una URL `https://...`.

Los pedidos se almacenan en `pedidos` y `pedido_detalles`. La API devuelve el detalle y dirección asociados; el frontend independiente permite descargar la boleta como PDF e imprimirla desde la sección **Mis Compras**.

## Verificar que una cuenta llegó a MySQL

Después de registrar una cuenta desde React, abre phpMyAdmin, selecciona `yakarta` y ejecuta:

```sql
SELECT DATABASE();
SELECT id, nombre, email, rol, saldo
FROM usuarios
ORDER BY id DESC;
```

También puedes consultar `http://localhost:8080/api/health`. El valor `usersCount` debe aumentar tras cada registro exitoso. No verás la contraseña en esa consulta porque nunca se almacena en texto plano.

## Comandos de verificación

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

**Importante:** para ejecutar Yakarta utiliza este backend Spring Boot en el puerto 8080. No levantes a la vez el servidor PHP antiguo en ese puerto; el frontend independiente ya no envía las solicitudes a `backend/api.php`.
