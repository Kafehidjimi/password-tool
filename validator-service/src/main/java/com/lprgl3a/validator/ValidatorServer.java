package com.lprgl3a.validator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serveur HTTP minimaliste expose par le conteneur Docker.
 *
 * On utilise volontairement com.sun.net.httpserver.HttpServer plutot qu'un
 * framework comme Spring Boot : ce service n'a qu'un seul endpoint a
 * exposer, et le JDK fournit deja tout le necessaire. Ajouter un framework
 * complet aurait alourdi l'image Docker et la dependance sans benefice reel
 * pour ce cas d'usage simple.
 *
 * Endpoint expose :
 *   POST /audit
 *   Corps : { "password": "monMotDePasse" }
 *   Reponse : { "entropyBits": 42.5, "strength": "Moyen", "warnings": [...] }
 */
public class ValidatorServer {

    private static final PasswordAnalyzer analyzer = new PasswordAnalyzer();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/audit", ValidatorServer::handleAudit);
        server.createContext("/health", exchange -> sendResponse(exchange, 200, "{\"status\":\"ok\"}"));

        server.setExecutor(null); // executeur par defaut, suffisant pour ce service
        server.start();

        System.out.println("Validator service demarre sur le port " + port);
    }

    private static void handleAudit(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Methode non autorisee, utiliser POST\"}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String password = extractPasswordField(body);

        if (password == null || password.isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"Champ 'password' manquant ou vide\"}");
            return;
        }

        PasswordAnalyzer.AnalysisResult result = analyzer.analyze(password);
        String json = toJson(result);
        sendResponse(exchange, 200, json);
    }

    /**
     * Extraction volontairement simple du champ "password" depuis un JSON
     * recu en entree. On evite d'ajouter une dependance de parsing JSON
     * (comme Jackson ou Gson) pour ce besoin tres restreint : une seule
     * cle attendue, sur un objet plat.
     */
    private static String extractPasswordField(String json) {
        var matcher = java.util.regex.Pattern
                .compile("\"password\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
                .matcher(json);
        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return null;
    }

    private static String toJson(PasswordAnalyzer.AnalysisResult result) {
        String warningsJson = result.warnings().stream()
                .map(w -> "\"" + w.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(","));

        return String.format(
                "{\"entropyBits\":%.2f,\"strength\":\"%s\",\"warnings\":[%s]}",
                result.entropyBits(), result.strength(), warningsJson
        );
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
