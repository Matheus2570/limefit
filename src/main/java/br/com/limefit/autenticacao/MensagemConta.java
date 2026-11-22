package br.com.limefit.autenticacao;

import br.com.limefit.desktop.Idioma;

public enum MensagemConta {
    DADOS("Informe seu nome (até 120 caracteres) e um e-mail válido.", "Enter your name (up to 120 characters) and a valid email."),
    EMAIL("Informe um e-mail válido e sua senha.", "Enter a valid email and your password."),
    SENHA("A senha deve ter de 8 a 128 caracteres.", "Your password must have 8 to 128 characters."),
    CONFIRMACAO("As senhas não coincidem.", "The passwords do not match."),
    DUPLICADO("Este e-mail já está cadastrado.", "This email is already registered."),
    CREDENCIAIS("E-mail ou senha incorretos.", "Incorrect email or password."),
    BANCO("Não foi possível acessar o banco. Inicie o MySQL no XAMPP e confira a conexão.", "Could not access the database. Start MySQL in XAMPP and check the connection."),
    FALHA("Não foi possível concluir. Tente novamente.", "Could not complete the operation. Please try again.");

    private final String pt, en;
    MensagemConta(String pt, String en) { this.pt = pt; this.en = en; }
    public String texto(Idioma idioma) { return idioma.escolher(pt, en); }
}
