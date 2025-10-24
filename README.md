 # PMT – Project Management Tool

Plateforme de gestion de projet collaborative (Angular 18 + Spring Boot 3.5 + MySQL). Ce README couvre le démarrage rapide, la configuration, l’API, les rôles/permissions, les tests/couverture, et l’industrialisation (Docker, CI/CD).

## Sommaire
- Présentation & fonctionnalités
- Architecture & dossiers
- Prérequis
- Démarrage rapide (Docker Compose / Local)
- Configuration (Backend & Frontend)
- Base de données & seed
- API (endpoints principaux)
- Rôles & permissions
- Tests & couverture
- CI/CD & Docker Hub
- Dépannage

---

## Présentation & fonctionnalités
PMT permet de planifier, suivre et collaborer sur des projets logiciels.

Fonctionnalités livrées (US):
- Inscription et connexion (formulaires, validations, erreurs)
- Création de projets (le créateur devient Administrateur du projet)
- Invitations par e‑mail (mock SMTP/log accepté) et gestion des invitations
- Attribution de rôles par projet (Administrateur, Membre, Observateur; Mainteneur supporté côté back)
- Tâches: création, assignation, mise à jour, suppression, détail
- Tableau de bord par statuts (type kanban)
- Notifications à l’assignation (in‑app + email mock)
- Historique des modifications des tâches (audit trail)

Points clés d’implémentation:
- Backend: Spring Boot 3.5 (Java 21), JPA/Hibernate, validations, services unit‑testés (≥60% lignes & branches, atteint).
- Frontend: Angular 18 standalone, Router, HttpClient, Reactive Forms; tests Karma/Jasmine (≥60% branches, atteint).
- DB: MySQL (prod/dev) / H2 (tests). Script SQL d’initialisation fourni.

---

## Architecture & dossiers
- `src/main/java` – Backend Spring Boot (controllers, services, repositories, DTOs)
- `frontend/` – Frontend Angular 18 (CLI)
- `db/init.sql` – Script SQL (structure + jeux de données)
- `docker-compose.yml` – Exécution orchestrée (API + DB + Front)
- `Dockerfile` – Image backend
- `frontend/Dockerfile` – Image frontend

Flux d’appel principal:
```
Angular (HttpClient) → REST API Spring (/api/...) → Services → JPA/Hibernate → MySQL
```

---

## Prérequis
- JDK 21 (Temurin recommandé)
- Node.js 18+ / npm 9+
- Docker Desktop (si vous utilisez Docker/Compose)
- MySQL 8 (si exécution locale sans Docker)

---

## Démarrage rapide

### Option A — Docker Compose (recommandé)
Windows PowerShell:
```powershell
cd e:\Développement\Dev\pmt\backend
docker compose up --build
```
Par défaut:
- API: http://localhost:8080
- Front: http://localhost:4200
- MySQL: localhost:3306 (utilisateur/mot de passe selon compose)

Arrêt:
```powershell
docker compose down -v
```

### Option B — Exécution locale (dev)
Backend:
```powershell
cd e:\Développement\Dev\pmt\backend
./mvnw.cmd spring-boot:run
```
Configurer la datasource MySQL (voir section Configuration). Un H2 est utilisé uniquement pour les tests.

Frontend:
```powershell
cd e:\Développement\Dev\pmt\backend\frontend
npm ci
npm start
```
Front accessible sur http://localhost:4200 (le proxy/API doit cibler http://localhost:8080).

---

## Configuration

