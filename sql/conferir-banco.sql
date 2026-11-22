-- Seu banco já atende ao desktop. Esta conferência só lê metadados; não altera dados.
USE limefit;
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME, ORDINAL_POSITION;
SHOW CREATE TABLE usuarios;
SHOW CREATE TABLE perfis;
SHOW CREATE TABLE alimentos;
SHOW CREATE TABLE refeicoes;
SHOW CREATE TABLE itens_refeicao;
SHOW TRIGGERS;
