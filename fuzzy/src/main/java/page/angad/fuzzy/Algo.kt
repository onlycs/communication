package page.angad.fuzzy

import kotlin.math.max

internal const val MAX_ASCII = 0x7f
internal const val CAPITAL_A_RUNE = 'A'.code
internal const val CAPITAL_Z_RUNE = 'Z'.code
internal const val SMALL_A_RUNE = 'a'.code
internal const val SMALL_Z_RUNE = 'z'.code
private const val NUMERAL_ZERO_RUNE = '0'.code
private const val NUMERAL_NINE_RUNE = '9'.code

internal const val SCORE_MATCH = 16
internal const val SCORE_GAP_START = -3
internal const val SCORE_GAP_EXTENSION = -1
internal const val BONUS_BOUNDARY = SCORE_MATCH / 2
internal const val BONUS_NON_WORD = SCORE_MATCH / 2
internal const val BONUS_CAMEL_123 = BONUS_BOUNDARY + SCORE_GAP_EXTENSION
internal const val BONUS_CONSECUTIVE = -(SCORE_GAP_START + SCORE_GAP_EXTENSION)
internal const val BONUS_FIRST_CHAR_MULTIPLIER = 2

internal class AlgoResult(
    val start: Int,
    val end: Int,
    val score: Int,
    val positions: MutableSet<Int>?,
)

private val NO_MATCH = AlgoResult(-1, -1, 0, null)

internal typealias AlgoFn = (
    caseSensitive: Boolean,
    normalize: Boolean,
    forward: Boolean,
    text: IntArray,
    pattern: IntArray,
    withPos: Boolean,
    slab: Slab?,
) -> AlgoResult

private enum class CharClass { NON_WORD, LOWER, UPPER, LETTER, NUMBER }

private fun createPosSet(withPos: Boolean): MutableSet<Int>? =
    if (withPos) LinkedHashSet() else null

private fun indexAt(index: Int, max: Int, forward: Boolean): Int =
    if (forward) index else max - index - 1

private fun charClassOfAscii(rune: Rune): CharClass = when (rune) {
    in SMALL_A_RUNE..SMALL_Z_RUNE -> CharClass.LOWER
    in CAPITAL_A_RUNE..CAPITAL_Z_RUNE -> CharClass.UPPER
    in NUMERAL_ZERO_RUNE..NUMERAL_NINE_RUNE -> CharClass.NUMBER
    else -> CharClass.NON_WORD
}

private fun charClassOfNonAscii(rune: Rune): CharClass {
    val char = String(Character.toChars(rune))

    // A rune is lowercase when uppercasing it changes it, and vice versa. This also catches
    // multi-char expansions such as the sharp s, which JS classifies as lowercase.
    if (char != char.uppercase()) return CharClass.LOWER
    if (char != char.lowercase()) return CharClass.UPPER

    return when (Character.getType(rune)) {
        Character.DECIMAL_DIGIT_NUMBER.toInt(),
        Character.LETTER_NUMBER.toInt(),
        Character.OTHER_NUMBER.toInt(),
            -> CharClass.NUMBER

        Character.UPPERCASE_LETTER.toInt(),
        Character.LOWERCASE_LETTER.toInt(),
        Character.TITLECASE_LETTER.toInt(),
        Character.MODIFIER_LETTER.toInt(),
        Character.OTHER_LETTER.toInt(),
            -> CharClass.LETTER

        else -> CharClass.NON_WORD
    }
}

private fun charClassOf(rune: Rune): CharClass =
    if (rune <= MAX_ASCII) charClassOfAscii(rune) else charClassOfNonAscii(rune)

private fun bonusFor(prevClass: CharClass, currClass: CharClass): Int = when {
    prevClass == CharClass.NON_WORD && currClass != CharClass.NON_WORD -> BONUS_BOUNDARY
    (prevClass == CharClass.LOWER && currClass == CharClass.UPPER) ||
            (prevClass != CharClass.NUMBER && currClass == CharClass.NUMBER) -> BONUS_CAMEL_123

    currClass == CharClass.NON_WORD -> BONUS_NON_WORD
    else -> 0
}

