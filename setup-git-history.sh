#!/bin/bash
# ============================================================================
# Script de construction de l'historique Git du projet.
# A executer UNE SEULE FOIS, depuis la racine du projet password-tool/
# (la ou se trouve le .gitignore).
#
# Ce script :
#   1. Initialise le depot Git
#   2. Cree une serie de commits logiques, dans l'ordre du developpement reel
#   3. Te laisse ajouter le remote GitHub et pousser a la fin
#
# IMPORTANT : avant de lancer ce script, ouvre le projet dans IntelliJ (ou
# ton IDE) pour qu'il genere le dossier .idea/, PUIS lance le script.
# ============================================================================

set -e  # arrete le script a la premiere erreur

echo "=== Initialisation du depot Git ==="
git init
git config user.name "Kafehidjimi"
git config user.email "aganiemmanuel7@gmail.com"

# ----------------------------------------------------------------------------
# Commit 1 : structure de base du depot
# ----------------------------------------------------------------------------
git add .gitignore README.md
git commit -m "Init: structure du depot, gitignore et README de base

Mise en place du squelette du projet : fichier .gitignore (exclusion des
artefacts de build Maven, conservation du dossier .idea pour verification
de l'environnement) et README initial."

# ----------------------------------------------------------------------------
# Commit 2 : squelette Maven de l'application CLI
# ----------------------------------------------------------------------------
git add cli-app/pom.xml
git commit -m "cli-app: init du module Maven

Creation du module cli-app avec sa configuration Maven (Java 21,
packaging JAR executable). Aucune dependance tierce : l'application
reposera uniquement sur les API natives du JDK."

# ----------------------------------------------------------------------------
# Commit 3 : generateur de mots de passe
# ----------------------------------------------------------------------------
git add cli-app/src/main/java/com/lprgl3a/cli/PasswordGenerator.java
git commit -m "cli-app: implementation du generateur de mots de passe

Ajout de PasswordGenerator avec SecureRandom (generateur cryptographique,
plus adapte qu'un Random standard pour un usage securitaire). Garantie
d'au moins un caractere de chaque type selectionne, puis melange aleatoire
pour eviter tout biais de position."

# ----------------------------------------------------------------------------
# Commit 4 : parsing des arguments CLI
# ----------------------------------------------------------------------------
git add cli-app/src/main/java/com/lprgl3a/cli/CliArguments.java
git commit -m "cli-app: parsing des arguments de ligne de commande

Ajout de CliArguments pour gerer les options --length, --upper/--lower,
--digits, --symbols, --count et --docker-url. Bascule automatique en
mode interactif si aucun argument n'est fourni."

# ----------------------------------------------------------------------------
# Commit 5 : squelette Maven du service de validation
# ----------------------------------------------------------------------------
git add validator-service/pom.xml
git commit -m "validator-service: init du module Maven

Creation du second module Maven, dedie au service de validation qui
tournera dans le conteneur Docker. Meme principe que cli-app : Java 21,
aucune dependance tierce."

# ----------------------------------------------------------------------------
# Commit 6 : analyseur de robustesse
# ----------------------------------------------------------------------------
git add validator-service/src/main/java/com/lprgl3a/validator/PasswordAnalyzer.java
git commit -m "validator-service: implementation de l'analyse de robustesse

Ajout de PasswordAnalyzer combinant deux approches : calcul d'entropie
theorique (longueur x log2(taille alphabet)) et detection de motifs
previsibles (suites, repetitions, annees, mots courants) qui penalisent
l'entropie effective. Classification finale en 5 niveaux de force."

# ----------------------------------------------------------------------------
# Commit 7 : serveur HTTP du service de validation
# ----------------------------------------------------------------------------
git add validator-service/src/main/java/com/lprgl3a/validator/ValidatorServer.java
git commit -m "validator-service: ajout du serveur HTTP

Exposition de l'analyseur via un serveur HTTP natif (HttpServer du JDK,
sans framework). Endpoint POST /audit recevant un mot de passe en JSON
et retournant l'entropie, la force et les avertissements detectes."

# ----------------------------------------------------------------------------
# Commit 8 : conteneurisation
# ----------------------------------------------------------------------------
git add validator-service/Dockerfile docker-compose.yml
git commit -m "docker: conteneurisation du service de validation

Dockerfile multi-stage (build Maven puis image d'execution JRE 21
Eclipse Temurin) pour que le conteneur execute un environnement Java
de bout en bout. Ajout de docker-compose.yml pour simplifier le
lancement du service."

# ----------------------------------------------------------------------------
# Commit 9 : client HTTP vers le conteneur Docker
# ----------------------------------------------------------------------------
git add cli-app/src/main/java/com/lprgl3a/cli/DockerValidatorClient.java
git commit -m "cli-app: client HTTP vers le service de validation Docker

Ajout de DockerValidatorClient (java.net.http.HttpClient) qui interroge
le conteneur Docker pour chaque mot de passe genere. Aucun mecanisme de
secours local : si Docker est indisponible, l'echec est signale
explicitement a l'utilisateur plutot que de calculer un score en local."

# ----------------------------------------------------------------------------
# Commit 10 : point d'entree principal
# ----------------------------------------------------------------------------
git add cli-app/src/main/java/com/lprgl3a/cli/Main.java
git commit -m "cli-app: ajout du point d'entree et du mode interactif

Implementation de Main qui orchestre generation et audit. Ajout du mode
interactif complet (saisies utilisateur) et du mode rafale (generation
et audit de plusieurs mots de passe en une seule execution)."

# ----------------------------------------------------------------------------
# Commit 11 : documentation README finale
# ----------------------------------------------------------------------------
git add README.md
git commit -m "docs: README complet avec guide de demarrage rapide

Redaction du README final : prerequis, commandes de build et de
lancement du conteneur Docker, tableau des options CLI disponibles."

# ----------------------------------------------------------------------------
# Commit 12 : document de synthese
# ----------------------------------------------------------------------------
git add docs/documentation-projet.docx
git commit -m "docs: ajout du document de synthese du projet

Document de 3 pages couvrant l'analyse fonctionnelle, l'analyse
technique (architecture, communication Java/Docker) et le guide
d'installation complet."

echo ""
echo "=== Historique de commits cree avec succes ==="
git log --oneline
echo ""
echo "Prochaines etapes :"
echo "  1. Cree le depot PRIVE sur GitHub (sans README/gitignore auto-genere)"
echo "  2. git remote add origin git@github.com:TON-USER/password-tool.git"
echo "  3. git branch -M main"
echo "  4. git push -u origin main"
echo "  5. Va dans Settings > Collaborators du repo et invite stouvoli@gmail.com"
