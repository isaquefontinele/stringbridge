package dev.stringbridge.core.android

import dev.stringbridge.core.android.AndroidEscaping.Segment
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidEscapingTest {
    @Test
    fun decodesBackslashEscapes() {
        assertEquals("It's \"here\" \\ @home ?", AndroidEscaping.decode("""It\'s \"here\" \\ \@home \?"""))
    }

    @Test
    fun decodesNewlineTabAndUnicode() {
        assertEquals("a\nb\tcé", AndroidEscaping.decode("""a\nb\tcé"""))
    }

    @Test
    fun keepsInvalidUnicodeEscapeAsText() {
        assertEquals("u12", AndroidEscaping.decode("""\u12"""))
    }

    @Test
    fun collapsesAndTrimsUnquotedWhitespace() {
        assertEquals("one two three", AndroidEscaping.decode("\n    one   two\n\tthree  \n"))
    }

    @Test
    fun quotesPreserveWhitespaceAndAreRemoved() {
        assertEquals("  padded   text", AndroidEscaping.decode("\"  padded  \"  \n text"))
        assertEquals("It's", AndroidEscaping.decode("\"It's\""))
    }

    @Test
    fun passesMarkupThroughAndKeepsSpacingAroundIt() {
        val segments = listOf(Segment.Text("Hello "), Segment.Markup("<b>"), Segment.Text("big"), Segment.Markup("</b>"), Segment.Text("  world"))
        assertEquals("Hello <b>big</b> world", AndroidEscaping.decode(segments))
    }
}
