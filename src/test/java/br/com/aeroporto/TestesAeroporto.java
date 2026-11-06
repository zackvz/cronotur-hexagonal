package br.com.aeroporto;

import br.com.aeroporto.adaptador.entrada.AdaptadorHttp;
import br.com.aeroporto.adaptador.saida.RepositorioEmArquivo;
import br.com.aeroporto.adaptador.saida.RepositorioEmMemoria;
import br.com.aeroporto.aplicacao.RepositorioDeVoos;
import br.com.aeroporto.aplicacao.ServicoDeVoos;
import br.com.aeroporto.dominio.Aviao;
import br.com.aeroporto.dominio.Voo;
import br.com.aeroporto.dominio.VooRecusadoException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Os testes são mais um adaptador de entrada: dirigem o hexágono sem terminal nem HTTP. */
public final class TestesAeroporto {

    private static int falhas = 0;

    private interface Caso {
        void executar() throws Exception;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("-- Regras de negócio --");
        caso("agenda um voo válido", () ->
                igual("Airbus A350-900", servico().agendar("la100", "a350", "Lisboa", 7900, 300).aviao().nome()));
        caso("aceita exatamente a capacidade e o alcance", () ->
                servico().agendar("EM195", "E195", "Recife", 4800, 146));
        caso("recusa passageiros acima da capacidade", () ->
                recusa(() -> servico().agendar("EM195", "E195", "Recife", 2000, 147)));
        caso("recusa distância acima do alcance", () ->
                recusa(() -> servico().agendar("LA320", "A320", "Lisboa", 7900, 150)));
        caso("recusa modelo desconhecido", () ->
                recusa(() -> servico().agendar("XX100", "CONCORDE", "Paris", 5000, 90)));
        caso("recusa código inválido e destino vazio", () -> {
            recusa(() -> servico().agendar("L!", "737", "Rio", 400, 100));
            recusa(() -> servico().agendar("LA100", "737", " ", 400, 100));
        });
        caso("recusa valores zerados", () -> {
            recusa(() -> servico().agendar("LA100", "737", "Rio", 0, 100));
            recusa(() -> servico().agendar("LA100", "737", "Rio", 400, 0));
        });
        caso("recusa código de voo repetido, sem salvar de novo", () -> {
            RepositorioEmMemoria repositorio = new RepositorioEmMemoria();
            ServicoDeVoos servico = new ServicoDeVoos(repositorio);
            servico.agendar("LA100", "737", "Rio", 400, 100);
            recusa(() -> servico.agendar("la100", "A380", "Dubai", 12000, 500));
            igual(1, repositorio.listar().size());
        });

        caso("lista os códigos na ordem de agendamento", () -> {
            ServicoDeVoos servico = servico();
            igual(List.of(), servico.listarCodigos());
            servico.agendar("la100", "737", "Rio", 400, 100);
            servico.agendar("GO200", "A320", "Recife", 2100, 180);
            igual(List.of("LA100", "GO200"), servico.listarCodigos());
        });

        System.out.println("-- Adaptadores: os dois repositórios cumprem o mesmo contrato --");
        caso("memória", () -> contrato(new RepositorioEmMemoria()));
        caso("arquivo", () -> {
            Path pasta = Files.createTempDirectory("aeroporto");
            Path arquivo = pasta.resolve("sub").resolve("voos.csv");
            contrato(new RepositorioEmArquivo(arquivo));
            igual(1, new RepositorioEmArquivo(arquivo).listar().size()); // sobrevive a um "reinício"
        });

        System.out.println("-- Adaptador HTTP --");
        caso("serve a interface web em /", () -> {
            int porta;
            try (ServerSocket livre = new ServerSocket(0)) {
                porta = livre.getLocalPort();
            }
            new AdaptadorHttp(servico()).iniciar(porta);
            var conexao = (HttpURLConnection) URI.create("http://localhost:" + porta + "/").toURL().openConnection();
            igual(200, conexao.getResponseCode());
            igual("text/html; charset=utf-8", conexao.getContentType());
        });

        System.out.println("-- Arquitetura: as dependências apontam para dentro --");
        caso("domínio não conhece aplicação, adaptador nem tecnologia", () -> proibe("dominio",
                "br.com.aeroporto.aplicacao", "br.com.aeroporto.adaptador", "br.com.aeroporto.configuracao",
                "java.io", "java.net", "java.nio", "com.sun"));
        caso("aplicação não conhece adaptador nem tecnologia", () -> proibe("aplicacao",
                "br.com.aeroporto.adaptador", "br.com.aeroporto.configuracao",
                "java.io", "java.net", "java.nio", "com.sun"));
        caso("adaptador usa as portas, não o serviço", () -> proibe("adaptador",
                "br.com.aeroporto.configuracao", "br.com.aeroporto.aplicacao.ServicoDeVoos"));

        System.out.println(falhas == 0 ? "\nTodos os testes passaram." : "\n" + falhas + " teste(s) falharam.");
        System.exit(falhas == 0 ? 0 : 1);
    }

    private static ServicoDeVoos servico() {
        return new ServicoDeVoos(new RepositorioEmMemoria());
    }

    private static void contrato(RepositorioDeVoos repositorio) {
        igual(List.of(), repositorio.listar());
        Voo voo = new Voo("LA100", Aviao.BOEING_787, "São Paulo; \"Guarulhos\"", 9000, 250);
        repositorio.salvar(voo);
        igual(List.of(voo), repositorio.listar());
    }

    private static void proibe(String camada, String... proibidos) throws IOException {
        Path raiz = Path.of("src", "main", "java", "br", "com", "aeroporto", camada);
        List<Path> fontes;
        try (Stream<Path> caminhos = Files.walk(raiz)) {
            fontes = caminhos.filter(c -> c.toString().endsWith(".java")).toList();
        }
        if (fontes.isEmpty()) {
            throw new AssertionError("nenhum arquivo encontrado em " + raiz);
        }
        for (Path fonte : fontes) {
            for (String linha : Files.readAllLines(fonte)) {
                for (String proibido : proibidos) {
                    if (linha.startsWith("import " + proibido)) {
                        throw new AssertionError(fonte.getFileName() + " viola a arquitetura: " + linha);
                    }
                }
            }
        }
    }

    private static void recusa(Caso caso) throws Exception {
        try {
            caso.executar();
        } catch (VooRecusadoException esperado) {
            return;
        }
        throw new AssertionError("deveria ter recusado o voo");
    }

    private static void igual(Object esperado, Object obtido) {
        if (!esperado.equals(obtido)) {
            throw new AssertionError("esperado " + esperado + ", obtido " + obtido);
        }
    }

    private static void caso(String nome, Caso caso) {
        try {
            caso.executar();
            System.out.println("[ok]    " + nome);
        } catch (Exception | AssertionError e) {
            falhas++;
            System.out.println("[FALHA] " + nome + ": " + e.getMessage());
        }
    }
}
