package com.lprgl3a.validator;

import java.util.List;
import java.util.Set;

/**
 * Coeur de l'audit de securite.
 *
 * La methode retenue combine deux approches complementaires, dans l'esprit
 * de zxcvbn mais reimplementee en Java pur (sans dependance externe) :
 *
 *   1. Calcul d'entropie theorique : plus l'alphabet utilise est large et
 *      plus le mot de passe est long, plus l'espace de recherche pour une
 *      attaque par force brute est grand.
 *
 *   2. Detection de motifs predictibles : un mot de passe peut avoir une
 *      entropie theorique elevee tout en etant trivial a deviner (suites
 *      "1234", repetitions "aaaa", dates, mots de dictionnaire courants).
 *      Ces motifs reduisent fortement l'entropie REELLE sans changer
 *      l'entropie THEORIQUE, d'ou la necessite de les penaliser explicitement.
 */
public class PasswordAnalyzer {

    // Petite liste de mots tres communs utilises dans les mots de passe.
    // Volontairement courte : l'objectif pedagogique est de demontrer le
    // principe de detection par dictionnaire, pas de livrer une base
    // exhaustive (ce qui releverait d'un outil comme zxcvbn lui-meme).
    private static final Set<String> COMMON_WORDS = Set.of(
            "password", "motdepasse", "azerty", "qwerty", "123456",
            "soleil", "admin", "bonjour", "welcome", "letmein"
    );

    public record AnalysisResult(double entropyBits, String strength, List<String> warnings) {}

    public AnalysisResult analyze(String password) {
        double entropy = computeEntropyBits(password);
        List<String> warnings = detectWeakPatterns(password);

        // Chaque motif faible detecte penalise l'entropie effective.
        // La penalite est volontairement forte (-15 bits par motif) car un
        // seul motif predictible suffit a rendre une attaque dictionnaire
        // bien plus rapide qu'une attaque brute-force pure.
        double effectiveEntropy = entropy - (warnings.size() * 15.0);

        String strength = classify(effectiveEntropy);
        return new AnalysisResult(Math.round(entropy * 100) / 100.0, strength, warnings);
    }

    /**
     * Entropie de Shannon theorique : log2(taille_alphabet ^ longueur)
     * = longueur * log2(taille_alphabet)
     */
    private double computeEntropyBits(String password) {
        int alphabetSize = 0;
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        if (hasLower) alphabetSize += 26;
        if (hasUpper) alphabetSize += 26;
        if (hasDigit) alphabetSize += 10;
        if (hasSymbol) alphabetSize += 33; // approximation des symboles ASCII imprimables

        if (alphabetSize == 0 || password.isEmpty()) {
            return 0.0;
        }

        return password.length() * (Math.log(alphabetSize) / Math.log(2));
    }

    private List<String> detectWeakPatterns(String password) {
        java.util.ArrayList<String> warnings = new java.util.ArrayList<>();
        String lower = password.toLowerCase();

        if (COMMON_WORDS.stream().anyMatch(lower::contains)) {
            warnings.add("Contient un mot du dictionnaire courant");
        }
        if (hasSequentialChars(lower)) {
            warnings.add("Contient une sequence previsible (ex: 1234, abcd)");
        }
        if (hasRepeatedChars(password)) {
            warnings.add("Contient une repetition de caracteres (ex: aaaa)");
        }
        if (lower.matches(".*(19|20)\\d{2}.*")) {
            warnings.add("Contient une annee plausible (potentielle date de naissance)");
        }

        return warnings;
    }

    private boolean hasSequentialChars(String s) {
        for (int i = 0; i < s.length() - 2; i++) {
            char a = s.charAt(i), b = s.charAt(i + 1), c = s.charAt(i + 2);
            if (b == a + 1 && c == b + 1) return true;
        }
        return false;
    }

    private boolean hasRepeatedChars(String s) {
        for (int i = 0; i < s.length() - 2; i++) {
            if (s.charAt(i) == s.charAt(i + 1) && s.charAt(i + 1) == s.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    private String classify(double effectiveEntropyBits) {
        if (effectiveEntropyBits < 28) return "Tres faible";
        if (effectiveEntropyBits < 36) return "Faible";
        if (effectiveEntropyBits < 60) return "Moyen";
        if (effectiveEntropyBits < 128) return "Fort";
        return "Tres fort";
    }
}
