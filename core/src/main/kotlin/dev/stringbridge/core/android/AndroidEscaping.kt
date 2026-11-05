package dev.stringbridge.core.android

/**
 * Decodes Android string resource text the way aapt2 does:
 *
 * - `\'`, `\"`, `\\`, `\@`, `\?` and any other escaped character become that character;
 *   `\n` and `\t` become a newline and a tab; `\uXXXX` becomes the code point.
 * - Unescaped double quotes are removed and preserve the whitespace between them.
 * - Outside quotes, runs of whitespace collapse to one space and leading/trailing whitespace is dropped.
 *
 * Inline markup (such as `<b>` or `<xliff:g>`) is passed through untouched as [Segment.Markup].
 */
internal object AndroidEscaping {

    sealed interface Segment {
        data class Text(val text: String) : Segment
        data class Markup(val markup: String) : Segment
    }

    fun decode(text: String): String = decode(listOf(Segment.Text(text)))

    fun decode(segments: List<Segment>): String {
        val out = StringBuilder()
        var inQuotes = false
        var pendingSpace = false

        fun emit(value: CharSequence) {
            if (pendingSpace) out.append(' ')
            pendingSpace = false
            out.append(value)
        }

        for (segment in segments) {
            when (segment) {
                is Segment.Markup -> emit(segment.markup)
                is Segment.Text -> {
                    val text = segment.text
                    var i = 0
                    while (i < text.length) {
                        val c = text[i]
                        when {
                            c == '\\' && i + 1 < text.length -> {
                                i++
                                when (val escaped = text[i]) {
                                    'n' -> emit("\n")
                                    't' -> emit("\t")
                                    'u' -> {
                                        val hex = text.substring(i + 1, minOf(i + 5, text.length))
                                        val codePoint = hex.takeIf { it.length == 4 }?.toIntOrNull(16)
                                        if (codePoint != null) {
                                            emit(codePoint.toChar().toString())
                                            i += 4
                                        } else {
                                            emit("u")
                                        }
                                    }
                                    else -> emit(escaped.toString())
                                }
                            }
                            c == '"' -> inQuotes = !inQuotes
                            c.isWhitespace() && !inQuotes -> if (out.isNotEmpty()) pendingSpace = true
                            else -> emit(c.toString())
                        }
                        i++
                    }
                }
            }
        }
        return out.toString()
    }
}
