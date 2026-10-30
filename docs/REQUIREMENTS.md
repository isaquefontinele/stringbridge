# Requirements — StringBridge

## 1. Goal
Detect and prevent localization inconsistencies between iOS and Android projects automatically, and convert translations between their formats.

## 2. Functional requirements

| ID | Requirement |
|---|---|
| FR-01 | Read Android `strings.xml` (strings, plurals, string-arrays, non-translatable). |
| FR-02 | Read and write iOS `.xcstrings`, `.strings` and `.stringsdict`. |
| FR-03 | Report keys missing per locale and per platform. |
| FR-04 | Map differently named keys across platforms via a config file. |
| FR-05 | Detect placeholder mismatches in count, type and position, translating specifiers across platforms. |
| FR-06 | Validate plural categories against CLDR rules per locale. |
| FR-07 | Flag untranslated, empty and badly escaped values. |
| FR-08 | Output terminal, JSON and SARIF reports; emit GitHub Actions annotations. |
| FR-09 | Configurable severities, ignore lists and exit codes for CI gating. |
| FR-10 | Convert between all supported formats. |
| FR-11 | Sync one platform from a source of truth, with dry-run diff. |
| FR-12 | Ship as CLI (JAR, Homebrew) and as a GitHub Action. |

## 3. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-01 | Runs fully offline; no telemetry; never uploads project files anywhere. |
| NFR-02 | Checks a project with 5,000 keys × 20 locales in under 5 seconds on CI hardware. |
| NFR-03 | Writers preserve comments and key order whenever the format allows. |
| NFR-04 | Each check rule is isolated, documented and covered by tests. |
| NFR-05 | Deterministic output (stable ordering) for clean diffs. |
| NFR-06 | Clear, actionable messages that show both platforms' values side by side. |

## 4. Out of scope
- Machine translation or any AI-generated translations.
- Hosting translations or a web dashboard.
- Web/i18next/Flutter ARB formats (possible future plugins).

## 5. Acceptance criteria for v1.0
- All FR implemented; round-trip tests pass for every format.
- GitHub Action used in at least one public sample repository with annotations visible on a PR.
- Documentation covering every rule, config option and exit code.
