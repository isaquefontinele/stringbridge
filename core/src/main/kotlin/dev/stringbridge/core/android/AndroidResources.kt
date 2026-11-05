package dev.stringbridge.core.android

import dev.stringbridge.core.ParseException
import dev.stringbridge.core.model.LocaleTag
import dev.stringbridge.core.model.LocalizedStrings
import dev.stringbridge.core.model.Platform
import dev.stringbridge.core.model.StringCatalog
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/** Reads an Android `res/` directory into a [StringCatalog]. */
object AndroidResources {

    /**
     * Reads every `*.xml` file in `values/` and in each `values-<locale>/` folder of [resDir].
     * The unqualified `values/` folder holds [sourceLocale]. Folders with other qualifiers
     * (such as `values-night` or `values-v21`) are ignored.
     */
    fun read(resDir: Path, sourceLocale: LocaleTag): StringCatalog {
        require(resDir.isDirectory()) { "Not a directory: $resDir" }
        val localizations = resDir.listDirectoryEntries("values*")
            .filter { it.isDirectory() }
            .mapNotNull { dir -> localeOf(dir.name, sourceLocale)?.let { readLocale(dir, it) } }
        return StringCatalog(Platform.ANDROID, sourceLocale, localizations)
    }

    /**
     * Returns the locale of a resource folder name, [sourceLocale] for plain `values`, or `null`
     * if the folder isn't a values folder qualified only by a locale.
     * Supports `values-pt`, `values-pt-rBR` and BCP 47 `values-b+sr+Latn`.
     */
    fun localeOf(folderName: String, sourceLocale: LocaleTag): LocaleTag? {
        if (folderName == "values") return sourceLocale
        val qualifiers = folderName.removePrefix("values-").takeIf { it != folderName } ?: return null
        val tag = when {
            qualifiers.startsWith("b+") -> qualifiers.removePrefix("b+").replace('+', '-')
            else -> Regex("([a-z]{2,3})(?:-r([A-Z]{2}|[0-9]{3}))?").matchEntire(qualifiers)
                ?.destructured?.let { (language, region) -> if (region.isEmpty()) language else "$language-$region" }
                ?: return null
        }
        return runCatching { LocaleTag.of(tag) }.getOrNull()
    }

    private fun readLocale(dir: Path, locale: LocaleTag): LocalizedStrings {
        val entries = dir.listDirectoryEntries()
            .filter { it.extension == "xml" }
            .sortedBy { it.name }
            .flatMap { file -> file.inputStream().use { AndroidStringsReader.readEntries(it, file.toString()) } }
        val duplicate = entries.groupBy { it.key }.entries.firstOrNull { it.value.size > 1 }
        if (duplicate != null) throw ParseException(dir.toString(), null, "Duplicate key '${duplicate.key}'")
        return LocalizedStrings(locale, entries)
    }
}
