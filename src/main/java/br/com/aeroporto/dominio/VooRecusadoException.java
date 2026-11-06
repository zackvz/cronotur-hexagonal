package br.com.aeroporto.dominio;

/** Uma regra de negócio impediu o agendamento do voo. */
public class VooRecusadoException extends RuntimeException {

    public VooRecusadoException(String mensagem) {
        super(mensagem);
    }
}
