package dev.stringbridge.core.android

import dev.stringbridge.core.ParseException
import dev.stringbridge.core.model.LocaleTag
import dev.stringbridge.core.model.Platform
import dev.stringbridge.core.model.PluralCategory.ONE
import dev.stringbridge.core.model.PluralCategory.OTHER
import dev.stringbridge.core.model.StringValue
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AndroidResourcesTest {
    private val en = LocaleTag.of("en")
    private val ptBr = LocaleTag.of("pt-BR")
    private val samplesDir = Path(System.getProperty("stringbridge.samples"))

    @Test
    fun mapsFolderNamesToLocales() {
        assertEquals(en, AndroidResources.localeOf("values", en))
        assertEquals(LocaleTag.of("pt"), AndroidResources.localeOf("values-pt", en))
        assertEquals(ptBr, AndroidResources.localeOf("values-pt-rBR", en))
        assertEquals(LocaleTag.of("es-419"), AndroidResources.localeOf("values-es-r419", en))
        assertEquals(LocaleTag.of("sr-Latn"), AndroidResources.localeOf("values-b+sr+Latn", en))
        assertNull(AndroidResources.localeOf("values-night", en))
        assertNull(AndroidResources.localeOf("values-v21", en))
        assertNull(AndroidResources.localeOf("values-pt-rBR-land", en))
        assertNull(AndroidResources.localeOf("drawable", en))
    }

    @Test
    fun readsDemoSample() {
        val catalog = AndroidResources.read(samplesDir.resolve("demo/android/res"), en)

        assertEquals(Platform.ANDROID, catalog.platform)
        assertEquals(listOf(en, ptBr), catalog.locales)

        val source = catalog.source!!
        assertEquals(listOf("app_name", "welcome_title", "greeting", "sync_status", "notes_count"), source.keys)
        assertFalse(source["app_name"]!!.translatable)
        assertEquals(StringValue.Text("Synced %1\$d notes from %2\$s"), source["sync_status"]?.value)

        val pt = catalog[ptBr]!!
        assertNull(pt["app_name"])
        assertEquals(StringValue.Text("Olá, %1\$s!"), pt["greeting"]?.value)
        assertEquals(StringValue.Plural(mapOf(ONE to "%d nota", OTHER to "%d notas")), pt["notes_count"]?.value)
    }

    @Test
    fun mergesFilesAndSkipsNonLocaleFolders() {
        val res = tempRes(
            "values/strings.xml" to """<resources><string name="a">A</string></resources>""",
            "values/arrays.xml" to """<resources><string-array name="b"><item>B</item></string-array></resources>""",
            "values-night/strings.xml" to """<resources><string name="a">Night</string></resources>""",
        )
        val catalog = AndroidResources.read(res, en)
        assertEquals(listOf(en), catalog.locales)
        assertEquals(listOf("b", "a"), catalog.source!!.keys)
    }

    @Test
    fun rejectsKeysDuplicatedAcrossFiles() {
        val res = tempRes(
            "values/a.xml" to """<resources><string name="x">1</string></resources>""",
            "values/b.xml" to """<resources><string name="x">2</string></resources>""",
        )
        assertFailsWith<ParseException> { AndroidResources.read(res, en) }
    }

    private fun tempRes(vararg files: Pair<String, String>): Path {
        val root = createTempDirectory("res")
        for ((path, content) in files) {
            val file = root.resolve(path)
            file.parent.createDirectories()
            file.writeText(content)
        }
        return root
    }
}
