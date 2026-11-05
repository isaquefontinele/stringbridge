package dev.stringbridge.core.model

/**
 * The value of a localized string, independent of its file format.
 *
 * Text is stored decoded: platform escaping (such as `\'` on Android or `\"` in `.strings`) is
 * removed by readers and added back by writers. Format specifiers such as `%1$s` or `%@` are kept
 * exactly as written in the source platform's syntax.
 */
sealed interface StringValue {

    /** A single piece of text. */
    data class Text(val text: String) : StringValue

    /** A pluralized string with one text per CLDR plural category, iterated in CLDR order. */
    class Plural(forms: Map<PluralCategory, String>) : StringValue {
        val forms: Map<PluralCategory, String> = forms.toSortedMap()

        init {
            require(forms.isNotEmpty()) { "A plural needs at least one form" }
        }

        val categories: Set<PluralCategory> get() = forms.keys

        operator fun get(category: PluralCategory): String? = forms[category]

        override fun equals(other: Any?): Boolean = other is Plural && forms == other.forms

        override fun hashCode(): Int = forms.hashCode()

        override fun toString(): String = "Plural(forms=$forms)"
    }

    /** An ordered list of strings (Android `<string-array>`). */
    data class StringArray(val items: List<String>) : StringValue
}
