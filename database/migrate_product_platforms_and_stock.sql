-- Yakarta: etiquetas de plataforma y stock inicial variado para el catálogo predeterminado.
-- Compatible con MariaDB. No elimina ni duplica productos.
--
-- Las plataformas se actualizan cada vez que se ejecuta este script.
-- El stock inicial se aplica SOLO la primera vez gracias a yakarta_migrations,
-- para no sobrescribir ventas posteriores si se vuelve a ejecutar por error.
--
-- Se asigna stock 0 únicamente a GTA VI; los otros 27 juegos reciben entre 1 y 15.

USE yakarta;

CREATE TABLE IF NOT EXISTS yakarta_migrations (
    version VARCHAR(120) NOT NULL PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

START TRANSACTION;

INSERT IGNORE INTO yakarta_migrations (version)
VALUES ('2026-11-23-platforms-stock-seed-v1');

SET @yakarta_apply_initial_stock = IF(ROW_COUNT() = 1, 1, 0);

UPDATE productos
SET
    plataformas = CASE LOWER(TRIM(nombre))
        WHEN 'midnight club' THEN '["PlayStation 3"]'
        WHEN 'fifa street 2' THEN '["PSP"]'
        WHEN 'black' THEN '["PlayStation 2"]'
        WHEN 'burnout 3: takedown' THEN '["PlayStation 2"]'
        WHEN 'dragon ball tenkaichi 3' THEN '["PlayStation 2"]'
        WHEN 'peak' THEN '["PC","PlayStation 5","Xbox Series S/X"]'
        WHEN 'zelda: ocarina of time' THEN '["Nintendo Switch"]'
        WHEN 'the outlast trials' THEN '["PC","PlayStation 4","Xbox Series S/X"]'
        WHEN 'undertale' THEN '["PC","PlayStation 5","Xbox Series S/X","Nintendo Switch"]'
        WHEN 'nfs: shift' THEN '["PC","PlayStation 3"]'
        WHEN 'bordelands 2' THEN '["PC"]'
        WHEN 'warcraft 3: frozen throne' THEN '["PC"]'
        WHEN 'gran turismo 5' THEN '["PlayStation 3"]'
        WHEN 'dead by daylight' THEN '["PC"]'
        WHEN 'transformers: the game' THEN '["PlayStation 2","PlayStation 3"]'
        WHEN 'gran turismo 4' THEN '["PlayStation 2"]'
        WHEN 'detroit' THEN '["PC","PlayStation 4"]'
        WHEN 'red dead redemption 2' THEN '["PC","PlayStation 4","Xbox Series S/X"]'
        WHEN 'papers please' THEN '["PC"]'
        WHEN 'mortal kombat 1' THEN '["PC","PlayStation 5","Xbox Series S/X","Nintendo Switch"]'
        WHEN 'resident evil requiem' THEN '["PC","PlayStation 5","Xbox Series S/X"]'
        WHEN 'pink extinct reprieve 2' THEN '["PC","PlayStation 5","PlayStation 4","PlayStation 3","PSP","Xbox Series S/X","Nintendo Switch"]'
        WHEN 'webfishing' THEN '["PC"]'
        WHEN 'automobilista 2' THEN '["PC"]'
        WHEN 'left 4 dead 2' THEN '["PC"]'
        WHEN 'cyberpunk' THEN '["PC","Nintendo Switch 2"]'
        WHEN 'gta vi' THEN '["PlayStation 5","Xbox Series S/X"]'
        WHEN 'blue alive condemnation 2' THEN '["PC","PlayStation 5","PlayStation 4","PlayStation 3","PlayStation 2","PSP","Xbox Series S/X","Nintendo Switch","Nintendo Switch 2"]'
        ELSE plataformas
    END,
    stock = IF(
        @yakarta_apply_initial_stock = 1,
        CASE LOWER(TRIM(nombre))
        WHEN 'midnight club' THEN 12
        WHEN 'fifa street 2' THEN 5
        WHEN 'black' THEN 9
        WHEN 'burnout 3: takedown' THEN 14
        WHEN 'dragon ball tenkaichi 3' THEN 3
        WHEN 'peak' THEN 8
        WHEN 'zelda: ocarina of time' THEN 11
        WHEN 'the outlast trials' THEN 6
        WHEN 'undertale' THEN 15
        WHEN 'nfs: shift' THEN 4
        WHEN 'bordelands 2' THEN 13
        WHEN 'warcraft 3: frozen throne' THEN 7
        WHEN 'gran turismo 5' THEN 2
        WHEN 'dead by daylight' THEN 10
        WHEN 'transformers: the game' THEN 5
        WHEN 'gran turismo 4' THEN 14
        WHEN 'detroit' THEN 8
        WHEN 'red dead redemption 2' THEN 1
        WHEN 'papers please' THEN 12
        WHEN 'mortal kombat 1' THEN 6
        WHEN 'resident evil requiem' THEN 9
        WHEN 'pink extinct reprieve 2' THEN 3
        WHEN 'webfishing' THEN 11
        WHEN 'automobilista 2' THEN 15
        WHEN 'left 4 dead 2' THEN 4
        WHEN 'cyberpunk' THEN 7
        WHEN 'gta vi' THEN 0
        WHEN 'blue alive condemnation 2' THEN 10
            ELSE stock
        END,
        stock
    )
WHERE LOWER(TRIM(nombre)) IN (
    'midnight club',
    'fifa street 2',
    'black',
    'burnout 3: takedown',
    'dragon ball tenkaichi 3',
    'peak',
    'zelda: ocarina of time',
    'the outlast trials',
    'undertale',
    'nfs: shift',
    'bordelands 2',
    'warcraft 3: frozen throne',
    'gran turismo 5',
    'dead by daylight',
    'transformers: the game',
    'gran turismo 4',
    'detroit',
    'red dead redemption 2',
    'papers please',
    'mortal kombat 1',
    'resident evil requiem',
    'pink extinct reprieve 2',
    'webfishing',
    'automobilista 2',
    'left 4 dead 2',
    'cyberpunk',
    'gta vi',
    'blue alive condemnation 2'
);

COMMIT;

-- Verificación: deben aparecer los 28 productos del catálogo inicial.
SELECT id, nombre, plataformas, stock
FROM productos
WHERE LOWER(TRIM(nombre)) IN (
    'midnight club',
    'fifa street 2',
    'black',
    'burnout 3: takedown',
    'dragon ball tenkaichi 3',
    'peak',
    'zelda: ocarina of time',
    'the outlast trials',
    'undertale',
    'nfs: shift',
    'bordelands 2',
    'warcraft 3: frozen throne',
    'gran turismo 5',
    'dead by daylight',
    'transformers: the game',
    'gran turismo 4',
    'detroit',
    'red dead redemption 2',
    'papers please',
    'mortal kombat 1',
    'resident evil requiem',
    'pink extinct reprieve 2',
    'webfishing',
    'automobilista 2',
    'left 4 dead 2',
    'cyberpunk',
    'gta vi',
    'blue alive condemnation 2'
)
ORDER BY id;

-- Verificación de la regla: exactamente un juego debe quedar con stock = 0.
SELECT
    COUNT(*) AS juegos_del_catalogo,
    SUM(stock = 0) AS juegos_sin_stock,
    MIN(stock) AS stock_minimo,
    MAX(stock) AS stock_maximo
FROM productos
WHERE LOWER(TRIM(nombre)) IN (
    'midnight club',
    'fifa street 2',
    'black',
    'burnout 3: takedown',
    'dragon ball tenkaichi 3',
    'peak',
    'zelda: ocarina of time',
    'the outlast trials',
    'undertale',
    'nfs: shift',
    'bordelands 2',
    'warcraft 3: frozen throne',
    'gran turismo 5',
    'dead by daylight',
    'transformers: the game',
    'gran turismo 4',
    'detroit',
    'red dead redemption 2',
    'papers please',
    'mortal kombat 1',
    'resident evil requiem',
    'pink extinct reprieve 2',
    'webfishing',
    'automobilista 2',
    'left 4 dead 2',
    'cyberpunk',
    'gta vi',
    'blue alive condemnation 2'
);
