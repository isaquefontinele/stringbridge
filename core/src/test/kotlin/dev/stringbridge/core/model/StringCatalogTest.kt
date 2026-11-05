package dev.stringbridge.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StringCatalogTest {
    private val en = LocaleTag.of("en")
    private val ptBr = LocaleTag.of("pt-BR")

    private fun text(key: String, value: String) = StringEntry(key, StringValue.Text(value))

    @Test
    fun localizedStringsKeepFileOrderAndLookUpByKey() {
        val strings = LocalizedStrings(en, listOf(text("b_key", "B"), text("a_key", "A")))
        assertEquals(listOf("b_key", "a_key"), strings.keys)
        assertEquals(StringValue.Text("A"), strings["a_key"]?.value)
        assertTrue("b_key" in strings)
        assertFalse("missing" in strings)
    }

    @Test
    fun localizedStringsRejectDuplicateKeys() {
        val error = assertFailsWith<IllegalArgumentException> {
            LocalizedStrings(en, listOf(text("title", "A"), text("title", "B")))
        }
        assertTrue("title" in error.message.orEmpty())
    }

    @Test
    fun entryRejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> { text(" ", "A") }
    }

    @Test
    fun catalogSortsLocalesAndFindsSource() {
        val catalog = StringCatalog(
            Platform.ANDROID,
            sourceLocale = en,
            localizations = listOf(LocalizedStrings(ptBr, emptyList()), LocalizedStrings(en, listOf(text("k", "v")))),
        )
        assertEquals(listOf(en, ptBr), catalog.locales)
        assertEquals(en, catalog.source?.locale)
        assertNull(catalog[LocaleTag.of("fr")])
    }

    @Test
    fun catalogRejectsDuplicateLocales() {
        assertFailsWith<IllegalArgumentException> {
            StringCatalog(Platform.APPLE, en, listOf(LocalizedStrings(en, emptyList()), LocalizedStrings(en, emptyList())))
        }
    }

    @Test
    fun entryDefaultsToTranslatableWithoutComment() {
        val entry = text("k", "v")
        assertTrue(entry.translatable)
        assertNull(entry.comment)
    }
}
