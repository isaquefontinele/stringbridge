# Roadmap

Total planned work to v1.0: **~75–95 hours**.

## Milestone 0 — Foundation (~6h)
- [ ] Gradle multi-module (cli, core), Clikt skeleton, version catalog
- [ ] GitHub Actions: build, test, release artifacts
- [ ] Synthetic sample projects (Android + iOS) under `samples/`

## Milestone 1 — Parsers & model (~18h)
- [ ] Platform-neutral model (keys, locales, values, plurals, arrays, comments)
- [ ] Android `strings.xml`: strings, plurals, string-arrays, `translatable="false"`, escaping
- [ ] iOS `.xcstrings` (String Catalog JSON): states, variations, plurals
- [ ] iOS `.strings` + `.stringsdict`
- [ ] Round-trip tests: read → write → read produces identical model

## Milestone 2 — Placeholders (~10h)
- [ ] Parse printf-style specifiers on both platforms (`%s`, `%@`, `%d`, `%ld`, `%lld`, positional `%1$s`)
- [ ] Cross-platform equivalence table
- [ ] Checks: count, type, position mismatches

## Milestone 3 — Check rules (~16h)
- [ ] Missing keys per locale / per platform
- [ ] Key mapping file for differently named keys (`stringbridge.yml`)
- [ ] CLDR plural category requirements per locale
- [ ] Untranslated, empty, whitespace, escaping rules
- [ ] Severity config and ignore lists

## Milestone 4 — Reports & CI (~12h)
- [ ] Terminal output with colors and summary
- [ ] JSON and SARIF reporters
- [ ] GitHub Actions annotations
- [ ] Published GitHub Action (Docker) + usage docs

## Milestone 5 — Convert & sync (~14h)
- [ ] `convert` between all supported formats
- [ ] `sync` from a source of truth with `--dry-run` diff
- [ ] Preserve comments and ordering where possible

## Milestone 6 — Distribution & 1.0 (~8h)
- [ ] Homebrew tap
- [ ] GraalVM native image builds (macOS, Linux)
- [ ] Docs site, CHANGELOG, semantic versioning

## Later
- Gradle plugin and Xcode build-phase script
- Detect unused keys by scanning Kotlin/Swift sources
- Compose Multiplatform resources (`composeResources/values*/strings.xml`) support
- XLIFF import/export for translators
