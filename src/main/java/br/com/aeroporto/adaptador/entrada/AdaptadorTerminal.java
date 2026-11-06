package br.com.aeroporto.adaptador.entrada;

import br.com.aeroporto.aplicacao.GerenciarVoos;
import br.com.aeroporto.dominio.Aviao;
import br.com.aeroporto.dominio.Voo;
import br.com.aeroporto.dominio.VooRecusadoException;

import java.io.Console;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

/** Adaptador de entrada: traduz o que é digitado em chamadas à porta de entrada. */
public final class AdaptadorTerminal {

    private final GerenciarVoos voos;
    private final Scanner entrada = new Scanner(System.in, charsetDoTerminal());

    public AdaptadorTerminal(GerenciarVoos voos) {
        this.voos = voos;
    }

    public void iniciar() {
        System.out.println("=== Aeroporto Hexagonal ===");
        while (true) {
            System.out.println();
            System.out.println("1) Agendar voo   2) Ver voos   3) Ver aviões   4) Ver códigos de voo   0) Sair");
            String opcao = perguntar("Opção: ");
            if (opcao == null || opcao.equals("0")) {
                System.out.println("Boa viagem!");
                return;
            }
            switch (opcao) {
                case "1" -> agendar();
                case "2" -> mostrarVoos();
                case "3" -> mostrarAvioes();
                case "4" -> mostrarCodigos();
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void agendar() {
        try {
            String codigo = perguntar("Código do voo: ");
            String modelo = perguntar("Modelo (ex.: 737, A320, E195): ");
            String destino = perguntar("Destino: ");
            int distanciaKm = Integer.parseInt(perguntar("Distância em km: "));
            int passageiros = Integer.parseInt(perguntar("Passageiros: "));

            Voo voo = voos.agendar(codigo, modelo, destino, distanciaKm, passageiros);
            System.out.println("Agendado: " + descrever(voo));
        } catch (NumberFormatException e) {
            System.out.println("Recusado: distância e passageiros precisam ser números inteiros.");
        } catch (VooRecusadoException e) {
            System.out.println("Recusado: " + e.getMessage());
        }
    }

    private void mostrarVoos() {
        if (voos.listarVoos().isEmpty()) {
            System.out.println("Nenhum voo agendado.");
        }
        voos.listarVoos().forEach(voo -> System.out.println(descrever(voo)));
    }

    private void mostrarCodigos() {
        List<String> codigos = voos.listarCodigos();
        if (codigos.isEmpty()) {
            System.out.println("Nenhum voo agendado.");
        }
        codigos.forEach(System.out::println);
    }

    private void mostrarAvioes() {
        for (Aviao aviao : voos.listarAvioes()) {
            System.out.printf("%-5s %-24s %3d passageiros  %5d km%n",
                    aviao.codigo(), aviao.nome(), aviao.capacidade(), aviao.alcanceKm());
        }
    }

    private String descrever(Voo voo) {
        return voo.codigo() + " | " + voo.aviao().nome() + " | " + voo.destino()
                + " | " + voo.distanciaKm() + " km | " + voo.passageiros() + " passageiros";
    }

    private String perguntar(String pergunta) {
        System.out.print(pergunta);
        return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
    }

    private static Charset charsetDoTerminal() {
        Console console = System.console();
        return console != null ? console.charset() : Charset.defaultCharset();
    }
}
