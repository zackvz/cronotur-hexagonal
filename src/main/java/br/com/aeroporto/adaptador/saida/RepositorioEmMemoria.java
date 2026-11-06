package br.com.aeroporto.adaptador.saida;

import br.com.aeroporto.aplicacao.RepositorioDeVoos;
import br.com.aeroporto.dominio.Voo;

import java.util.ArrayList;
import java.util.List;

/** Adaptador de saída: guarda os voos em memória. Os dados somem ao encerrar. */
public final class RepositorioEmMemoria implements RepositorioDeVoos {

    private final List<Voo> voos = new ArrayList<>();

    @Override
    public void salvar(Voo voo) {
        voos.add(voo);
    }

    @Override
    public List<Voo> listar() {
        return List.copyOf(voos);
    }
}
