package com.lprgl3a.cli;

/**
 * Analyse des arguments passes en ligne de commande.
 *
 * Format attendu :
 *   --length=16 --upper --lower --digits --symbols --count=3 --docker-url=http://localhost:8080
 *
 * Si aucun argument n'est fourni, le programme bascule en mode interactif
 * (saisies utilisateur), conformement a l'exigence "arguments OU saisies
 * utilisateur" du cahier des charges.
 */
public class CliArguments {

    private int length = 16;
    private boolean useLower = true;
    private boolean useUpper = true;
    private boolean useDigits = true;
    private boolean useSymbols = false;
    private int count = 1;
    private String dockerUrl = "http://localhost:8080";
    private boolean interactiveMode = false;

    public static CliArguments parse(String[] args) {
        CliArguments result = new CliArguments();

        if (args.length == 0) {
            result.interactiveMode = true;
            return result;
        }

        for (String arg : args) {
            if (arg.startsWith("--length=")) {
                result.length = Integer.parseInt(arg.substring("--length=".length()));
            } else if (arg.equals("--lower")) {
                result.useLower = true;
            } else if (arg.equals("--no-lower")) {
                result.useLower = false;
            } else if (arg.equals("--upper")) {
                result.useUpper = true;
            } else if (arg.equals("--no-upper")) {
                result.useUpper = false;
            } else if (arg.equals("--digits")) {
                result.useDigits = true;
            } else if (arg.equals("--no-digits")) {
                result.useDigits = false;
            } else if (arg.equals("--symbols")) {
                result.useSymbols = true;
            } else if (arg.startsWith("--count=")) {
                result.count = Integer.parseInt(arg.substring("--count=".length()));
            } else if (arg.startsWith("--docker-url=")) {
                result.dockerUrl = arg.substring("--docker-url=".length());
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printHelp();
                System.exit(0);
            }
        }

        return result;
    }

    private static void printHelp() {
        System.out.println("""
                Generateur et auditeur de mots de passe - LPRGL3A

                Usage :
                  java -jar cli-app.jar [options]

                Options :
                  --length=N         Longueur du mot de passe (defaut: 16)
                  --lower / --no-lower     Inclure / exclure les minuscules (defaut: inclus)
                  --upper / --no-upper     Inclure / exclure les majuscules (defaut: inclus)
                  --digits / --no-digits   Inclure / exclure les chiffres (defaut: inclus)
                  --symbols                Inclure les symboles (defaut: exclus)
                  --count=N           Nombre de mots de passe a generer en mode rafale (defaut: 1)
                  --docker-url=URL    Adresse du service de validation Docker (defaut: http://localhost:8080)

                Sans argument : mode interactif avec saisies utilisateur.
                """);
    }

    public PasswordGenerator.Options toGeneratorOptions() {
        return new PasswordGenerator.Options(length, useLower, useUpper, useDigits, useSymbols);
    }

    public int getCount() { return count; }
    public String getDockerUrl() { return dockerUrl; }
    public boolean isInteractiveMode() { return interactiveMode; }
}
