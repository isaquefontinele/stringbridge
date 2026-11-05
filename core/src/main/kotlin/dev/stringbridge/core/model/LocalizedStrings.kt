package dev.stringbridge.core.model

/**
 * All strings of one [locale], in the order they appear in the source file(s).
 * Keys are unique within a locale.
 */
class LocalizedStrings(val locale: LocaleTag, val entries: List<StringEntry>) {

    private val byKey: Map<String, StringEntry> = entries.associateBy { it.key }

    init {
        require(byKey.size == entries.size) {
            val duplicates = entries.groupBy { it.key }.filterValues { it.size > 1 }.keys
            "Duplicate keys in $locale: ${duplicates.joinToString()}"
        }
    }

    /** Keys in file order. */
    val keys: List<String> get() = entries.map { it.key }

    operator fun get(key: String): StringEntry? = byKey[key]

    operator fun contains(key: String): Boolean = key in byKey

    override fun equals(other: Any?): Boolean =
        other is LocalizedStrings && locale == other.locale && entries == other.entries

    override fun hashCode(): Int = 31 * locale.hashCode() + entries.hashCode()

    override fun toString(): String = "LocalizedStrings(locale=$locale, entries=$entries)"
}
