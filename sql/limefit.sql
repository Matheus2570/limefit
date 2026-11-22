-- Instalação nova: este arquivo é completo; NÃO precisa executar a migração 001 depois.
-- Banco existente já migrado: não precisa executar nenhum DDL para a versão Swing.
CREATE DATABASE IF NOT EXISTS limefit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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

-- usuario_id NULL é reservado a registros legados da migração 001.
-- Os gatilhos abaixo exigem proprietário nas novas linhas e impedem a troca de dono.
CREATE TABLE IF NOT EXISTS perfis (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    identificador CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    peso_kg DECIMAL(5,2) NOT NULL,
    altura_cm DECIMAL(5,2) NOT NULL,
    objetivo ENUM('mass', 'loss', 'balance', 'maintain') NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    UNIQUE KEY uq_perfis_usuario (usuario_id),
    UNIQUE KEY uq_perfis_id_usuario (id, usuario_id),
    CONSTRAINT fk_perfis_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CHECK (peso_kg BETWEEN 25 AND 300),
    CHECK (altura_cm BETWEEN 100 AND 230)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS alimentos (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    calorias DECIMAL(6,2) UNSIGNED NOT NULL,
    proteinas DECIMAL(5,2) UNSIGNED NOT NULL,
    carboidratos DECIMAL(5,2) UNSIGNED NOT NULL,
    gorduras DECIMAL(5,2) UNSIGNED NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    UNIQUE KEY uq_alimentos_usuario_nome (usuario_id, nome),
    UNIQUE KEY uq_alimentos_id_usuario (id, usuario_id),
    CONSTRAINT fk_alimentos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CHECK (calorias <= 1000),
    CHECK (proteinas <= 100 AND carboidratos <= 100 AND gorduras <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refeicoes (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    perfil_id INT UNSIGNED NOT NULL,
    data_plano DATE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    UNIQUE KEY uq_refeicoes_id_usuario (id, usuario_id),
    INDEX idx_refeicoes_perfil_data (perfil_id, data_plano),
    INDEX idx_refeicoes_usuario_data (usuario_id, data_plano),
    CONSTRAINT fk_refeicoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_refeicoes_perfis FOREIGN KEY (perfil_id) REFERENCES perfis(id) ON DELETE CASCADE,
    CONSTRAINT fk_refeicoes_perfil_conta FOREIGN KEY (perfil_id, usuario_id)
        REFERENCES perfis(id, usuario_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS itens_refeicao (
    refeicao_id INT UNSIGNED NOT NULL,
    alimento_id INT UNSIGNED NOT NULL,
    quantidade_g DECIMAL(7,2) NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    PRIMARY KEY (refeicao_id, alimento_id),
    INDEX idx_itens_alimento (alimento_id),
    CONSTRAINT fk_itens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_itens_refeicoes FOREIGN KEY (refeicao_id) REFERENCES refeicoes(id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_alimentos FOREIGN KEY (alimento_id) REFERENCES alimentos(id),
    CONSTRAINT fk_itens_refeicao_conta FOREIGN KEY (refeicao_id, usuario_id)
        REFERENCES refeicoes(id, usuario_id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_alimento_conta FOREIGN KEY (alimento_id, usuario_id)
        REFERENCES alimentos(id, usuario_id),
    CHECK (quantidade_g > 0 AND quantidade_g <= 10000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
