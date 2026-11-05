package dev.stringbridge.core.model

/**
 * A BCP 47 language tag such as `en`, `pt-BR` or `sr-Latn`, normalized so that tags written
 * differently by each platform compare equal (`pt_br`, `pt-br` and `pt-BR` are the same locale).
 *
 * Normalization: language in lower case, script in title case, region in upper case,
 * any other subtag in lower case. Both `-` and `_` are accepted as separators.
 */
@ConsistentCopyVisibility
data class LocaleTag private constructor(val tag: String) : Comparable<LocaleTag> {

    /** The primary language subtag, e.g. `pt` for `pt-BR`. */
    val language: String get() = tag.substringBefore('-')

    override fun compareTo(other: LocaleTag): Int = tag.compareTo(other.tag)

    override fun toString(): String = tag

    companion object {
        private val LANGUAGE = Regex("[a-zA-Z]{2,8}")
        private val SCRIPT = Regex("[a-zA-Z]{4}")
        private val REGION = Regex("[a-zA-Z]{2}|[0-9]{3}")
        private val OTHER = Regex("[a-zA-Z0-9]{1,8}")

        /** Parses and normalizes [value]; throws [IllegalArgumentException] if it isn't a valid tag. */
        fun of(value: String): LocaleTag {
            val parts = value.trim().split('-', '_')
            require(parts.first().matches(LANGUAGE)) { "Invalid locale tag: '$value'" }
            val normalized = parts.mapIndexed { index, part ->
                when {
                    index == 0 -> part.lowercase()
                    index == 1 && part.matches(SCRIPT) -> part.lowercase().replaceFirstChar { it.uppercase() }
                    index <= 2 && part.matches(REGION) -> part.uppercase()
                    part.matches(OTHER) -> part.lowercase()
                    else -> throw IllegalArgumentException("Invalid locale tag: '$value'")
                }
            }
            return LocaleTag(normalized.joinToString("-"))
        }
    }
}
