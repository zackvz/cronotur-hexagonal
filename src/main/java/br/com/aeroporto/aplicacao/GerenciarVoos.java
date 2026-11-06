package br.com.aeroporto.aplicacao;

import br.com.aeroporto.dominio.Aviao;
import br.com.aeroporto.dominio.Voo;

import java.util.List;

/** Porta de entrada: o que o aeroporto oferece ao mundo externo. */
public interface GerenciarVoos {

    Voo agendar(String codigo, String modelo, String destino, int distanciaKm, int passageiros);

    List<Voo> listarVoos();

    List<String> listarCodigos();

    List<Aviao> listarAvioes();
}
