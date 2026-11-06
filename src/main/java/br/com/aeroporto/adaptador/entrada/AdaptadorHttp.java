package br.com.aeroporto.adaptador.entrada;

import br.com.aeroporto.aplicacao.GerenciarVoos;
import br.com.aeroporto.dominio.Aviao;
import br.com.aeroporto.dominio.Voo;
import br.com.aeroporto.dominio.VooRecusadoException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Adaptador de entrada: traduz requisições HTTP em chamadas à porta de entrada. */
public final class AdaptadorHttp {

    private static final String JSON = "application/json; charset=utf-8";
    private static final String HTML = "text/html; charset=utf-8";
    private static final String PAGINA = "/web/index.html";

    private final GerenciarVoos voos;

    public AdaptadorHttp(GerenciarVoos voos) {
        this.voos = voos;
    }

    public void iniciar(int porta) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", porta), 0);
        servidor.createContext("/", this::atender);
        servidor.start();
        System.out.println("Interface em http://localhost:" + porta + " e API em /voos (Ctrl+C para parar)");
    }

    private void atender(HttpExchange troca) throws IOException {
        int status;
        String corpo;
        String tipo = JSON;
        try {
            String rota = troca.getRequestMethod() + " " + troca.getRequestURI().getPath();
            switch (rota) {
                case "GET /" -> {
                    status = 200;
                    corpo = pagina();
                    tipo = HTML;
                }
                case "GET /voos" -> {
                    status = 200;
                    corpo = lista(voos.listarVoos(), AdaptadorHttp::json);
                }
                case "GET /voos/codigos" -> {
                    status = 200;
                    corpo = lista(voos.listarCodigos(), AdaptadorHttp::texto);
                }
                case "GET /avioes" -> {
                    status = 200;
                    corpo = lista(voos.listarAvioes(), AdaptadorHttp::json);
                }
                case "POST /voos" -> {
                    status = 201;
                    corpo = json(agendar(troca));
                }
                default -> {
                    status = 404;
                    corpo = erro("Rota não encontrada.");
                }
            }
        } catch (VooRecusadoException e) {
            status = 422;
            corpo = erro(e.getMessage());
        } catch (IllegalArgumentException e) {
            status = 400;
            corpo = erro("Requisição inválida: envie codigo, modelo, destino, distancia e passageiros.");
        } catch (RuntimeException e) {
            status = 500;
            corpo = erro("Erro interno.");
        }
        responder(troca, status, tipo, corpo);
    }

    private static String pagina() throws IOException {
        try (InputStream arquivo = AdaptadorHttp.class.getResourceAsStream(PAGINA)) {
            if (arquivo == null) {
                throw new IllegalStateException("Página não encontrada no classpath: " + PAGINA);
            }
            return new String(arquivo.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Voo agendar(HttpExchange troca) throws IOException {
        Map<String, String> formulario = lerFormulario(troca);
        return voos.agendar(
                campo(formulario, "codigo"),
                campo(formulario, "modelo"),
                campo(formulario, "destino"),
                Integer.parseInt(campo(formulario, "distancia")),
                Integer.parseInt(campo(formulario, "passageiros")));
    }

    private static Map<String, String> lerFormulario(HttpExchange troca) throws IOException {
        String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> formulario = new HashMap<>();
        for (String par : corpo.split("&")) {
            String[] chaveEValor = par.split("=", 2);
            if (chaveEValor.length == 2) {
                formulario.put(chaveEValor[0], URLDecoder.decode(chaveEValor[1], StandardCharsets.UTF_8));
            }
        }
        return formulario;
    }

    private static String campo(Map<String, String> formulario, String nome) {
        String valor = formulario.get(nome);
        if (valor == null) {
            throw new IllegalArgumentException("Campo ausente: " + nome);
        }
        return valor;
    }

    private static void responder(HttpExchange troca, int status, String tipo, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", tipo);
        troca.sendResponseHeaders(status, bytes.length);
        try (var saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private static <T> String lista(List<T> itens, Function<T, String> paraJson) {
        return itens.stream().map(paraJson).collect(Collectors.joining(",", "[", "]"));
    }

    private static String json(Voo voo) {
        return "{\"codigo\":" + texto(voo.codigo())
                + ",\"modelo\":" + texto(voo.aviao().nome())
                + ",\"destino\":" + texto(voo.destino())
                + ",\"distanciaKm\":" + voo.distanciaKm()
                + ",\"passageiros\":" + voo.passageiros() + "}";
    }

    private static String json(Aviao aviao) {
        return "{\"codigo\":" + texto(aviao.codigo())
                + ",\"nome\":" + texto(aviao.nome())
                + ",\"capacidade\":" + aviao.capacidade()
                + ",\"alcanceKm\":" + aviao.alcanceKm() + "}";
    }

    private static String erro(String mensagem) {
        return "{\"erro\":" + texto(mensagem) + "}";
    }

    private static String texto(String valor) {
        String seguro = valor.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("\\p{Cntrl}", " ");
        return "\"" + seguro + "\"";
    }
}
