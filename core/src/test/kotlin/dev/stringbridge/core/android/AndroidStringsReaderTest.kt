package dev.stringbridge.core.android

import dev.stringbridge.core.ParseException
import dev.stringbridge.core.model.LocaleTag
import dev.stringbridge.core.model.LocalizedStrings
import dev.stringbridge.core.model.PluralCategory.FEW
import dev.stringbridge.core.model.PluralCategory.ONE
import dev.stringbridge.core.model.PluralCategory.OTHER
import dev.stringbridge.core.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AndroidStringsReaderTest {
    private val en = LocaleTag.of("en")

    private fun read(body: String): LocalizedStrings =
        AndroidStringsReader.read(
            """<?xml version="1.0" encoding="utf-8"?>
            |<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
            |$body
            |</resources>""".trimMargin().byteInputStream(),
            en,
            "strings.xml",
        )

    @Test
    fun readsStringsInFileOrder() {
        val strings = read(
            """
            <string name="title">Title</string>
            <string name="greeting">Hello, %1${'$'}s!</string>
            """,
        )
        assertEquals(listOf("title", "greeting"), strings.keys)
        assertEquals(StringValue.Text("Hello, %1\$s!"), strings["greeting"]?.value)
        assertEquals(en, strings.locale)
    }

    @Test
    fun readsPlurals() {
        val strings = read(
            """
            <plurals name="files">
                <item quantity="other">%d plików</item>
                <item quantity="one">%d plik</item>
                <item quantity="few">%d pliki</item>
            </plurals>
            """,
        )
        val plural = strings["files"]?.value as StringValue.Plural
        assertEquals(listOf(ONE, FEW, OTHER), plural.categories.toList())
        assertEquals("%d pliki", plural[FEW])
    }

    @Test
    fun readsStringArrays() {
        val strings = read(
            """
            <string-array name="sizes">
                <item>Small</item>
                <item>It\'s big</item>
            </string-array>
            """,
        )
        assertEquals(StringValue.StringArray(listOf("Small", "It's big")), strings["sizes"]?.value)
    }

    @Test
    fun readsTranslatableFlag() {
        val strings = read(
            """
            <string name="app_name" translatable="false">Demo</string>
            <string name="title">Title</string>
            """,
        )
        assertFalse(strings["app_name"]!!.translatable)
        assertTrue(strings["title"]!!.translatable)
    }

    @Test
    fun attachesOnlyTheCommentDirectlyBeforeAnElement() {
        val strings = read(
            """
            <!-- Shown on the home screen -->
            <string name="title">Title</string>
            <string name="subtitle">Subtitle</string>
            """,
        )
        assertEquals("Shown on the home screen", strings["title"]?.comment)
        assertNull(strings["subtitle"]?.comment)
    }

    @Test
    fun decodesEscapingAndEntities() {
        val strings = read(
            """
            <string name="apostrophe">Don\'t</string>
            <string name="quoted">"Don't   stop"</string>
            <string name="entities">Fish &amp; chips &lt;3</string>
            <string name="cdata"><![CDATA[<b>raw</b>]]></string>
            """,
        )
        assertEquals("Don't", (strings["apostrophe"]?.value as StringValue.Text).text)
        assertEquals("Don't   stop", (strings["quoted"]?.value as StringValue.Text).text)
        assertEquals("Fish & chips <3", (strings["entities"]?.value as StringValue.Text).text)
        assertEquals("<b>raw</b>", (strings["cdata"]?.value as StringValue.Text).text)
    }

    @Test
    fun keepsInlineMarkup() {
        val strings = read(
            """<string name="welcome">Hi <b>there</b>, <xliff:g id="user" example="Ana">%1${'$'}s</xliff:g>!</string>""",
        )
        assertEquals(
            """Hi <b>there</b>, <xliff:g id="user" example="Ana">%1${'$'}s</xliff:g>!""",
            (strings["welcome"]?.value as StringValue.Text).text,
        )
    }

    @Test
    fun ignoresOtherResourceTypes() {
        val strings = read(
            """
            <color name="accent">#FF0000</color>
            <integer-array name="ids"><item>1</item></integer-array>
            <string name="title">Title</string>
            """,
        )
        assertEquals(listOf("title"), strings.keys)
    }

    @Test
    fun rejectsUnknownPluralQuantity() {
        val error = assertFailsWith<ParseException> {
            read("""<plurals name="x"><item quantity="several">x</item></plurals>""")
        }
        assertTrue("several" in error.reason)
    }

    @Test
    fun rejectsMissingName() {
        assertFailsWith<ParseException> { read("""<string>Nameless</string>""") }
    }

    @Test
    fun rejectsDuplicateKeys() {
        assertFailsWith<IllegalArgumentException> {
            read("""<string name="a">1</string><string name="a">2</string>""")
        }
    }

    @Test
    fun reportsMalformedXmlWithLine() {
        val error = assertFailsWith<ParseException> { read("""<string name="a">unclosed""") }
        assertEquals("strings.xml", error.source)
        assertTrue(error.line != null)
    }

    @Test
    fun doesNotResolveExternalEntities() {
        val xml = """<?xml version="1.0"?>
            <!DOCTYPE resources [<!ENTITY xxe SYSTEM "file:///etc/hosts">]>
            <resources><string name="a">&xxe;</string></resources>"""
        assertFailsWith<ParseException> { AndroidStringsReader.read(xml.byteInputStream(), en) }
    }
}
