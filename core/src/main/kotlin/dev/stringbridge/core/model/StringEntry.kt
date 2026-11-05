package dev.stringbridge.core.model

/**
 * One localized string: its [key], [value] and the metadata that travels with it.
 *
 * @property comment Developer comment for translators, if the format has one.
 * @property translatable `false` for strings that must not be translated (Android
 *   `translatable="false"`); such strings are expected only in the source locale.
 */
data class StringEntry(
    val key: String,
    val value: StringValue,
    val comment: String? = null,
    val translatable: Boolean = true,
) {
    init {
        require(key.isNotBlank()) { "String key must not be blank" }
    }
}