private fun bonusAt(input: IntArray, idx: Int): Int =
    if (idx == 0) BONUS_BOUNDARY else bonusFor(charClassOf(input[idx - 1]), charClassOf(input[idx]))

// Scratch space reused across calls, mirroring fzf's per-worker slab. Go hands each search
// goroutine its own slab; ThreadLocal is the JVM equivalent. Like fzf, the slab is never
// cleared between calls, so the score matrix can hold stale values in cells the current
// search does not write.
private const val SLAB_16_SIZE = 100 * 1024
private const val SLAB_32_SIZE = 2048

internal class Slab(size16: Int, size32: Int) {
    val i16 = ShortArray(size16)
    val i32 = IntArray(size32)
}

private val SLAB_LOCAL = ThreadLocal.withInitial { Slab(SLAB_16_SIZE, SLAB_32_SIZE) }

internal val slab: Slab get() = SLAB_LOCAL.get()

/**
 * A window onto a slab array (or onto a standalone array when the slab is exhausted), standing
 * in for the Go slices the original algorithm indexes into.
 */
private class Buf16(private val data: ShortArray, private val base: Int) {
    operator fun get(i: Int): Int = data[base + i].toInt()
    operator fun set(i: Int, value: Int) {
        data[base + i] = value.toShort()
    }

    fun sub(i: Int) = Buf16(data, base + i)
}

private class Buf32(private val data: IntArray, private val base: Int) {
    operator fun get(i: Int): Int = data[base + i]
    operator fun set(i: Int, value: Int) {
        data[base + i] = value
    }

    fun sub(i: Int) = Buf32(data, base + i)
}

private class Allocator(private val slab: Slab?) {
    private var offset16 = 0
    private var offset32 = 0

    fun alloc16(size: Int): Buf16 {
        val s = slab
        if (s != null && s.i16.size > offset16 + size) {
            val buf = Buf16(s.i16, offset16)
            offset16 += size
            return buf
        }
        return Buf16(ShortArray(size), 0)
    }

    fun alloc32(size: Int): Buf32 {
        val s = slab
        if (s != null && s.i32.size > offset32 + size) {
            val buf = Buf32(s.i32, offset32)
            offset32 += size
            return buf
        }
        return Buf32(IntArray(size), 0)
    }
}

private fun indexOfRune(input: IntArray, rune: Rune, from: Int, until: Int): Int {
    for (i in from until until) if (input[i] == rune) return i - from
    return -1
}

private fun trySkip(input: IntArray, caseSensitive: Boolean, char: Rune, from: Int): Int {
    var idx = indexOfRune(input, char, from, input.size)
    if (idx == 0) return from

    if (!caseSensitive && char in SMALL_A_RUNE..SMALL_Z_RUNE) {
        // Look for the uppercase form, but no further than the lowercase hit we already have.
        val until = if (idx > 0) from + idx else input.size
        val uidx = indexOfRune(input, char - 32, from, until)
        if (uidx >= 0) idx = uidx
    }

    if (idx < 0) return -1
    return from + idx
}

private fun isAscii(runes: IntArray): Boolean {
    for (rune in runes) if (rune >= 128) return false
    return true
}

private fun asciiFuzzyIndex(input: IntArray, pattern: IntArray, caseSensitive: Boolean): Int {
    if (!isAscii(input)) return 0
    if (!isAscii(pattern)) return -1

    var firstIdx = 0
    var idx = 0

    for (pidx in pattern.indices) {
        idx = trySkip(input, caseSensitive, pattern[pidx], idx)
        if (idx < 0) return -1
        if (pidx == 0 && idx > 0) firstIdx = idx - 1
        idx++
    }

    return firstIdx
}

