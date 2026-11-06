package br.com.aeroporto.adaptador.saida;

import br.com.aeroporto.aplicacao.RepositorioDeVoos;
import br.com.aeroporto.dominio.Aviao;
import br.com.aeroporto.dominio.Voo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/** Adaptador de saída: guarda cada voo em uma linha de um arquivo CSV. */
public final class RepositorioEmArquivo implements RepositorioDeVoos {

    private final Path arquivo;

    public RepositorioEmArquivo(Path arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public void salvar(Voo voo) {
        // O destino é texto livre: codificado, ele não pode quebrar o formato do arquivo.
        String linha = String.join(";",
                voo.codigo(),
                voo.aviao().codigo(),
                URLEncoder.encode(voo.destino(), StandardCharsets.UTF_8),
                String.valueOf(voo.distanciaKm()),
                String.valueOf(voo.passageiros()));
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            Files.createDirectories(pasta);
            Files.writeString(arquivo, linha + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Voo> listar() {
        if (!Files.exists(arquivo)) {
            return List.of();
        }
        try {
            return Files.readAllLines(arquivo, StandardCharsets.UTF_8).stream()
                    .map(RepositorioEmArquivo::paraVoo)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Voo paraVoo(String linha) {
        String[] campos = linha.split(";");
        return new Voo(campos[0], Aviao.porCodigo(campos[1]),
                URLDecoder.decode(campos[2], StandardCharsets.UTF_8),
                Integer.parseInt(campos[3]), Integer.parseInt(campos[4]));
    }
}
