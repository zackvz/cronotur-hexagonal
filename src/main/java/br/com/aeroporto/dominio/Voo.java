package br.com.aeroporto.dominio;

import java.util.List;

/** Um voo agendado. Toda regra de negócio do sistema está aqui. */
public record Voo(String codigo, Aviao aviao, String destino, int distanciaKm, int passageiros) {

    public Voo {
        codigo = codigo == null ? "" : codigo.trim().toUpperCase();
        destino = destino == null ? "" : destino.trim();

        if (!codigo.matches("[A-Z0-9]{3,8}")) {
            throw new VooRecusadoException("Código de voo inválido: use de 3 a 8 letras ou números.");
        }
        if (destino.isEmpty()) {
            throw new VooRecusadoException("Informe o destino do voo.");
        }
        if (distanciaKm <= 0) {
            throw new VooRecusadoException("A distância precisa ser maior que zero.");
        }
        if (passageiros <= 0) {
            throw new VooRecusadoException("O voo precisa ter ao menos um passageiro.");
        }
        if (passageiros > aviao.capacidade()) {
            throw new VooRecusadoException(aviao.nome() + " leva no máximo "
                    + aviao.capacidade() + " passageiros.");
        }
        if (distanciaKm > aviao.alcanceKm()) {
            throw new VooRecusadoException(aviao.nome() + " alcança no máximo "
                    + aviao.alcanceKm() + " km.");
        }
    }

    public void verificarCodigoLivre(List<Voo> agendados) {
        for (Voo agendado : agendados) {
            if (agendado.codigo.equals(codigo)) {
                throw new VooRecusadoException("Já existe um voo com o código " + codigo + ".");
            }
        }
    }
}
