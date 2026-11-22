package br.com.limefit.autenticacao;

public final class ValidacaoConta extends Exception {
    private final MensagemConta mensagem;
    public ValidacaoConta(MensagemConta mensagem) { this.mensagem = mensagem; }
    public MensagemConta mensagem() { return mensagem; }
}
