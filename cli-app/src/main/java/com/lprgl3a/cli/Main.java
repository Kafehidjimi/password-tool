package com.lprgl3a.cli;

import java.util.List;
import java.util.Scanner;

/**
 * Point d'entree de l'application.
 *
 * Orchestration :
 *   1. Recuperation des options (arguments CLI ou saisie interactive)
 *   2. Generation du/des mot(s) de passe (mode rafale si count > 1)
 *   3. Pour chaque mot de passe genere, audit obligatoire via le conteneur
 *      Docker (aucun calcul de score en local, voir DockerValidatorClient)
 */
public class Main {

    public static void main(String[] args) {
        CliArguments cliArgs = CliArguments.parse(args);

        PasswordGenerator.Options options;
        int count;
        String dockerUrl;

        if (cliArgs.isInteractiveMode()) {
            InteractiveInput input = readInteractiveInput();
            options = input.options();
            count = input.count();
            dockerUrl = input.dockerUrl();
        } else {
            options = cliArgs.toGeneratorOptions();
            count = cliArgs.getCount();
            dockerUrl = cliArgs.getDockerUrl();
        }

        PasswordGenerator generator = new PasswordGenerator();
        DockerValidatorClient validatorClient = new DockerValidatorClient(dockerUrl);

        System.out.println();
        System.out.println("=== Generation de " + count + " mot(s) de passe ===");
        System.out.println();

        for (int i = 1; i <= count; i++) {
            String password = generator.generate(options);
            System.out.printf("[%d] %s%n", i, password);

            try {
                DockerValidatorClient.AuditResult audit = validatorClient.audit(password);
                System.out.printf("    Entropie estimee : %.2f bits%n", audit.entropyBits());
                System.out.printf("    Force : %s%n", audit.strength());
                if (!audit.warnings().isEmpty()) {
                    System.out.println("    Avertissements :");
                    for (String w : audit.warnings()) {
                        System.out.println("      - " + w);
                    }
                }
            } catch (DockerValidatorClient.DockerUnavailableException e) {
                // Conformement a la consigne, on ne calcule PAS de score de
                // secours en local : on signale clairement l'echec a
                // l'utilisateur et on arrete le programme.
                System.err.println();
                System.err.println("ERREUR : " + e.getMessage());
                System.err.println("L'audit de securite necessite le conteneur Docker actif.");
                System.exit(1);
            }
            System.out.println();
        }
    }

    private record InteractiveInput(PasswordGenerator.Options options, int count, String dockerUrl) {}

    private static InteractiveInput readInteractiveInput() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Mode interactif - Generateur de mots de passe ===");
        System.out.println();

        int length = askInt(scanner, "Longueur du mot de passe", 16);
        boolean lower = askBoolean(scanner, "Inclure les minuscules ?", true);
        boolean upper = askBoolean(scanner, "Inclure les majuscules ?", true);
        boolean digits = askBoolean(scanner, "Inclure les chiffres ?", true);
        boolean symbols = askBoolean(scanner, "Inclure les symboles ?", false);
        int count = askInt(scanner, "Combre de mots de passe a generer (mode rafale)", 1);
        System.out.print("Adresse du service Docker [http://localhost:8080] : ");
        String url = scanner.nextLine().trim();
        if (url.isEmpty()) url = "http://localhost:8080";

        PasswordGenerator.Options options = new PasswordGenerator.Options(length, lower, upper, digits, symbols);
        return new InteractiveInput(options, count, url);
    }

    private static int askInt(Scanner scanner, String prompt, int defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "] : ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Valeur invalide, utilisation de la valeur par defaut.");
            return defaultValue;
        }
    }

    private static boolean askBoolean(Scanner scanner, String prompt, boolean defaultValue) {
        String defaultLabel = defaultValue ? "O/n" : "o/N";
        System.out.print(prompt + " [" + defaultLabel + "] : ");
        String line = scanner.nextLine().trim().toLowerCase();
        if (line.isEmpty()) return defaultValue;
        return line.startsWith("o") || line.startsWith("y");
    }
}
