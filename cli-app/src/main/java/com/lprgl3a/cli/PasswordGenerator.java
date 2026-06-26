package com.lprgl3a.cli;

import java.security.SecureRandom;

/**
 * Generation de mots de passe aleatoires selon des options configurables.
 *
 * On utilise SecureRandom plutot que java.util.Random : pour un outil dont
 * la finalite est la securite, un generateur pseudo-aleatoire non
 * cryptographique (Random) serait predictible et donc inadapte.
 */
public class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?";

    private final SecureRandom random = new SecureRandom();

    public record Options(int length, boolean useLower, boolean useUpper,
                           boolean useDigits, boolean useSymbols) {

        public Options {
            if (length < 1) {
                throw new IllegalArgumentException("La longueur doit etre superieure a 0");
            }
            if (!useLower && !useUpper && !useDigits && !useSymbols) {
                throw new IllegalArgumentException("Au moins un type de caractere doit etre selectionne");
            }
        }
    }

    public String generate(Options options) {
        StringBuilder alphabet = new StringBuilder();
        if (options.useLower()) alphabet.append(LOWER);
        if (options.useUpper()) alphabet.append(UPPER);
        if (options.useDigits()) alphabet.append(DIGITS);
        if (options.useSymbols()) alphabet.append(SYMBOLS);

        StringBuilder password = new StringBuilder(options.length());

        // On garantit au moins un caractere de chaque type selectionne,
        // afin d'eviter le cas (statistiquement rare mais possible) ou un
        // mot de passe long ne contiendrait par hasard aucun chiffre ou
        // symbole malgre l'option activee.
        java.util.List<String> mandatoryPools = new java.util.ArrayList<>();
        if (options.useLower()) mandatoryPools.add(LOWER);
        if (options.useUpper()) mandatoryPools.add(UPPER);
        if (options.useDigits()) mandatoryPools.add(DIGITS);
        if (options.useSymbols()) mandatoryPools.add(SYMBOLS);

        for (String pool : mandatoryPools) {
            if (password.length() < options.length()) {
                password.append(pool.charAt(random.nextInt(pool.length())));
            }
        }

        while (password.length() < options.length()) {
            password.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }

        return shuffle(password.toString());
    }

    private String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
