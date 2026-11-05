package dev.stringbridge.core.android

import dev.stringbridge.core.ParseException
import dev.stringbridge.core.android.AndroidEscaping.Segment
import dev.stringbridge.core.model.LocaleTag
import dev.stringbridge.core.model.LocalizedStrings
import dev.stringbridge.core.model.PluralCategory
import dev.stringbridge.core.model.StringEntry
import dev.stringbridge.core.model.StringValue
import java.io.InputStream
import java.nio.file.Path
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants.CDATA
import javax.xml.stream.XMLStreamConstants.CHARACTERS
import javax.xml.stream.XMLStreamConstants.COMMENT
import javax.xml.stream.XMLStreamConstants.END_ELEMENT
import javax.xml.stream.XMLStreamConstants.SPACE
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader
import kotlin.io.path.inputStream

/**
 * Reads one Android `strings.xml`-style resource file: `<string>`, `<plurals>` and
 * `<string-array>` elements. Other resource types in the same file are ignored.
 *
 * - Text is decoded with [AndroidEscaping]; inline markup such as `<b>` or `<xliff:g>` is kept verbatim.
 * - `translatable="false"` maps to [StringEntry.translatable].
 * - An XML comment directly before an element becomes its [StringEntry.comment].
 * - DTDs and external entities are disabled.
 */
object AndroidStringsReader {

    private val factory: XMLInputFactory = XMLInputFactory.newFactory().apply {
        setProperty(XMLInputFactory.SUPPORT_DTD, false)
        setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
        setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true)
        setProperty(XMLInputFactory.IS_COALESCING, true)
    }

    fun read(file: Path, locale: LocaleTag): LocalizedStrings =
        LocalizedStrings(locale, file.inputStream().use { readEntries(it, file.toString()) })

    fun read(input: InputStream, locale: LocaleTag, source: String = "<input>"): LocalizedStrings =
        LocalizedStrings(locale, readEntries(input, source))

    /** Reads the entries of one file in file order, without checking for duplicate keys. */
    internal fun readEntries(input: InputStream, source: String): List<StringEntry> {
        val reader = try {
            factory.createXMLStreamReader(input)
        } catch (e: XMLStreamException) {
            throw ParseException(source, e.location?.lineNumber, e.message.orEmpty(), e)
        }
        try {
            return Parser(reader, source).parse()
        } catch (e: XMLStreamException) {
            throw ParseException(source, e.location?.lineNumber, e.message.orEmpty(), e)
        } finally {
            reader.close()
        }
    }

    private class Parser(private val reader: XMLStreamReader, private val source: String) {

        fun parse(): List<StringEntry> {
            val entries = mutableListOf<StringEntry>()
            reader.nextTag()
            if (reader.localName != "resources") fail("Expected <resources> but found <${reader.localName}>")

            var comment: String? = null
            while (reader.hasNext()) {
                when (reader.next()) {
                    COMMENT -> comment = reader.text.trim().ifEmpty { null }
                    START_ELEMENT -> {
                        readElement(comment)?.let(entries::add)
                        comment = null
                    }
                    CHARACTERS, CDATA -> if (!reader.isWhiteSpace) comment = null
                    END_ELEMENT -> break
                }
            }
            return entries
        }

        /** Reads the element at the cursor and leaves the cursor on its end tag. */
        private fun readElement(comment: String?): StringEntry? {
            val tag = reader.localName
            if (tag !in setOf("string", "plurals", "string-array")) {
                skipElement()
                return null
            }
            val name = attribute("name") ?: fail("<$tag> without a name attribute")
            val translatable = attribute("translatable") != "false"
            val value = when (tag) {
                "string" -> StringValue.Text(readText())
                "plurals" -> readPlural(name)
                else -> StringValue.StringArray(readItems { readText() })
            }
            return StringEntry(name, value, comment, translatable)
        }

        private fun readPlural(name: String): StringValue.Plural {
            val forms = linkedMapOf<PluralCategory, String>()
            readItems {
                val quantity = attribute("quantity") ?: fail("Plural '$name' has an <item> without quantity")
                val category = PluralCategory.fromCldrName(quantity)
                    ?: fail("Plural '$name' has an unknown quantity '$quantity'")
                if (category in forms) fail("Plural '$name' repeats quantity '$quantity'")
                forms[category] = readText()
            }
            if (forms.isEmpty()) fail("Plural '$name' has no items")
            return StringValue.Plural(forms)
        }

        /** Calls [readItem] with the cursor on each child `<item>`; other children are skipped. */
        private fun <T> readItems(readItem: () -> T): List<T> {
            val items = mutableListOf<T>()
            while (reader.nextTag() == START_ELEMENT) {
                if (reader.localName == "item") items += readItem() else skipElement()
            }
            return items
        }

        /** Reads the mixed content of the current element and decodes it. */
        private fun readText(): String {
            val segments = mutableListOf<Segment>()
            var depth = 0
            while (true) {
                when (reader.next()) {
                    CHARACTERS, CDATA, SPACE -> segments += Segment.Text(reader.text)
                    START_ELEMENT -> {
                        depth++
                        segments += Segment.Markup(startTag())
                    }
                    END_ELEMENT -> {
                        if (depth == 0) return AndroidEscaping.decode(segments)
                        depth--
                        segments += Segment.Markup("</${qualifiedName()}>")
                    }
                }
            }
        }

        private fun startTag(): String = buildString {
            append('<').append(qualifiedName())
            for (i in 0 until reader.attributeCount) {
                val prefix = reader.getAttributePrefix(i)
                val name = reader.getAttributeLocalName(i)
                append(' ')
                if (!prefix.isNullOrEmpty()) append(prefix).append(':')
                append(name).append("=\"").append(escapeAttribute(reader.getAttributeValue(i))).append('"')
            }
            append('>')
        }

        private fun qualifiedName(): String =
            if (reader.prefix.isNullOrEmpty()) reader.localName else "${reader.prefix}:${reader.localName}"

        private fun escapeAttribute(value: String) =
            value.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;")

        private fun skipElement() {
            var depth = 1
            while (depth > 0) {
                when (reader.next()) {
                    START_ELEMENT -> depth++
                    END_ELEMENT -> depth--
                }
            }
        }

        private fun attribute(name: String): String? = reader.getAttributeValue(null, name)

        private fun fail(reason: String): Nothing = throw ParseException(source, reader.location?.lineNumber, reason)
    }
}
