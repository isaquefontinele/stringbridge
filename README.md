# StringBridge

**Keep iOS and Android translations in sync — and catch localization bugs in CI before your users do.**

StringBridge is a command-line tool (and GitHub Action) that reads Android `strings.xml` and iOS `.xcstrings` / `Localizable.strings` / `.stringsdict`, compares them across platforms and languages, reports problems, and converts between formats.

> 🇧🇷 *Ferramenta de CI que sincroniza e valida traduções entre iOS e Android: chaves faltando, placeholders incompatíveis, plurais quebrados e conversão entre formatos.*

> **Status:** early development. See [ROADMAP.md](ROADMAP.md).

## Why

Apps that ship natively on both platforms keep two separate sets of translation files. They drift apart:

- A string exists on Android but was never added on iOS.
- The Portuguese translation uses `%s` where iOS expects `%@`, or swaps argument positions.
- A plural is missing the `many` form required by the target language.
- Someone copied the English text into the French file "for now".

These bugs reach production because nothing checks them automatically. Translation management platforms solve this, but they're paid, external services. StringBridge is a free, local, open-source check that runs in your existing CI.

## Features (planned)

**Check** (`stringbridge check`)
- Missing keys per language, per platform
- Keys present on one platform but not the other (with a mapping file for differently named keys)
- Placeholder mismatches: count, type and position (`%1$s` ↔ `%1$@`, `%d` ↔ `%ld`)
- Plural categories required by CLDR for each locale (e.g. `one`/`other` for pt, `one`/`few`/`many`/`other` for pl)
- Untranslated values (identical to the source language)
- Empty strings, trailing whitespace, unescaped characters (`'` on Android, `"` in `.strings`)
- Exit codes and severity levels for CI gating

**Convert** (`stringbridge convert`)
- `strings.xml` ↔ `.xcstrings` (String Catalog)
- `strings.xml` ↔ `Localizable.strings` + `.stringsdict`
- Placeholder translation between platforms, plurals and string arrays

**Sync** (`stringbridge sync`)
- Use one platform (or a neutral JSON file) as the source of truth and generate the other
- Dry-run diff output

**Reports**
- Human-readable terminal output
- JSON for scripting
- **SARIF** for GitHub code scanning
- GitHub Actions annotations inline on pull requests

## Example

```bash
$ stringbridge check --android app/src/main/res --ios iosApp/Resources/Localizable.xcstrings

✖ pt-BR  checkout_total   placeholder mismatch: Android "%1$s itens" vs iOS "%lld itens"
✖ es     onboarding_title missing on iOS
⚠ fr     settings_privacy untranslated (same as en)
⚠ pt-BR  cart_items       plural: missing category "many" (required by CLDR for pt)

4 problems (2 errors, 2 warnings)
```

GitHub Actions:

```yaml
- uses: isaquefontinele/stringbridge-action@v1
  with:
    android: app/src/main/res
    ios: iosApp/Resources/Localizable.xcstrings
    fail-on: error
```

## Who it's for

Mobile teams and solo developers shipping the same app on iOS and Android in more than one language — including Kotlin Multiplatform projects with native UI on each side.

## Built with

- **Kotlin** (JVM) with **Clikt** for the CLI
- **kotlinx.serialization** for `.xcstrings` (JSON) and reports
- XML parsing via the JDK StAX API; a hand-written parser for `.strings`
- **CLDR plural rules** (Unicode data, bundled at build time)
- Distribution: fat JAR, Homebrew tap, Docker image for the GitHub Action
- Optional: GraalVM native image for faster CI startup
- Tests: `kotlin.test` + golden files from real-world-like sample projects (synthetic strings only)

## Architecture

```
cli/            Clikt commands (check, convert, sync)
core/
  model/        Platform-neutral model: Key, Locale, Value, Plural, Placeholder
  android/      strings.xml reader/writer
  apple/        .xcstrings, .strings, .stringsdict readers/writers
  placeholders/ Format-specifier parser and cross-platform mapping
  rules/        Check rules (each one small, documented and testable)
  report/       Terminal, JSON, SARIF, GitHub annotations
action/         Dockerfile + action.yml
samples/        Synthetic sample projects used in tests
```

## Getting started

Requirements: JDK 17+.

```bash
git clone https://github.com/isaquefontinele/stringbridge.git
cd stringbridge
./gradlew :cli:installDist
./cli/build/install/stringbridge/bin/stringbridge check --android samples/demo/android/res --ios samples/demo/ios/Localizable.xcstrings
```

## Contributing

New check rules are a great first contribution — each rule is one class with tests. See [ROADMAP.md](ROADMAP.md) and [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md).

## License

[MIT](LICENSE) © 2026 Isaque Sales Domingos Fontinele. CLDR data © Unicode, Inc., under the Unicode License.
