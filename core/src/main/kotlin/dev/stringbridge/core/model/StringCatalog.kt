package dev.stringbridge.core.model

/**
 * Every locale's strings for one [platform], e.g. an Android `res/` folder or an iOS String Catalog.
 *
 * Localizations are kept sorted by locale tag so that anything derived from a catalog has a
 * stable order (NFR-05).
 *
 * @property sourceLocale The development language the other locales are translated from.
 */
class StringCatalog(
    val platform: Platform,
    val sourceLocale: LocaleTag,
    localizations: List<LocalizedStrings>,
) {
    val localizations: List<LocalizedStrings> = localizations.sortedBy { it.locale }

    private val byLocale: Map<LocaleTag, LocalizedStrings> = localizations.associateBy { it.locale }

    init {
        require(byLocale.size == localizations.size) {
            val duplicates = localizations.groupBy { it.locale }.filterValues { it.size > 1 }.keys
            "Duplicate locales: ${duplicates.joinToString()}"
        }
    }

    val locales: List<LocaleTag> get() = localizations.map { it.locale }

    /** Strings of the [sourceLocale], or `null` if the catalog has none. */
    val source: LocalizedStrings? get() = byLocale[sourceLocale]

    operator fun get(locale: LocaleTag): LocalizedStrings? = byLocale[locale]

    override fun equals(other: Any?): Boolean =
        other is StringCatalog && platform == other.platform && sourceLocale == other.sourceLocale &&
            localizations == other.localizations

    override fun hashCode(): Int = (platform.hashCode() * 31 + sourceLocale.hashCode()) * 31 + localizations.hashCode()

    override fun toString(): String =
        "StringCatalog(platform=$platform, sourceLocale=$sourceLocale, localizations=$localizations)"
}
