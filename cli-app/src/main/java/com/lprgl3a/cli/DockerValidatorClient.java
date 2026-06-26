package com.lprgl3a.cli;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client HTTP vers le service de validation Java qui tourne dans le
 * conteneur Docker.
 *
 * Important : ce client ne contient AUCUN mecanisme de secours local.
 * Si le conteneur Docker n'est pas accessible, l'audit echoue
 * explicitement (DockerUnavailableException). C'est un choix deliberer :
 * la consigne du projet exige que la notation de la robustesse soit
 * realisee par l'outil externe conteneurise, pas par l'application Java
 * elle-meme. Un repli silencieux contournerait cette exigence.
 */
public class DockerValidatorClient {

    private final HttpClient httpClient;
    private final String validatorUrl;

    public DockerValidatorClient(String validatorUrl) {
        this.validatorUrl = validatorUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public record AuditResult(double entropyBits, String strength, List<String> warnings) {}

    public static class DockerUnavailableException extends RuntimeException {
        public DockerUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public AuditResult audit(String password) {
        String requestBody = "{\"password\":\"" + escapeJson(password) + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(validatorUrl + "/audit"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new DockerUnavailableException(
                        "Le service de validation a repondu avec le code " + response.statusCode(), null);
            }

            return parseJson(response.body());

        } catch (IOException | InterruptedException e) {
            throw new DockerUnavailableException(
                    "Impossible de contacter le conteneur Docker a l'adresse " + validatorUrl +
                            ". Verifiez qu'il est demarre (voir le guide d'installation).", e);
        }
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Parsing volontairement simple (regex) puisque la reponse du service
     * suit un format JSON plat et previsible que nous controlons
     * nous-memes (voir ValidatorServer). Ajouter une librairie de parsing
     * JSON complete serait disproportionne pour ce besoin.
     */
    private AuditResult parseJson(String json) {
        double entropy = extractDouble(json, "entropyBits");
        String strength = extractString(json, "strength");
        List<String> warnings = extractWarnings(json);
        return new AuditResult(entropy, strength, warnings);
    }

    private double extractDouble(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*([0-9.]+)").matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : 0.0;
    }

    private String extractString(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : "Inconnu";
    }

    private List<String> extractWarnings(String json) {
        Matcher arrayMatcher = Pattern.compile("\"warnings\"\\s*:\\s*\\[(.*?)]").matcher(json);
        if (!arrayMatcher.find()) return List.of();

        String inner = arrayMatcher.group(1).trim();
        if (inner.isEmpty()) return List.of();

        return List.of(inner.split(",")).stream()
                .map(s -> s.trim().replaceAll("^\"|\"$", ""))
                .toList();
    }
}
