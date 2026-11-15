-- Fixture do esquema ORIGINAL, antes de 001-contas-e-propriedade.sql.
-- Depois da migração use dados vinculados à conta, conforme docs/CONTAS-E-DADOS.md.
-- Este arquivo é usado pela validação da preservação dos registros legados.
USE limefit;
START TRANSACTION;

SET @identificador_teste = SHA2('limefit-perfil-teste-01', 256);
INSERT INTO perfis (identificador, peso_kg, altura_cm, objetivo)
VALUES (@identificador_teste, 80, 180, 'maintain')
ON DUPLICATE KEY UPDATE id = id;
SET @perfil_teste = (SELECT id FROM perfis WHERE identificador = @identificador_teste);

INSERT INTO alimentos (nome, calorias, proteinas, carboidratos, gorduras)
VALUES ('Alimento de teste A', 200, 10, 30, 4), ('Alimento de teste B', 80, 1, 20, 0)
ON DUPLICATE KEY UPDATE id = id;
SET @alimento_a = (SELECT id FROM alimentos WHERE nome = 'Alimento de teste A');
SET @alimento_b = (SELECT id FROM alimentos WHERE nome = 'Alimento de teste B');

SET @data_teste = '2026-01-15';
INSERT INTO refeicoes (perfil_id, data_plano, nome)
SELECT @perfil_teste, @data_teste, 'Refeição de teste'
WHERE NOT EXISTS (
    SELECT 1 FROM refeicoes WHERE perfil_id = @perfil_teste
        AND data_plano = @data_teste AND nome = 'Refeição de teste'
);
SET @refeicao_teste = (SELECT MIN(id) FROM refeicoes WHERE perfil_id = @perfil_teste
    AND data_plano = @data_teste AND nome = 'Refeição de teste');

INSERT INTO itens_refeicao (refeicao_id, alimento_id, quantidade_g)
VALUES (@refeicao_teste, @alimento_a, 150), (@refeicao_teste, @alimento_b, 50)
ON DUPLICATE KEY UPDATE quantidade_g = VALUES(quantidade_g);

COMMIT;
SELECT @perfil_teste AS perfil_id_para_teste, @data_teste AS data_para_teste;
