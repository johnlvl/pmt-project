# PMT Backend (Spring Boot 3.5, Java 21)

Backend du projet PMT (Project Management Tool). Application Spring Boot 3.5 (Java 21), JPA/Hibernate, DTOs, H2 en test et MySQL en exécution.

## Sommaire
- Présentation
- Prérequis
- Build & Tests (Maven)
- Exécution locale (Maven)
- Docker (build & run)
- Configuration (variables d’environnement)
- CI/CD (GitHub Actions)
- Couverture de code
- Secrets requis
- Dépannage

## Présentation
- Architecture: controller → service → repository, DTOs aux contrôleurs, validations Bean Validation.
- Persistance: JPA/Hibernate; H2 en tests (profil `test`) et MySQL en runtime.
- Conventions: scoping par `projectId` dans les repositories; vérification de l’appartenance au projet dans les services.
- Notifications: in-app + email (implémentation par défaut `NoopEmailService`).
- Particularité H2: table `users` (évite le mot réservé "user").

## Prérequis
- JDK 21 (Temurin recommandé)
- Maven Wrapper (fourni): `mvnw` / `mvnw.cmd`
- MySQL 8 (pour l’exécution locale non test)

## Build & Tests (Maven)
Exécuter les tests (profil par défaut sans couverture):
```bash
./mvnw test
```
Activer la couverture (profil JaCoCo):
```bash
./mvnw -Pcoverage test
```

## Exécution locale (Maven)
Lancer l’application:
```bash
./mvnw spring-boot:run
```
Configurer la connexion MySQL via `application.properties` ou variables d’environnement (voir section Configuration).

## Docker (build & run)
Construire l’image localement:
```bash
docker build -t pmt-backend:local .
```
Lancer le conteneur:
```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/pmt_db?useSSL=false&serverTimezone=UTC" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="<mot_de_passe>" \
  pmt-backend:local
```
Remarque: `host.docker.internal` permet d’accéder au MySQL local depuis le conteneur sur Windows/macOS.

## Configuration (variables d’environnement)
Principales variables Spring:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (ex: `update`)
- `SPRING_JPA_SHOW_SQL` (`true`/`false`)
Vous pouvez aussi passer des options JVM via `JAVA_OPTS` (ex: `-Xms256m -Xmx512m`).

## CI/CD (GitHub Actions)
Workflow: `.github/workflows/ci.yml`
- Build + tests (JDK 21, cache Maven).
- Couverture (profil `coverage`) sur develop et déclenchement manuel.
- Build & push Docker vers Docker Hub sur `main`/`develop` (si secrets configurés).

## Couverture de code
- Profil `coverage` active JaCoCo. Rapport généré sous `target/site/jacoco/`.
- La CI téléverse le rapport comme artifact lorsque le profil est activé.

## Secrets requis
A créer dans GitHub → Settings → Secrets and variables → Actions:
- `DOCKERHUB_USERNAME`: identifiant Docker Hub
- `DOCKERHUB_TOKEN`: token Docker Hub (droits de push)

## Dépannage
- Erreur H2 liée à `user`: la table JPA est `users` pour éviter le mot réservé.
- JaCoCo et JDK 25: l’agent est derrière un profil; par défaut les tests ne chargent pas l’agent.
- Connexion MySQL depuis Docker: utiliser `host.docker.internal` et vérifier le port 3306.

---
Pour toute question ou évolution (Docker Compose, publication du rapport sous forme de badge, etc.), ouvrez une issue ou une PR.
