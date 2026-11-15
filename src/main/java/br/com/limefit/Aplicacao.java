package br.com.limefit;

import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.StatusBanco;
import br.com.limefit.autenticacao.Sair;
import br.com.limefit.autenticacao.UsuarioDao;
import br.com.limefit.modulos.login.LoginPagina;
import br.com.limefit.modulos.login.CadastroPagina;
import br.com.limefit.modulos.login.CadastroSucessoPagina;
import br.com.limefit.modulos.alimentos.AlimentosPagina;
import br.com.limefit.modulos.inicio.InicioPagina;
import br.com.limefit.modulos.perfil.PerfilPagina;
import br.com.limefit.modulos.planejamento.PlanejamentoPagina;
import br.com.limefit.modulos.projeto.ProjetoPagina;
import br.com.limefit.modulos.refeicoes.RefeicoesPagina;
import br.com.limefit.web.Roteador;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Aplicacao {
    private Aplicacao() {}

    public static void main(String[] args) throws IOException {
        int porta = Integer.parseInt(System.getenv().getOrDefault("LIMEFIT_HTTP_PORT", "8080"));
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", porta), 0);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Conexao conexao = new Conexao();
        servidor.createContext("/", criarRoteador(conexao));
        servidor.setExecutor(executor);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servidor.stop(0);
            executor.shutdownNow();
        }));
        servidor.start();
        System.out.println("LimeFit: http://localhost:" + servidor.getAddress().getPort() + "/");
    }

    // A mesma composição é usada pelo servidor e pelos testes HTTP de integração.
    public static Roteador criarRoteador(Conexao conexao) {
        Roteador roteador = new Roteador();
        roteador.registrar(new InicioPagina());
        roteador.registrar(new PerfilPagina());
        roteador.registrar(new AlimentosPagina());
        roteador.registrar(new RefeicoesPagina());
        roteador.registrar(new PlanejamentoPagina());
        roteador.registrar(new ProjetoPagina());
        UsuarioDao usuarios = new UsuarioDao(conexao);
        roteador.registrar(new LoginPagina(usuarios));
        roteador.registrar(new CadastroPagina(usuarios));
        roteador.registrar(new CadastroSucessoPagina());
        roteador.registrar("/sair", new Sair());
        roteador.registrar("/status/banco", new StatusBanco(conexao));

        return roteador;
    }
}
