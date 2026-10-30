package dev.stringbridge.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.stringbridge.core.StringBridge

/** Root command. Subcommands (check, convert, sync) are added in later milestones. */
class StringBridgeCommand : CliktCommand(name = "stringbridge") {
    init {
        versionOption(StringBridge.VERSION)
    }

    override fun help(context: Context) =
        "Keep iOS and Android translations in sync and catch localization bugs in CI."

    override fun run() = Unit
}
