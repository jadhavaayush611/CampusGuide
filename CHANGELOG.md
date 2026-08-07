# Changelog

All notable changes to the CampusGuide project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0-MVP] - 2026-08-06

### Added
- Complete MVP Release Candidate stabilization and code audit.
- Full verification of all core domains: Platform, Academic, Campus, and Personal.
- Atlas AI streaming response workflow orchestrator with thinking timeline visualization.
- Production-grade security configuration, hardened CORS profile, secure request headers, and custom exception boundaries.
- Offline support capabilities, dynamic query caching with TanStack Query, and optimized route prefetching.
- Complete visual styling audit supporting modern aesthetics (glassmorphism, vibrant colors, premium skeletons, and dark/light modes).
- Full suite of 300 passing backend tests covering critical controllers, security authorization configurations, and services.

### Changed
- Standardized package versions in pom.xml and package.json to release v1.0.0-MVP.
- Cleaned up source code repositories, removed redundant logs and development comments.


## [0.2.0] - 2026-07-25

### Added
- Standardized repository configuration with `.editorconfig`, `.gitattributes`, and refined `.gitignore`.
- GitHub Issue templates (`bug_report.md`, `feature_request.md`, `documentation.md`) and Pull Request template (`PULL_REQUEST_TEMPLATE.md`).
- Repository governance files: `CODEOWNERS`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `ROADMAP.md`, `ARCHITECTURE.md`, and `LICENSE`.
- Standardized directory layout structure with `design/`, `scripts/`, and `.github/workflows/`.

### Changed
- Standardized project `README.md` with structured sections and documentation placeholders.
- Cleaned root repository structure by removing obsolete untracked root files.

## [0.1.0] - 2026-07-25

### Added
- Initial CampusGuide platform architecture covering four core domains: Platform, Academic, Campus, and Personal.
- Spring Boot 4.0.6 backend services with MongoDB Atlas persistence.
- React + Vite frontend application structure with Tailwind CSS styling.
- Comprehensive module documentation and API specifications in `docs/`.
