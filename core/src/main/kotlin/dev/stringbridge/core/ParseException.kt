package dev.stringbridge.core

/** A strings file could not be read. [source] names the file and [line] is 1-based when known. */
class ParseException(
    val source: String,
    val line: Int?,
    val reason: String,
    cause: Throwable? = null,
) : Exception(if (line != null) "$source:$line: $reason" else "$source: $reason", cause)
