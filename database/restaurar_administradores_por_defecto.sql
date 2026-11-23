-- Restaura las dos cuentas administradoras incluidas en el database.sql original de Yakarta.
-- Compatible con MySQL y MariaDB.
--
-- Las contraseñas se almacenan con PBKDF2-HMAC-SHA256, el mismo formato que PasswordUtil
-- verifica en el backend actual. No se guardan contraseñas en texto plano.
--
-- Este script es idempotente: solo inserta una cuenta si su correo no existe.
-- No cambia contraseñas, roles, saldos ni otros datos de cuentas ya existentes.

USE yakarta;

START TRANSACTION;

INSERT INTO usuarios (nombre, email, password, rol, saldo)
SELECT
    'Cristian Orlando',
    'admin@yakarta.com',
    'pbkdf2$160000$e715e3bf102a29cc664c992c9ba1d897$f70d8058f19ce1c84cc6503772a7fc52717d4843ed2df55fbd67a6048cea01c7',
    'Administrador',
    0.00
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE LOWER(email) = LOWER('admin@yakarta.com')
);

INSERT INTO usuarios (nombre, email, password, rol, saldo)
SELECT
    'Mixin 123',
    'mixin@yakarta.com',
    'pbkdf2$160000$adcad95919fb47d43813278d33093971$7e4e2336036329f7916478b696b2fcd4bd42eb0437c6fac2acb4b4395b31e221',
    'Administrador',
    0.00
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE LOWER(email) = LOWER('mixin@yakarta.com')
);

COMMIT;

-- Verificación: deben aparecer ambas cuentas con rol Administrador.
SELECT id, nombre, email, rol, saldo
FROM usuarios
WHERE LOWER(email) IN ('admin@yakarta.com', 'mixin@yakarta.com')
ORDER BY email;
