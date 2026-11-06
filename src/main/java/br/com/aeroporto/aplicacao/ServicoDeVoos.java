package br.com.aeroporto.aplicacao;

import br.com.aeroporto.dominio.Aviao;
import br.com.aeroporto.dominio.Voo;

import java.util.List;

/** Implementa a porta de entrada e usa a porta de saída. Orquestra; quem decide é o domínio. */
public final class ServicoDeVoos implements GerenciarVoos {

    private final RepositorioDeVoos repositorio;

    public ServicoDeVoos(RepositorioDeVoos repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Voo agendar(String codigo, String modelo, String destino, int distanciaKm, int passageiros) {
        Voo voo = new Voo(codigo, Aviao.porCodigo(modelo), destino, distanciaKm, passageiros);
        voo.verificarCodigoLivre(repositorio.listar());
        repositorio.salvar(voo);
        return voo;
    }

    @Override
    public List<Voo> listarVoos() {
        return repositorio.listar();
    }

    @Override
    public List<String> listarCodigos() {
        return repositorio.listar().stream().map(Voo::codigo).toList();
    }

    @Override
    public List<Aviao> listarAvioes() {
        return List.of(Aviao.values());
    }
}
