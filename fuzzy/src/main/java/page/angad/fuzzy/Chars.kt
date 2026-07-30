package page.angad.fuzzy

// The set of runes JS matches with `\s`
private val WHITESPACE_RUNES: Set<Rune> = HashSet<Rune>().apply {
    @Suppress("SpellCheckingInspection")
    val listed = " \u000c\n\r\t\u000b\u00a0\u1680\u2028\u2029\u202f\u205f\u3000\ufeff"
    for (c in listed) add(c.code)
    for (rune in 0x2000..0x200a) add(rune)
}

internal fun isWhitespace(rune: Rune): Boolean = rune in WHITESPACE_RUNES

internal fun whitespacesAtStart(runes: IntArray): Int {
    var count = 0
    while (count < runes.size && isWhitespace(runes[count])) count++
    return count
}

internal fun whitespacesAtEnd(runes: IntArray): Int {
    var count = 0
    while (count < runes.size && isWhitespace(runes[runes.size - 1 - count])) count++
    return count
}

internal fun String.trimStartLikeJs(): String = trimStart { isWhitespace(it.code) }

internal fun String.trimEndLikeJs(): String = trimEnd { isWhitespace(it.code) }