internal fun fuzzyMatchV2(
    caseSensitive: Boolean,
    normalize: Boolean,
    forward: Boolean,
    input: IntArray,
    pattern: IntArray,
    withPos: Boolean,
    slabArg: Slab?,
): AlgoResult {
    val m = pattern.size
    if (m == 0) return AlgoResult(0, 0, 0, createPosSet(withPos))

    val n = input.size
    if (slabArg != null && n * m > slabArg.i16.size) {
        return fuzzyMatchV1(caseSensitive, normalize, forward, input, pattern, withPos, slabArg)
    }

    // Phase 1. Optimized search for ASCII string
    val idx = asciiFuzzyIndex(input, pattern, caseSensitive)
    if (idx < 0) return NO_MATCH

    val alloc = Allocator(slabArg)
    val h0 = alloc.alloc16(n)
    val c0 = alloc.alloc16(n)
    val b = alloc.alloc16(n)
    val f = alloc.alloc32(m)
    val t = alloc.alloc32(n)

    for (i in 0 until n) t[i] = input[i]

    // Phase 2. Calculate bonus for each point
    var maxScore = 0
    var maxScorePos = 0
    var pidx = 0
    var lastIdx = 0
    val pchar0 = pattern[0]
    var pchar = pattern[0]
    var prevH0 = 0
    var prevCharClass = CharClass.NON_WORD
    var inGap = false

    val tSub = t.sub(idx)
    val h0Sub = h0.sub(idx)
    val c0Sub = c0.sub(idx)
    val bSub = b.sub(idx)

    for (off in 0 until n - idx) {
        var char = tSub[off]
        val charClass: CharClass

        if (char <= MAX_ASCII) {
            charClass = charClassOfAscii(char)
            if (!caseSensitive && charClass == CharClass.UPPER) char += 32
        } else {
            charClass = charClassOfNonAscii(char)
            if (!caseSensitive && charClass == CharClass.UPPER) char = lowerRune(char)
            if (normalize) char = normalizeRune(char)
        }

        tSub[off] = char
        val bonus = bonusFor(prevCharClass, charClass)
        bSub[off] = bonus
        prevCharClass = charClass

        if (char == pchar) {
            if (pidx < m) {
                f[pidx] = idx + off
                pidx++
                pchar = pattern[minOf(pidx, m - 1)]
            }
            lastIdx = idx + off
        }

        if (char == pchar0) {
            val score = SCORE_MATCH + bonus * BONUS_FIRST_CHAR_MULTIPLIER
            h0Sub[off] = score
            c0Sub[off] = 1
            if (m == 1 && ((forward && score > maxScore) || (!forward && score >= maxScore))) {
                maxScore = score
                maxScorePos = idx + off
                if (forward && bonus == BONUS_BOUNDARY) break
            }
            inGap = false
        } else {
            h0Sub[off] = max(prevH0 + if (inGap) SCORE_GAP_EXTENSION else SCORE_GAP_START, 0)
            c0Sub[off] = 0
            inGap = true
        }
        prevH0 = h0Sub[off]
    }

    if (pidx != m) return NO_MATCH

    if (m == 1) {
        val pos = if (withPos) linkedSetOf(maxScorePos) else null
        return AlgoResult(maxScorePos, maxScorePos + 1, maxScore, pos)
    }

    // Phase 3. Fill in score matrix (H)
    val f0 = f[0]
    val width = lastIdx - f0 + 1
    val h = alloc.alloc16(width * m)
    for (i in 0 until width) h[i] = h0[f0 + i]
    val c = alloc.alloc16(width * m)
    for (i in 0 until width) c[i] = c0[f0 + i]

    for (off in 0 until m - 1) {
        val fVal = f[off + 1]
        val rowPchar = pattern[off + 1]
        val rowPidx = off + 1
        val row = rowPidx * width
        val rowLen = lastIdx + 1 - fVal
        val rowT = t.sub(fVal)
        val rowB = b.sub(fVal)
        val cSub = c.sub(row + fVal - f0)
        val cDiag = c.sub(row + fVal - f0 - 1 - width)
        val hSub = h.sub(row + fVal - f0)
        val hDiag = h.sub(row + fVal - f0 - 1 - width)
        val hLeft = h.sub(row + fVal - f0 - 1)
        hLeft[0] = 0

        var rowInGap = false
        for (i in 0 until rowLen) {
            val char = rowT[i]
            val col = i + fVal
            var s1 = 0
            var consecutive = 0

            val s2 = hLeft[i] + if (rowInGap) SCORE_GAP_EXTENSION else SCORE_GAP_START

            if (rowPchar == char) {
                s1 = hDiag[i] + SCORE_MATCH
                var bonus = rowB[i]
                consecutive = cDiag[i] + 1

                if (bonus == BONUS_BOUNDARY) {
                    consecutive = 1
                } else if (consecutive > 1) {
                    bonus = max(bonus, max(BONUS_CONSECUTIVE, b[col - consecutive + 1]))
                }

                if (s1 + bonus < s2) {
                    s1 += rowB[i]
                    consecutive = 0
                } else {
                    s1 += bonus
                }
            }
            cSub[i] = consecutive

            rowInGap = s1 < s2
            val score = max(max(s1, s2), 0)
            if (rowPidx == m - 1 && ((forward && score > maxScore) || (!forward && score >= maxScore))) {
                maxScore = score
                maxScorePos = col
            }
            hSub[i] = score
        }
    }

    // Phase 4. (Optional) Backtrace to find character positions
    val pos = createPosSet(withPos)
    var j = f0
    if (pos != null) {
        var i = m - 1
        j = maxScorePos
        var preferMatch = true

        while (true) {
            val rowStart = i * width
            val j0 = j - f0
            val s = h[rowStart + j0]

            var s1 = 0
            var s2 = 0
            if (i > 0 && j >= f[i]) s1 = h[rowStart - width + j0 - 1]
            if (j > f[i]) s2 = h[rowStart + j0 - 1]

            if (s > s1 && (s > s2 || (s == s2 && preferMatch))) {
                pos.add(j)
                if (i == 0) break
                i--
            }

            preferMatch = c[rowStart + j0] > 1 ||
                    (rowStart + width + j0 + 1 < width * m && c[rowStart + width + j0 + 1] > 0)
            j--
        }
    }

    return AlgoResult(j, maxScorePos + 1, maxScore, pos)
}

