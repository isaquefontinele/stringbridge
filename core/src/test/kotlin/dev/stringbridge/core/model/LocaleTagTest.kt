package dev.stringbridge.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocaleTagTest {
    @Test
    fun normalizesCaseAndSeparators() {
        assertEquals("pt-BR", LocaleTag.of("pt_br").tag)
        assertEquals("pt-BR", LocaleTag.of("PT-br").tag)
        assertEquals(LocaleTag.of("pt-BR"), LocaleTag.of("pt_BR"))
    }

    @Test
    fun normalizesScriptAndNumericRegion() {
        assertEquals("sr-Latn", LocaleTag.of("sr-latn").tag)
        assertEquals("zh-Hant-TW", LocaleTag.of("zh_hant_tw").tag)
        assertEquals("es-419", LocaleTag.of("es-419").tag)
    }

    @Test
    fun exposesLanguage() {
        assertEquals("pt", LocaleTag.of("pt-BR").language)
    }

    @Test
    fun rejectsInvalidTags() {
        assertFailsWith<IllegalArgumentException> { LocaleTag.of("") }
        assertFailsWith<IllegalArgumentException> { LocaleTag.of("p") }
        assertFailsWith<IllegalArgumentException> { LocaleTag.of("en-US!") }
    }

    @Test
    fun sortsByTag() {
        val tags = listOf("pt-BR", "en", "es").map(LocaleTag::of).sorted()
        assertEquals(listOf("en", "es", "pt-BR"), tags.map { it.tag })
    }
}
