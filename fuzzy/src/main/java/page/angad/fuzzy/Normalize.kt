package page.angad.fuzzy

import java.text.Normalizer

private const val NORMALIZE_MIN = 0x00c0
private const val NORMALIZE_MAX = 0x2184

private val BASE: Map<Int, Char> = mapOf(
    0x00d8 to 'O',
    0x00df to 's',
    0x00f8 to 'o',
    0x0111 to 'd',
    0x0127 to 'h',
    0x0131 to 'i',
    0x0140 to 'l',
    0x0142 to 'l',
    0x0167 to 't',
    0x017f to 's',
    0x0180 to 'b',
    0x0181 to 'B',
    0x0183 to 'b',
    0x0186 to 'O',
    0x0188 to 'c',
    0x0189 to 'D',
    0x018a to 'D',
    0x018c to 'd',
    0x018e to 'E',
    0x0190 to 'E',
    0x0192 to 'f',
    0x0193 to 'G',
    0x0197 to 'I',
    0x0199 to 'k',
    0x019a to 'l',
    0x019c to 'M',
    0x019d to 'N',
    0x019e to 'n',
    0x019f to 'O',
    0x01a5 to 'p',
    0x01ab to 't',
    0x01ad to 't',
    0x01ae to 'T',
    0x01b2 to 'V',
    0x01b4 to 'y',
    0x01b6 to 'z',
    0x01dd to 'e',
    0x01e5 to 'g',
    0x0220 to 'N',
    0x0221 to 'd',
    0x0225 to 'z',
    0x0234 to 'l',
    0x0235 to 'n',
    0x0236 to 't',
    0x0237 to 'j',
    0x023a to 'A',
    0x023b to 'C',
    0x023c to 'c',
    0x023d to 'L',
    0x023e to 'T',
    0x023f to 's',
    0x0240 to 'z',
    0x0243 to 'B',
    0x0244 to 'U',
    0x0245 to 'V',
    0x0246 to 'E',
    0x0247 to 'e',
    0x0248 to 'J',
    0x0249 to 'j',
    0x024a to 'Q',
    0x024b to 'q',
    0x024c to 'R',
    0x024d to 'r',
    0x024e to 'Y',
    0x024f to 'y',
    0x0250 to 'a',
    0x0251 to 'a',
    0x0253 to 'b',
    0x0254 to 'o',
    0x0255 to 'c',
    0x0256 to 'd',
    0x0257 to 'd',
    0x0258 to 'e',
    0x025b to 'e',
    0x025c to 'e',
    0x025d to 'e',
    0x025e to 'e',
    0x025f to 'j',
    0x0260 to 'g',
    0x0261 to 'g',
    0x0262 to 'G',
    0x0265 to 'h',
    0x0266 to 'h',
    0x0268 to 'i',
    0x026a to 'I',
    0x026b to 'l',
    0x026c to 'l',
    0x026d to 'l',
    0x026f to 'm',
    0x0270 to 'm',
    0x0271 to 'm',
    0x0272 to 'n',
    0x0273 to 'n',
    0x0274 to 'N',
    0x0275 to 'o',
    0x0279 to 'r',
    0x027a to 'r',
    0x027b to 'r',
    0x027c to 'r',
    0x027d to 'r',
    0x027e to 'r',
    0x027f to 'r',
    0x0280 to 'R',
    0x0281 to 'R',
    0x0282 to 's',
    0x0287 to 't',
    0x0288 to 't',
    0x0289 to 'u',
    0x028b to 'v',
    0x028c to 'v',
    0x028d to 'w',
    0x028e to 'y',
    0x028f to 'Y',
    0x0290 to 'z',
    0x0291 to 'z',
    0x0297 to 'c',
    0x0299 to 'B',
    0x029a to 'e',
    0x029b to 'G',
    0x029c to 'H',
    0x029d to 'j',
    0x029e to 'k',
    0x029f to 'L',
    0x02a0 to 'q',
    0x02ae to 'h',
    0x0363 to 'a',
    0x0364 to 'e',
    0x0365 to 'i',
    0x0366 to 'o',
    0x0367 to 'u',
    0x0368 to 'c',
    0x0369 to 'd',
    0x036a to 'h',
    0x036b to 'm',
    0x036c to 'r',
    0x036d to 't',
    0x036e to 'v',
    0x036f to 'x',
    0x1d00 to 'A',
    0x1d03 to 'B',
    0x1d04 to 'C',
    0x1d05 to 'D',
    0x1d07 to 'E',
    0x1d08 to 'e',
    0x1d09 to 'i',
    0x1d0a to 'J',
    0x1d0b to 'K',
    0x1d0c to 'L',
    0x1d0d to 'M',
    0x1d0e to 'N',
    0x1d0f to 'O',
    0x1d10 to 'O',
    0x1d11 to 'o',
    0x1d12 to 'o',
    0x1d13 to 'o',
    0x1d16 to 'o',
    0x1d17 to 'o',
    0x1d18 to 'P',
    0x1d19 to 'R',
    0x1d1a to 'R',
    0x1d1b to 'T',
    0x1d1c to 'U',
    0x1d1d to 'u',
    0x1d1e to 'u',
    0x1d1f to 'm',
    0x1d20 to 'V',
    0x1d21 to 'W',
    0x1d22 to 'Z',
    0x1d62 to 'i',
    0x1d63 to 'r',
    0x1d64 to 'u',
    0x1d65 to 'v',
    0x1e9a to 'a',
    0x1e9b to 's',
    0x2071 to 'i',
    0x2095 to 'h',
    0x2096 to 'k',
    0x2097 to 'l',
    0x2098 to 'm',
    0x2099 to 'n',
    0x209a to 'p',
    0x209b to 's',
    0x209c to 't',
    0x2184 to 'c',
)

private const val ASCII_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

private val VIETNAMESE_RANGES = listOf(
    'a' to 7844..7863,
    'e' to 7870..7879,
    'o' to 7888..7907,
    'u' to 7912..7921,
)

/**
 * Dense lookup of [NORMALIZE_MIN]..[NORMALIZE_MAX] onto their ASCII equivalent.
 * A zero entry means the rune has no mapping.
 */
private val NORMALIZED = IntArray(NORMALIZE_MAX - NORMALIZE_MIN + 1).also { table ->
    for ((rune, ascii) in BASE) table[rune - NORMALIZE_MIN] = ascii.code

    // Compose every ASCII letter with every combining diacritic and record the
    // ones that NFC collapses into a single precomposed rune.
    for (diacritic in 0x0300..0x036f) {
        for (ascii in ASCII_LETTERS) {
            val composed =
                Normalizer.normalize("" + ascii + diacritic.toChar(), Normalizer.Form.NFC)
            val rune = composed.codePointAt(0)
            if (rune in 127..NORMALIZE_MAX) table[rune - NORMALIZE_MIN] = ascii.code
        }
    }

    // Vietnamese blocks, where the runes alternate uppercase/lowercase.
    for ((lower, range) in VIETNAMESE_RANGES) {
        val upper = lower.uppercaseChar()
        for (rune in range) table[rune - NORMALIZE_MIN] =
            if (rune % 2 == 0) upper.code else lower.code
    }
}

/** Strips the diacritic from [rune], returning it unchanged when it has no ASCII equivalent. */
internal fun normalizeRune(rune: Rune): Rune {
    if (rune !in NORMALIZE_MIN..NORMALIZE_MAX) return rune
    val normalized = NORMALIZED[rune - NORMALIZE_MIN]
    return if (normalized != 0) normalized else rune
}
