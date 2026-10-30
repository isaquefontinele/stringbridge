package dev.stringbridge.cli

import com.github.ajalt.clikt.testing.test
import dev.stringbridge.core.StringBridge
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class StringBridgeCommandTest {
    @Test
    fun helpDescribesTheTool() {
        val result = StringBridgeCommand().test("--help")
        assertEquals(0, result.statusCode)
        assertContains(result.output, "Usage: stringbridge")
    }

    @Test
    fun versionPrintsCoreVersion() {
        val result = StringBridgeCommand().test("--version")
        assertEquals(0, result.statusCode)
        assertContains(result.output, StringBridge.VERSION)
    }
}
