# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

StringBridge is an open-source (MIT) CLI and GitHub Action that keeps iOS and Android
translations in sync. It reads Android `strings.xml` and iOS `.xcstrings` / `.strings` /
`.stringsdict`, reports missing keys, placeholder mismatches, missing CLDR plural forms and
untranslated strings, converts between formats, and outputs terminal, JSON and SARIF reports.

Key docs: [README.md](README.md), [ROADMAP.md](ROADMAP.md), [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md)
(functional `FR-xx` and non-functional `NFR-xx` requirements). Work is tracked as GitHub issues
grouped by roadmap milestone.

## Stack

- Kotlin (JVM), JDK 17+ toolchain
- Gradle (Kotlin DSL) with the wrapper and a version catalog in `gradle/libs.versions.toml`
- Clikt for the CLI
- kotlinx.serialization for `.xcstrings` (JSON) and reports
- JDK StAX for XML; a hand-written parser for `.strings`
- CLDR plural rules bundled at build time
- Tests: `kotlin.test` (JUnit 5) + golden files from `samples/`

## Module layout

```
cli/            Clikt commands (check, convert, sync); depends on :core
core/           All logic, no CLI dependencies. Planned packages:
  model/        Platform-neutral model: Key, Locale, Value, Plural, Placeholder
  android/      strings.xml reader/writer
  apple/        .xcstrings, .strings, .stringsdict readers/writers
  placeholders/ Format-specifier parser and cross-platform mapping
  rules/        Check rules (one small, documented, tested class per rule)
  report/       Terminal, JSON, SARIF, GitHub annotations
action/         Dockerfile + action.yml (GitHub Action)
samples/        Synthetic sample projects used in tests (no real app strings)
```

Base package: `dev.stringbridge` (`dev.stringbridge.cli`, `dev.stringbridge.core`).

## Build and test

```bash
./gradlew build                    # compile + test all modules
./gradlew test                     # tests only
./gradlew :core:test --tests '*SomeTest'
./gradlew :cli:run --args="--help" # run the CLI from Gradle
./gradlew :cli:installDist         # install to cli/build/install/stringbridge
./cli/build/install/stringbridge/bin/stringbridge --help
```

## Conventions

- Code, docs, commit messages and issues are in English.
- Dependency and plugin versions live only in `gradle/libs.versions.toml`; never hard-code versions in
  module build files.
- Keep `core` free of CLI and I/O-presentation concerns; `cli` only parses args and wires `core`.
- Each check rule is isolated, documented and covered by tests (NFR-04).
- Output must be deterministic with stable ordering (NFR-05).
- Fully offline: no telemetry, no network calls, never upload project files (NFR-01).
- Sample/test data must be synthetic — never copy strings from real apps.
- Out of scope: machine/AI translation, hosted dashboards, web/Flutter formats.

## Git workflow

- Small, focused commits on `main`; one issue per change. Reference issues in commit messages
  (`Closes #N` on the commit that finishes an issue).
- Don't change the repo's or global git `user.email`.
- Ask before pushing.
