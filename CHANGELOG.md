# Changelog

All notable changes to this project will be documented in this file.

The format is inspired by Keep a Changelog, and this project uses semantic-ish versioning (major.minor.patch).

## [1.0.1] - Unreleased
### Added
- README: nouvelle section "Versions" avec tags d'images Docker et exemples PowerShell.

### Changed
- Post-v1 rollover: bump des versions applicatives (backend `pom.xml`, frontend `package.json`) à 1.0.1.

## [1.0.0] - 2025-10-24
### Added
- Première release publique (v1.0.0).
- Backend Spring Boot 3.5: API utilisateurs, projets, invitations, rôles, tâches, assignations, historique, notifications (mock).
- Frontend Angular 18: écrans principaux (auth, projets, tâches, board), validations et navigation protégée.
- Tests automatisés: couverture atteinte (back ≥60% lignes/branches, front ≥60% branches).
- Docker: images backend et frontend; docker-compose (MySQL + services).
- CI/CD: GitHub Actions pour build/tests/couverture et push des images sur Docker Hub.

### Notes
- La sécurité avancée (Spring Security) n'est pas incluse pour cette release conformément au brief.
- H2 est utilisé pour les tests; MySQL 8 pour l'exécution locale/compose.

[1.0.1]: https://github.com/your-org/your-repo/releases/tag/v1.0.1
[1.0.0]: https://github.com/your-org/your-repo/releases/tag/v1.0.0
