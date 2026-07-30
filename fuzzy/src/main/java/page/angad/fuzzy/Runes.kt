package page.angad.fuzzy

import java.text.Normalizer

/**
 * A single character of a haystack or a needle.
 *
 * fzf is written in Go, where this is a `rune` (a full Unicode code point). The JS port splits on
 * UTF-16 code units instead, so a surrogate pair becomes two runes; this port keeps that behavior
 * so that match offsets line up with [String] indices.
 */
internal typealias Rune = Int

internal fun strToRunes(str: String): IntArray = IntArray(str.length) { str[it].code }

internal fun runesToStr(runes: IntArray): String {
    val sb = StringBuilder(runes.size)
    for (rune in runes) sb.appendCodePoint(rune)
    return sb.toString()
}

internal fun String.nfc(): String = Normalizer.normalize(this, Normalizer.Form.NFC)

/** Mirrors JS `String.fromCodePoint(rune).toLowerCase().codePointAt(0)`. */
internal fun lowerRune(rune: Rune): Rune {
    if (rune < 128) return if (rune in CAPITAL_A_RUNE..CAPITAL_Z_RUNE) rune + 32 else rune
    return String(Character.toChars(rune)).lowercase().codePointAt(0)
}