private fun calculateScore(
    caseSensitive: Boolean,
    normalize: Boolean,
    text: IntArray,
    pattern: IntArray,
    sidx: Int,
    eidx: Int,
    withPos: Boolean,
): Pair<Int, MutableSet<Int>?> {
    var pidx = 0
    var score = 0
    var inGap = false
    var consecutive = 0
    var firstBonus = 0

    val pos = createPosSet(withPos)
    var prevCharClass = CharClass.NON_WORD
    if (sidx > 0) prevCharClass = charClassOf(text[sidx - 1])

    for (idx in sidx until eidx) {
        var rune = text[idx]
        val charClass = charClassOf(rune)

        if (!caseSensitive) {
            if (rune in CAPITAL_A_RUNE..CAPITAL_Z_RUNE) rune += 32
            else if (rune > MAX_ASCII) rune = lowerRune(rune)
        }

        if (normalize) rune = normalizeRune(rune)

        // The bounds check stands in for JS reading past the end of an array and getting
        // undefined, which never compares equal.
        if (pidx < pattern.size && rune == pattern[pidx]) {
            pos?.add(idx)

            score += SCORE_MATCH
            var bonus = bonusFor(prevCharClass, charClass)

            if (consecutive == 0) {
                firstBonus = bonus
            } else {
                if (bonus == BONUS_BOUNDARY) firstBonus = bonus
                bonus = max(max(bonus, firstBonus), BONUS_CONSECUTIVE)
            }

            score += if (pidx == 0) bonus * BONUS_FIRST_CHAR_MULTIPLIER else bonus

            inGap = false
            consecutive++
            pidx++
        } else {
            score += if (inGap) SCORE_GAP_EXTENSION else SCORE_GAP_START
            inGap = true
            consecutive = 0
            firstBonus = 0
        }
        prevCharClass = charClass
    }

    return score to pos
}

