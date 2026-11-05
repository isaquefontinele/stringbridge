package dev.stringbridge.core.model

/** The CLDR plural categories, in CLDR order. Both Android and Apple use these names. */
enum class PluralCategory(val cldrName: String) {
    ZERO("zero"),
    ONE("one"),
    TWO("two"),
    FEW("few"),
    MANY("many"),
    OTHER("other"),
    ;

    companion object {
        /** Returns the category named [name] (e.g. `"few"`), or `null` if it isn't a CLDR category. */
        fun fromCldrName(name: String): PluralCategory? = entries.firstOrNull { it.cldrName == name }
    }
}
