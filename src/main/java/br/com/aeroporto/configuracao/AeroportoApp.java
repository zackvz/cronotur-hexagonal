package br.com.aeroporto.configuracao;

import br.com.aeroporto.adaptador.entrada.AdaptadorHttp;
import br.com.aeroporto.adaptador.entrada.AdaptadorTerminal;
import br.com.aeroporto.adaptador.saida.RepositorioEmArquivo;
import br.com.aeroporto.adaptador.saida.RepositorioEmMemoria;
import br.com.aeroporto.aplicacao.GerenciarVoos;
import br.com.aeroporto.aplicacao.RepositorioDeVoos;
import br.com.aeroporto.aplicacao.ServicoDeVoos;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Raiz de composição: o único lugar que conhece todas as peças e as encaixa.
 * Uso: [--http] [--arquivo]
 */
public final class AeroportoApp {

    private AeroportoApp() {
    }

    public static void main(String[] args) throws IOException {
        List<String> opcoes = List.of(args);

        RepositorioDeVoos repositorio = opcoes.contains("--arquivo")
                ? new RepositorioEmArquivo(Path.of("dados", "voos.csv"))
                : new RepositorioEmMemoria();

        GerenciarVoos hexagono = new ServicoDeVoos(repositorio);

        if (opcoes.contains("--http")) {
            new AdaptadorHttp(hexagono).iniciar(8080);
        } else {
            new AdaptadorTerminal(hexagono).iniciar();
        }
    }
}
