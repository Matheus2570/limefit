-- Para bancos que já receberam sql/limefit.sql. Importar depois dele.
-- Não remove linhas, não converte senhas e não adivinha o dono de dados antigos.
USE limefit;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- NULL fica reservado aos registros legados, que não aparecem nas consultas por conta.
ALTER TABLE perfis
    ADD COLUMN IF NOT EXISTS usuario_id BIGINT UNSIGNED NULL,
    ADD UNIQUE INDEX IF NOT EXISTS uq_perfis_usuario (usuario_id),
    ADD UNIQUE INDEX IF NOT EXISTS uq_perfis_id_usuario (id, usuario_id),
    ADD CONSTRAINT fk_perfis_usuario FOREIGN KEY IF NOT EXISTS (usuario_id) REFERENCES usuarios (id);

ALTER TABLE alimentos
    ADD COLUMN IF NOT EXISTS usuario_id BIGINT UNSIGNED NULL,
    DROP INDEX IF EXISTS nome,
    ADD UNIQUE INDEX IF NOT EXISTS uq_alimentos_usuario_nome (usuario_id, nome),
    ADD UNIQUE INDEX IF NOT EXISTS uq_alimentos_id_usuario (id, usuario_id),
    ADD CONSTRAINT fk_alimentos_usuario FOREIGN KEY IF NOT EXISTS (usuario_id) REFERENCES usuarios (id);

ALTER TABLE refeicoes
    ADD COLUMN IF NOT EXISTS usuario_id BIGINT UNSIGNED NULL,
    ADD UNIQUE INDEX IF NOT EXISTS uq_refeicoes_id_usuario (id, usuario_id),
    ADD INDEX IF NOT EXISTS idx_refeicoes_usuario_data (usuario_id, data_plano),
    ADD CONSTRAINT fk_refeicoes_usuario FOREIGN KEY IF NOT EXISTS (usuario_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_refeicoes_perfil_conta FOREIGN KEY IF NOT EXISTS (perfil_id, usuario_id)
        REFERENCES perfis (id, usuario_id) ON DELETE CASCADE;

ALTER TABLE itens_refeicao
    ADD COLUMN IF NOT EXISTS usuario_id BIGINT UNSIGNED NULL,
    ADD CONSTRAINT fk_itens_usuario FOREIGN KEY IF NOT EXISTS (usuario_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_itens_refeicao_conta FOREIGN KEY IF NOT EXISTS (refeicao_id, usuario_id)
        REFERENCES refeicoes (id, usuario_id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_itens_alimento_conta FOREIGN KEY IF NOT EXISTS (alimento_id, usuario_id)
        REFERENCES alimentos (id, usuario_id);

-- As FKs compostas impedem misturar perfil, refeição e alimentos de contas diferentes.
-- Os gatilhos exigem dono nas novas linhas e impedem trocar o dono de linhas já atribuídas.
-- Linhas antigas só podem ser atualizadas após uma associação administrativa explícita.
DELIMITER $$
CREATE OR REPLACE TRIGGER perfis_exigir_dono BEFORE INSERT ON perfis FOR EACH ROW
BEGIN
    IF NEW.usuario_id IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'usuario_id obrigatorio'; END IF;
END$$
CREATE OR REPLACE TRIGGER alimentos_exigir_dono BEFORE INSERT ON alimentos FOR EACH ROW
BEGIN
    IF NEW.usuario_id IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'usuario_id obrigatorio'; END IF;
END$$
CREATE OR REPLACE TRIGGER refeicoes_exigir_dono BEFORE INSERT ON refeicoes FOR EACH ROW
BEGIN
    IF NEW.usuario_id IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'usuario_id obrigatorio'; END IF;
END$$
CREATE OR REPLACE TRIGGER itens_exigir_dono BEFORE INSERT ON itens_refeicao FOR EACH ROW
BEGIN
    IF NEW.usuario_id IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'usuario_id obrigatorio'; END IF;
END$$
CREATE OR REPLACE TRIGGER perfis_preservar_dono BEFORE UPDATE ON perfis FOR EACH ROW
BEGIN
    IF NEW.usuario_id IS NULL OR (OLD.usuario_id IS NOT NULL AND NOT (NEW.usuario_id <=> OLD.usuario_id))
    THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dono obrigatorio e imutavel'; END IF;
END$$
CREATE OR REPLACE TRIGGER alimentos_preservar_dono BEFORE UPDATE ON alimentos FOR EACH ROW
BEGIN
    IF NEW.usuario_id IS NULL OR (OLD.usuario_id IS NOT NULL AND NOT (NEW.usuario_id <=> OLD.usuario_id))
    THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dono obrigatorio e imutavel'; END IF;
END$$
CREATE OR REPLACE TRIGGER refeicoes_preservar_dono BEFORE UPDATE ON refeicoes FOR EACH ROW
BEGIN
    IF NEW.usuario_id IS NULL OR (OLD.usuario_id IS NOT NULL AND NOT (NEW.usuario_id <=> OLD.usuario_id))
    THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dono obrigatorio e imutavel'; END IF;
END$$
CREATE OR REPLACE TRIGGER itens_preservar_dono BEFORE UPDATE ON itens_refeicao FOR EACH ROW
BEGIN
    IF NEW.usuario_id IS NULL OR (OLD.usuario_id IS NOT NULL AND NOT (NEW.usuario_id <=> OLD.usuario_id))
    THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dono obrigatorio e imutavel'; END IF;
END$$
DELIMITER ;
