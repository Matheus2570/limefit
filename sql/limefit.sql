CREATE DATABASE IF NOT EXISTS limefit
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE limefit;

CREATE TABLE IF NOT EXISTS perfis (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    identificador CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    peso_kg DECIMAL(5,2) NOT NULL,
    altura_cm DECIMAL(5,2) NOT NULL,
    objetivo ENUM('mass', 'loss', 'balance', 'maintain') NOT NULL,
    CHECK (peso_kg BETWEEN 25 AND 300),
    CHECK (altura_cm BETWEEN 100 AND 230)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alimentos (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    calorias DECIMAL(6,2) UNSIGNED NOT NULL,
    proteinas DECIMAL(5,2) UNSIGNED NOT NULL,
    carboidratos DECIMAL(5,2) UNSIGNED NOT NULL,
    gorduras DECIMAL(5,2) UNSIGNED NOT NULL,
    CHECK (calorias <= 1000),
    CHECK (proteinas <= 100 AND carboidratos <= 100 AND gorduras <= 100)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS refeicoes (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    perfil_id INT UNSIGNED NOT NULL,
    data_plano DATE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    INDEX idx_refeicoes_perfil_data (perfil_id, data_plano),
    CONSTRAINT fk_refeicoes_perfis FOREIGN KEY (perfil_id) REFERENCES perfis (id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS itens_refeicao (
    refeicao_id INT UNSIGNED NOT NULL,
    alimento_id INT UNSIGNED NOT NULL,
    quantidade_g DECIMAL(7,2) NOT NULL,
    PRIMARY KEY (refeicao_id, alimento_id),
    INDEX idx_itens_alimento (alimento_id),
    CONSTRAINT fk_itens_refeicoes FOREIGN KEY (refeicao_id) REFERENCES refeicoes (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_itens_alimentos FOREIGN KEY (alimento_id) REFERENCES alimentos (id),
    CHECK (quantidade_g > 0 AND quantidade_g <= 10000)
) ENGINE=InnoDB;
