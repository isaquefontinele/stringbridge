package dev.stringbridge.core.model

import dev.stringbridge.core.model.PluralCategory.FEW
import dev.stringbridge.core.model.PluralCategory.ONE
import dev.stringbridge.core.model.PluralCategory.OTHER
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class StringValueTest {
    @Test
    fun pluralFormsAreInCldrOrder() {
        val plural = StringValue.Plural(mapOf(OTHER to "%d items", FEW to "%d itemy", ONE to "%d item"))
        assertEquals(listOf(ONE, FEW, OTHER), plural.categories.toList())
        assertEquals("%d item", plural[ONE])
        assertNull(plural[PluralCategory.ZERO])
    }

    @Test
    fun pluralsWithSameFormsAreEqual() {
        assertEquals(
            StringValue.Plural(mapOf(ONE to "a", OTHER to "b")),
            StringValue.Plural(mapOf(OTHER to "b", ONE to "a")),
        )
    }

    @Test
    fun pluralNeedsAForm() {
        assertFailsWith<IllegalArgumentException> { StringValue.Plural(emptyMap()) }
    }

    @Test
    fun parsesCldrCategoryNames() {
        assertEquals(FEW, PluralCategory.fromCldrName("few"))
        assertNull(PluralCategory.fromCldrName("several"))
    }
}