internal fun fuzzyMatchV1(
    caseSensitive: Boolean,
    normalize: Boolean,
    forward: Boolean,
    text: IntArray,
    pattern: IntArray,
    withPos: Boolean,
    slabArg: Slab?,
): AlgoResult {
    if (pattern.isEmpty()) return AlgoResult(0, 0, 0, null)
    if (asciiFuzzyIndex(text, pattern, caseSensitive) < 0) return NO_MATCH

    var pidx = 0
    var sidx = -1
    var eidx = -1

    val lenRunes = text.size
    val lenPattern = pattern.size

    for (index in 0 until lenRunes) {
        var rune = text[indexAt(index, lenRunes, forward)]

        if (!caseSensitive) {
            if (rune in CAPITAL_A_RUNE..CAPITAL_Z_RUNE) rune += 32
            else if (rune > MAX_ASCII) rune = lowerRune(rune)
        }

        if (normalize) rune = normalizeRune(rune)

        if (rune == pattern[indexAt(pidx, lenPattern, forward)]) {
            if (sidx < 0) sidx = index
            pidx++
            if (pidx == lenPattern) {
                eidx = index + 1
                break
            }
        }
    }

    if (sidx < 0 || eidx < 0) return NO_MATCH

    pidx--
    for (index in eidx - 1 downTo sidx) {
        var rune = text[indexAt(index, lenRunes, forward)]

        if (!caseSensitive) {
            if (rune in CAPITAL_A_RUNE..CAPITAL_Z_RUNE) rune += 32
            else if (rune > MAX_ASCII) rune = lowerRune(rune)
        }

        if (rune == pattern[indexAt(pidx, lenPattern, forward)]) {
            pidx--
            if (pidx < 0) {
                sidx = index
                break
            }
        }
    }

    if (!forward) {
        val sidxTemp = sidx
        sidx = lenRunes - eidx
        eidx = lenRunes - sidxTemp
    }

    val (score, pos) = calculateScore(caseSensitive, normalize, text, pattern, sidx, eidx, withPos)
    return AlgoResult(sidx, eidx, score, pos)
}

internal fun exactMatchNaive(
    caseSensitive: Boolean,
    normalize: Boolean,
    forward: Boolean,
    text: IntArray,
    pattern: IntArray,
    withPos: Boolean,
    slabArg: Slab?,
): AlgoResult {
    if (pattern.isEmpty()) return AlgoResult(0, 0, 0, null)

    val lenRunes = text.size
    val lenPattern = pattern.size

    if (lenRunes < lenPattern) return NO_MATCH
    if (asciiFuzzyIndex(text, pattern, caseSensitive) < 0) return NO_MATCH

    var pidx = 0
    var bestPos = -1
    var bonus = 0
    var bestBonus = -1

    var index = 0
    while (index < lenRunes) {
        val textIdx = indexAt(index, lenRunes, forward)
        var rune = text[textIdx]

        if (!caseSensitive) {
            if (rune in CAPITAL_A_RUNE..CAPITAL_Z_RUNE) rune += 32
            else if (rune > MAX_ASCII) rune = lowerRune(rune)
        }

        if (normalize) rune = normalizeRune(rune)

        val patternIdx = indexAt(pidx, lenPattern, forward)

        if (pattern[patternIdx] == rune) {
            if (patternIdx == 0) bonus = bonusAt(text, textIdx)

            pidx++
            if (pidx == lenPattern) {
                if (bonus > bestBonus) {
                    bestPos = index
                    bestBonus = bonus
                }
                if (bonus == BONUS_BOUNDARY) break

                index -= pidx - 1
                pidx = 0
                bonus = 0
            }
        } else {
            index -= pidx
            pidx = 0
            bonus = 0
        }
        index++
    }

    if (bestPos < 0) return NO_MATCH

    val sidx: Int
    val eidx: Int
    if (forward) {
        sidx = bestPos - lenPattern + 1
        eidx = bestPos + 1
    } else {
        sidx = lenRunes - (bestPos + 1)
        eidx = lenRunes - (bestPos - lenPattern + 1)
    }

    val (score, _) = calculateScore(caseSensitive, normalize, text, pattern, sidx, eidx, false)
    return AlgoResult(sidx, eidx, score, null)
}