### Backend (variables Spring courantes)
- `SPRING_DATASOURCE_URL` (ex: `jdbc:mysql://localhost:3306/pmt_db?useSSL=false&serverTimezone=UTC`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (ex: `update`)
- `SPRING_JPA_SHOW_SQL` (`true`/`false`)

Démo utilisateur (seed optionnel au démarrage):
- `pmt.demo-user.enabled` (default `true`)
- `pmt.demo-user.email` (default `alice@example.com`)
- `pmt.demo-user.username` (default `Alice`)
- `pmt.demo-user.password` (default `password`)

### Frontend
Le front consomme l’API via `/api/...` (config de base pour être servi derrière le même domaine). Un paramètre optionnel `window.RUNTIME_CONFIG.USER_EMAIL` peut simuler une session (utilisé par `SessionService`) quand aucun stockage local n’est présent.

---

## Base de données & seed
- Script SQL: `db/init.sql` (structure tables + rôles + jeux de données de démo)
- Seed applicatif: `RoleInitializer` crée les rôles manquants au démarrage (Administrateur, Mainteneur, Membre). Le script SQL ajoute aussi Observateur.
- Lors de la création d’un projet, le créateur devient automatiquement « Administrateur » du projet (voir `ProjectService.create`).

---

## API (endpoints principaux)

Authentification (sans sécurité avancée):
- `POST /api/users/register` — body: `{ username, email, password }` → 201 + `{ id, username, email }`
- `POST /api/users/login` — body: `{ email, password }` → 200 + `{ id, username, email }`

Projets:
- `POST /api/projects` — body: `{ name, description?, startDate?, creatorEmail }` → 201 + `ProjectResponse`
- `GET /api/projects` — paramètres pageables (Spring) → 200 + `Page<ProjectResponse>`
- `GET /api/projects/{projectId}` → 200 + `ProjectResponse`
- `POST /api/projects/assign-role` — body: `{ projectId, targetEmail, roleName, requesterEmail }` → 204

Membres de projet:
- `GET /api/projects/{projectId}/members` → 200 + liste des membres
- `DELETE /api/projects/{projectId}/members/{userId}` → 204

Invitations:
- `POST /api/invitations` — body: `{ projectId, requesterEmail, targetEmail }` → 201 + `id`
- `GET /api/invitations?email=...&status=PENDING|ACCEPTED|DECLINED` → 200 + liste
- `POST /api/invitations/{id}/accept?email=...` → 204
- `POST /api/invitations/{id}/decline` → 204

Tâches:
- `POST /api/tasks` — body: `{ projectId, requesterEmail, name, description?, dueDate?, priority? }` → 201 + `TaskResponse`
- `PATCH /api/tasks/update` — body: `{ taskId, projectId, requesterEmail, name?, description?, status?, priority?, dueDate? }` → 200 + `TaskResponse`
- `GET /api/tasks?projectId=...&requesterEmail=...&assigneeEmail?=...` → 200 + liste
- `GET /api/tasks/board?projectId=...&requesterEmail=...` → 200 + board
- `GET /api/tasks/{taskId}?projectId=...&requesterEmail=...` → 200 + `TaskResponse`
- `GET /api/tasks/{taskId}/history?projectId=...&requesterEmail=...` → 200 + historique
- `DELETE /api/tasks/{taskId}?projectId=...&requesterEmail=...` → 204
- `POST /api/tasks/assign` — body: `{ projectId, taskId, assigneeEmail, requesterEmail }` → 204

Notifications:
- `GET /api/notifications?userEmail=...` → 200 + liste
- `POST /api/notifications/{id}/read?userEmail=...` → 204

Codes d’erreurs usuels:
- 400/422: requête invalide (validations)
- 401: identifiants invalides
- 403: permissions insuffisantes
- 404: ressource introuvable (user/projet/tâche)

---

## Rôles & permissions

| Action | Administrateur | Membre | Observateur |
|---|:--:|:--:|:--:|
| Ajouter un membre / attribuer un rôle | X |  |  |
| Créer une tâche | X | X |  |
| Assigner une tâche | X | X |  |
| Mettre à jour une tâche | X | X |  |
| Voir une tâche | X | X | X |
| Voir le tableau de bord | X | X | X |
| Être notifié | X | X | X |
| Voir l’historique | X | X | X |

Remarques:
- Les rôles sont scopés par projet. Pas d’« admin système » global.
- Le créateur d’un projet est automatiquement Administrateur de ce projet.

---

## Tests & couverture

Backend (Maven):
```powershell
cd e:\Développement\Dev\pmt\backend
./mvnw.cmd test
./mvnw.cmd -Pcoverage test
```
Rapport JaCoCo: `target/site/jacoco/index.html` (objectif ≥ 60% lignes et branches, atteint ~78% lignes / ~65% branches).

Frontend (Angular):
```powershell
cd e:\Développement\Dev\pmt\backend\frontend
npm ci
npm test -- --watch=false --browsers=ChromeHeadless --code-coverage
```
Rapport: `frontend/coverage/pmt-frontend/index.html` (objectif ≥ 60% branches, atteint ~66%).

---

## CI/CD & Docker Hub
Pipeline (GitHub Actions) — principes:
- Build backend (JDK 21) + tests + rapport de couverture (profil `coverage`)
- Build frontend + tests + couverture
- Build des images Docker `backend` et `frontend`
- Push vers Docker Hub (sur branches/politiques définies) si les secrets sont présents

Secrets requis (GitHub → Settings → Secrets and variables → Actions):
- `DOCKERHUB_LOGIN_USERNAME`
- `DOCKERHUB_LOGIN_TOKEN`
- `DOCKERHUB_IMAGE_NAMESPACE` (optionnel; défaut = `DOCKERHUB_LOGIN_USERNAME`)

Build Docker local manuel:
```powershell
# Backend
cd e:\...\pmt\backend
docker build -t your-dh-namespace/pmt-backend:local .

# Frontend
cd e:\...\pmt\backend\frontend
docker build -t your-dh-namespace/pmt-frontend:local .
```

---

## Dépannage
- Problème MySQL depuis un conteneur: utiliser `host.docker.internal` (Win/macOS) et vérifier le port 3306.
- JaCoCo & JDK 25: quelques warnings d’instrumentation peuvent apparaître; privilégier JDK 21 en CI pour la couverture.
- Erreur liée au nom de table réservé: la table utilisateurs s’appelle `users`.
- Les rôles sont créés au démarrage si manquants; le script SQL fournit aussi des données de test.

---

Pour toute question, ouvrez une issue/PR. Contributions bienvenues.
