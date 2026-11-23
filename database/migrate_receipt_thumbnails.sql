-- Conserva una copia de la portada del juego en el momento de la compra.
-- Se puede ejecutar varias veces y es compatible con MariaDB/MySQL.
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pedido_detalles'
      AND COLUMN_NAME = 'imagen_producto'
);
SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE pedido_detalles ADD COLUMN imagen_producto TEXT NULL',
    'SELECT 1'
);
PREPARE receipt_thumbnail_stmt FROM @ddl;
EXECUTE receipt_thumbnail_stmt;
DEALLOCATE PREPARE receipt_thumbnail_stmt;

-- Los pedidos anteriores no necesitan actualizarse manualmente:
-- la API utiliza la imagen actual de productos como respaldo si no hay instantánea.