internal fun prefixMatch(
    caseSensitive: Boolean,
    normalize: Boolean,
    forward: Boolean,
    text: IntArray,
    pattern: IntArray,
    withPos: Boolean,
    slabArg: Slab?,
): AlgoResult {
    if (pattern.isEmpty()) return AlgoResult(0, 0, 0, null)

    val trimmedLen = if (isWhitespace(pattern[0])) 0 else whitespacesAtStart(text)
    if (text.size - trimmedLen < pattern.size) return NO_MATCH

    for (index in pattern.indices) {
        var rune = text[trimmedLen + index]
        if (!caseSensitive) rune = lowerRune(rune)
        if (normalize) rune = normalizeRune(rune)
        if (rune != pattern[index]) return NO_MATCH
    }

    val lenPattern = pattern.size
    val (score, _) = calculateScore(
        caseSensitive, normalize, text, pattern, trimmedLen, trimmedLen + lenPattern, false,
    )
    return AlgoResult(trimmedLen, trimmedLen + lenPattern, score, null)
}

internal fun suffixMatch(
    caseSensitive: Boolean,
    normalize: Boolean,
    forward: Boolean,
    text: IntArray,
    pattern: IntArray,
    withPos: Boolean,
    slabArg: Slab?,
): AlgoResult {
    var trimmedLen = text.size
    if (pattern.isEmpty() || !isWhitespace(pattern[pattern.size - 1])) {
        trimmedLen -= whitespacesAtEnd(text)
    }

    if (pattern.isEmpty()) return AlgoResult(trimmedLen, trimmedLen, 0, null)

    val diff = trimmedLen - pattern.size
    if (diff < 0) return NO_MATCH

    for (index in pattern.indices) {
        var rune = text[index + diff]
        if (!caseSensitive) rune = lowerRune(rune)
        if (normalize) rune = normalizeRune(rune)
        if (rune != pattern[index]) return NO_MATCH
    }

    val sidx = trimmedLen - pattern.size
    val (score, _) = calculateScore(
        caseSensitive,
        normalize,
        text,
        pattern,
        sidx,
        trimmedLen,
        false
    )
    return AlgoResult(sidx, trimmedLen, score, null)
}

internal fun equalMatch(
    caseSensitive: Boolean,
    normalize: Boolean,
    forward: Boolean,
    text: IntArray,
    pattern: IntArray,
    withPos: Boolean,
    slabArg: Slab?,
): AlgoResult {
    val lenPattern = pattern.size
    if (lenPattern == 0) return NO_MATCH

    val trimmedLen = if (isWhitespace(pattern[0])) 0 else whitespacesAtStart(text)
    val trimmedEndLen = if (isWhitespace(pattern[lenPattern - 1])) 0 else whitespacesAtEnd(text)

    if (text.size - trimmedLen - trimmedEndLen != lenPattern) return NO_MATCH

    var match = true
    if (normalize) {
        for (idx in pattern.indices) {
            var rune = text[trimmedLen + idx]
            if (!caseSensitive) rune = lowerRune(rune)
            if (normalizeRune(pattern[idx]) != normalizeRune(rune)) {
                match = false
                break
            }
        }
    } else {
        var textStr = runesToStr(text).substring(trimmedLen, text.size - trimmedEndLen)
        if (!caseSensitive) textStr = textStr.lowercase()
        match = textStr == runesToStr(pattern)
    }

    if (!match) return NO_MATCH

    val score = (SCORE_MATCH + BONUS_BOUNDARY) * lenPattern +
            (BONUS_FIRST_CHAR_MULTIPLIER - 1) * BONUS_BOUNDARY
    return AlgoResult(trimmedLen, trimmedLen + lenPattern, score, null)
}
