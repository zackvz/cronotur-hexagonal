package br.com.aeroporto.aplicacao;

import br.com.aeroporto.dominio.Voo;

import java.util.List;

/** Porta de saída: o que o aeroporto precisa do mundo externo. */
public interface RepositorioDeVoos {

    void salvar(Voo voo);

    List<Voo> listar();
}
