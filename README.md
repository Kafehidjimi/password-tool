# Generateur et auditeur de mots de passe (LPRGL3A)

Outil CLI Java 21 pour generer des mots de passe robustes et auditer leur
solidite via un service de validation Java conteneurise.

## Prerequis

- Java 21 (JDK)
- Maven 3.9+
- Docker et Docker Compose

## Demarrage rapide

### 1. Lancer le service de validation (Docker)

```bash
docker compose up --build -d
```

Verifie que le service repond :

```bash
curl http://localhost:8080/health
# {"status":"ok"}
```

### 2. Compiler l'application CLI

```bash
cd cli-app
mvn clean package
```

### 3. Executer l'application

Mode interactif (sans argument) :

```bash
java -jar target/cli-app.jar
```

Mode ligne de commande :

```bash
java -jar target/cli-app.jar --length=20 --upper --lower --digits --symbols --count=5
```

## Options disponibles

| Option              | Description                                      | Defaut                  |
|----------------------|---------------------------------------------------|--------------------------|
| `--length=N`         | Longueur du mot de passe                          | 16                       |
| `--lower/--no-lower` | Inclure/exclure les minuscules                     | inclus                   |
| `--upper/--no-upper` | Inclure/exclure les majuscules                     | inclus                   |
| `--digits/--no-digits` | Inclure/exclure les chiffres                     | inclus                   |
| `--symbols`          | Inclure les symboles                               | exclus                   |
| `--count=N`          | Nombre de mots de passe a generer (mode rafale)    | 1                        |
| `--docker-url=URL`   | Adresse du service de validation                   | http://localhost:8080   |
| `--help`             | Affiche l'aide                                     | -                        |

## Arret du service Docker

```bash
docker compose down
```

## Documentation complete

Voir `docs/documentation-projet.docx` pour l'analyse fonctionnelle, technique
et le guide d'installation detaille.
