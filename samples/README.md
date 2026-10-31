# Samples

Synthetic sample projects used by tests and examples. All strings are invented for this
repository; never copy strings from real apps into this folder.

## `demo/`

A tiny notes app localized in English (`en`, source) and Brazilian Portuguese (`pt-BR`).

```
demo/
  android/res/values/strings.xml          en
  android/res/values-pt-rBR/strings.xml   pt-BR
  ios/Localizable.xcstrings               en + pt-BR (String Catalog)
```

Each platform has a plain string, a string with placeholders and a plural (`notes_count`).
`app_name` is marked `translatable="false"` on Android.

### Deliberate problems

These are intentional so later checks have something to find. Don't fix them.

| Key | Problem | Where |
|---|---|---|
| `welcome_title` | Missing on iOS (exists on Android in `en` and `pt-BR`) | `ios/Localizable.xcstrings` |
| `sync_status` | Placeholder mismatch: `pt-BR` swaps types (`%1$@` / `%2$lld`) while `en` and Android use a number first and a string second | `ios/Localizable.xcstrings` (`pt-BR`) |
