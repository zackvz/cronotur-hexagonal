package br.com.aeroporto.dominio;

/** Modelos de avião aceitos pelo aeroporto. Capacidade e alcance são valores aproximados. */
public enum Aviao {
    BOEING_737("737", "Boeing 737-800", 189, 5_400),
    BOEING_747("747", "Boeing 747-8", 467, 14_300),
    BOEING_777("777", "Boeing 777-300ER", 396, 13_600),
    BOEING_787("787", "Boeing 787-9 Dreamliner", 296, 14_000),
    AIRBUS_A320("A320", "Airbus A320neo", 194, 6_300),
    AIRBUS_A330("A330", "Airbus A330-300", 300, 11_700),
    AIRBUS_A350("A350", "Airbus A350-900", 325, 15_000),
    AIRBUS_A380("A380", "Airbus A380", 545, 14_800),
    EMBRAER_E195("E195", "Embraer E195-E2", 146, 4_800);

    private final String codigo;
    private final String nome;
    private final int capacidade;
    private final int alcanceKm;

    Aviao(String codigo, String nome, int capacidade, int alcanceKm) {
        this.codigo = codigo;
        this.nome = nome;
        this.capacidade = capacidade;
        this.alcanceKm = alcanceKm;
    }

    public static Aviao porCodigo(String codigo) {
        for (Aviao aviao : values()) {
            if (aviao.codigo.equalsIgnoreCase(codigo)) {
                return aviao;
            }
        }
        throw new VooRecusadoException("Modelo desconhecido: " + codigo);
    }

    public String codigo() {
        return codigo;
    }

    public String nome() {
        return nome;
    }

    public int capacidade() {
        return capacidade;
    }

    public int alcanceKm() {
        return alcanceKm;
    }
}
