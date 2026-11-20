# Yakarta Backend (Spring Boot + MySQL)

Este es el proyecto **backend independiente** de Yakarta. Contiene únicamente la aplicación Java/Spring Boot, endpoints REST, entidades, repositorios, servicios y el script SQL. No incluye código React, Vite ni `package.json`.

## Requisitos
- JDK 17 o superior.
- MySQL 8.x (puedes utilizar MySQL de XAMPP).
- Conexión a Internet la primera vez que Maven descargue dependencias, si no están en caché.

## 1. Crear la base de datos

1. Inicia MySQL en XAMPP.
2. Abre `http://localhost/phpmyadmin`.
3. Selecciona **Importar**.
4. Importa `database/yakarta.sql`.

El script crea la base `yakarta`. Las tablas y los registros iniciales se crean al iniciar Spring Boot.

## 2. Configurar MySQL

Edita `src/main/resources/application.properties`. Valores predeterminados para XAMPP:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/yakarta?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago
spring.datasource.username=root
spring.datasource.password=
```

Si tu instalación utiliza otro usuario, contraseña o puerto, modifica los valores o define `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` como variables de entorno.

## 3. Iniciar Spring Boot

Abre PowerShell en esta carpeta (la que contiene `pom.xml`) y ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

Comprueba el acceso a la base de datos en:

`http://localhost:8080/api/health`

La respuesta esperada incluye `"ok": true` y `"database": "MySQL"`.

## 4. Comunicación con el frontend

El backend permanece separado del proyecto React. Expone endpoints REST bajo `/api`. El frontend debe configurar en su propio `.env`:

```env
VITE_API_URL=http://localhost:8080/api
```

CORS permite los orígenes de Vite `http://localhost:5173`, `http://127.0.0.1:5173` y el preview `4173`. Si cambias el puerto, actualiza `config/CorsConfig.java`.

React no se conecta directamente a MySQL: solo consume esta API. Puedes ejecutar backend y frontend desde dos carpetas independientes y en dos terminales distintas.

## 5. Cuenta inicial

Si la base no contiene usuarios, el inicializador crea:

- Correo: `admin@tienda.com`
- Contraseña inicial: `123`
- Rol: `Administrador`

Cambia esta contraseña antes de exponer el sistema fuera de tu computador.

## Notas
- Agregar fondos es una simulación académica; no procesa pagos reales.
- Los accesos OAuth externos requieren configurar credenciales oficiales.
