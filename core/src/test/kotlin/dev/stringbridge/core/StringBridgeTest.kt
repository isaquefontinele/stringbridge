package dev.stringbridge.core

import kotlin.test.Test
import kotlin.test.assertTrue

class StringBridgeTest {
    @Test
    fun versionIsNotBlank() {
        assertTrue(StringBridge.VERSION.isNotBlank())
    }
}
