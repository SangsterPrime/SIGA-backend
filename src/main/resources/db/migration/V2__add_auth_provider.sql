-- =====================================================================
-- V2__add_auth_provider.sql  —  Soporte de autenticación manual + Google
--
-- Agrega la columna `provider` para distinguir cuentas LOCAL (registro manual con
-- email + contraseña) de GOOGLE (OAuth). No borra datos: las filas existentes se
-- crearon vía Google OAuth, así que se rellenan con 'GOOGLE'.
-- El rol ya está cubierto por `tipo_usuario` (PASAJERO/FUNCIONARIO/ADMINISTRADOR)
-- y la contraseña por `password_hash` (ya NULLABLE para cuentas Google).
-- =====================================================================

ALTER TABLE usuario ADD COLUMN provider VARCHAR(10);

UPDATE usuario SET provider = 'GOOGLE' WHERE provider IS NULL;

ALTER TABLE usuario ALTER COLUMN provider SET NOT NULL;

ALTER TABLE usuario ADD CONSTRAINT chk_provider CHECK (provider IN ('LOCAL','GOOGLE'));
