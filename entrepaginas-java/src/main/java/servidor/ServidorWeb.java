package servidor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ServidorWeb {
    static final int PORTA = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;
    static final Gson GSON = new GsonBuilder().serializeNulls().create();

    static final List<String> EXTENSOES_SITE = List.of("html", "css", "js", "png", "svg", "ico");

    public static void main(String[] args) throws IOException {
        Path site = Path.of("../entrepaginas-web").toAbsolutePath().normalize();

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PORTA), 0);
        servidor.createContext("/api/", ServidorWeb::tratarApi);
        servidor.createContext("/", troca -> entregarArquivo(troca, site));

        servidor.setExecutor(Executors.newSingleThreadExecutor());
        servidor.start();

        System.out.println("Servidor EntrePáginas ligado em http://localhost:" + PORTA);
        System.out.println("Site: " + site);

        try {
            ConexaoBanco.abrir();
            System.out.println("Conexão com o MySQL OK.");
        } catch (ErroNegocio e) {
            System.out.println(e.getMessage());
        }
    }

    private static void tratarApi(HttpExchange troca) throws IOException {
        troca.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        troca.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        troca.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equals(troca.getRequestMethod())) { responder(troca, 204, null); return; }

        String metodo = troca.getRequestMethod();

        String[] partes = troca.getRequestURI().getPath().substring("/api/".length()).split("/");
        JsonObject corpo = lerCorpo(troca);

        try {
            Object resposta = rotear(ConexaoBanco.abrir(), metodo, partes, corpo);
            responder(troca, 200, resposta);
        } catch (ErroNegocio e) {
            responder(troca, 400, Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            responder(troca, 500, Map.of("erro", "Erro no servidor: " + e.getMessage()));
        }
    }

    private static Object rotear(Connection conn, String metodo, String[] p, JsonObject c) {
        ServicoBiblioteca servico = new ServicoBiblioteca(conn);
        boolean post = "POST".equals(metodo), delete = "DELETE".equals(metodo);

        switch (p[0]) {
            case "estado":
                return servico.estado();
            case "login":
                return servico.login(texto(c, "email"), texto(c, "senha"));
            case "usuarios":
                if (post) return servico.cadastrarCliente(texto(c, "nome"), texto(c, "cpf"), texto(c, "email_usuario"),
                        texto(c, "telefone"), texto(c, "senha"));
                break;
            case "livros":
                if (post) return servico.salvarLivro(inteiroOuNulo(c, "id_livro"), texto(c, "titulo"), texto(c, "isbn"),
                        inteiro(c, "ano_publicacao"), new BigDecimal(c.get("preco_venda").getAsString()),
                        inteiro(c, "id_categoria"), listaTexto(c, "autores"));
                if (delete && p.length > 1) { servico.removerLivro(Integer.parseInt(p[1])); return Map.of("ok", true); }
                break;
            case "exemplares":
                if (post) return servico.cadastrarExemplar(inteiro(c, "id_livro"), texto(c, "finalidade"), inteiro(c, "id_funcionario"));
                if (delete && p.length > 1) { servico.removerExemplar(Integer.parseInt(p[1])); return Map.of("ok", true); }
                break;
            case "emprestimos":
                if (post && p.length == 1)
                    return servico.registrarEmprestimo(texto(c, "cpf"), texto(c, "codigo"), inteiro(c, "id_funcionario"));
                if (post && p.length == 3 && "devolver".equals(p[2]))
                    return servico.devolver(Integer.parseInt(p[1]));
                break;
            case "multas":
                if (post && p.length == 3 && "pagar".equals(p[2])) { servico.pagarMulta(Integer.parseInt(p[1])); return Map.of("ok", true); }
                break;
            case "vendas":
                if (post) {
                    List<Integer> ids = new ArrayList<>();
                    for (JsonElement e : c.getAsJsonArray("ids_livros")) ids.add(e.getAsInt());
                    return servico.registrarVenda(inteiro(c, "id_usuario"), ids, inteiro(c, "id_forma_pagamento"), inteiro(c, "id_funcionario"));
                }
                break;
            default:
        }
        throw new ErroNegocio("Rota não encontrada: " + metodo + " /api/" + String.join("/", p));
    }

    private static JsonObject lerCorpo(HttpExchange troca) throws IOException {
        try (InputStream entrada = troca.getRequestBody()) {
            String texto = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            if (texto.isBlank()) return new JsonObject();
            JsonElement json = JsonParser.parseString(texto);
            return json.isJsonObject() ? json.getAsJsonObject() : new JsonObject();
        }
    }

    private static String texto(JsonObject c, String campo) {
        return c.has(campo) && !c.get(campo).isJsonNull() ? c.get(campo).getAsString() : null;
    }

    private static int inteiro(JsonObject c, String campo) {
        if (!c.has(campo) || c.get(campo).isJsonNull()) throw new ErroNegocio("Campo obrigatório: " + campo);
        return c.get(campo).getAsInt();
    }

    private static Integer inteiroOuNulo(JsonObject c, String campo) {
        return c.has(campo) && !c.get(campo).isJsonNull() ? c.get(campo).getAsInt() : null;
    }

    private static List<String> listaTexto(JsonObject c, String campo) {
        List<String> lista = new ArrayList<>();
        if (c.has(campo) && c.get(campo).isJsonArray()) {
            JsonArray arr = c.getAsJsonArray(campo);
            for (JsonElement e : arr) if (!e.getAsString().isBlank()) lista.add(e.getAsString().trim());
        }
        return lista;
    }

    private static void responder(HttpExchange troca, int codigo, Object dados) throws IOException {
        if (dados == null) { troca.sendResponseHeaders(codigo, -1); troca.close(); return; }
        byte[] bytes = GSON.toJson(dados).getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        troca.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private static final Map<String, String> TIPOS = Map.of(
            "html", "text/html; charset=utf-8", "css", "text/css; charset=utf-8",
            "js", "text/javascript; charset=utf-8", "png", "image/png", "svg", "image/svg+xml",
            "txt", "text/plain; charset=utf-8", "ico", "image/x-icon");

    private static void entregarArquivo(HttpExchange troca, Path site) throws IOException {
        String caminho = troca.getRequestURI().getPath();
        if (caminho.equals("/")) caminho = "/index.html";
        Path arquivo = site.resolve(caminho.substring(1)).normalize();

        String nome = arquivo.getFileName() == null ? "" : arquivo.getFileName().toString();
        String ext = nome.substring(nome.lastIndexOf('.') + 1).toLowerCase();
        if (!arquivo.startsWith(site) || !Files.isRegularFile(arquivo) || !EXTENSOES_SITE.contains(ext)) {
            texto404(troca, "Página não encontrada");
            return;
        }
        byte[] bytes = Files.readAllBytes(arquivo);
        troca.getResponseHeaders().set("Content-Type", TIPOS.getOrDefault(ext, "application/octet-stream"));
        troca.getResponseHeaders().set("Cache-Control", "no-store");
        troca.sendResponseHeaders(200, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private static void texto404(HttpExchange troca, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        troca.sendResponseHeaders(404, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }
}
