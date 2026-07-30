package page.angad.fuzzy

internal enum class TermType { FUZZY, EXACT, PREFIX, SUFFIX, EQUAL }

internal fun algoFnForTermType(type: TermType): AlgoFn = when (type) {
    TermType.FUZZY -> ::fuzzyMatchV2
    TermType.EXACT -> ::exactMatchNaive
    TermType.PREFIX -> ::prefixMatch
    TermType.SUFFIX -> ::suffixMatch
    TermType.EQUAL -> ::equalMatch
}

internal class Term(
    val type: TermType,
    val inv: Boolean,
    val text: IntArray,
    val caseSensitive: Boolean,
    val normalize: Boolean,
)

internal class ExtendedPattern(
    val str: String,
    val termSets: List<List<Term>>,
    val sortable: Boolean,
    val cacheable: Boolean,
    val fuzzy: Boolean,
)

private val SPACE_GROUP = Regex(" +")

internal fun buildPatternForExtendedMatch(
    fuzzy: Boolean,
    caseMode: Casing,
    normalize: Boolean,
    query: String,
): ExtendedPattern {
    var cacheable = true
    var str = query.trimStartLikeJs()

    // Trailing whitespace goes, except for a single space that was escaped as "\ ".
    val trimmedAtRight = str.trimEndLikeJs()
    str = if (trimmedAtRight.endsWith("\\") && str.getOrNull(trimmedAtRight.length) == ' ') {
        "$trimmedAtRight "
    } else {
        trimmedAtRight
    }

    var sortable = false
    val termSets = parseTerms(fuzzy, caseMode, normalize, str)

    loop@ for (termSet in termSets) {
        for ((idx, term) in termSet.withIndex()) {
            if (!term.inv) sortable = true

            if (!cacheable || idx > 0 || term.inv ||
                (fuzzy && term.type != TermType.FUZZY) ||
                (!fuzzy && term.type != TermType.EXACT)
            ) {
                cacheable = false
                if (sortable) break@loop
            }
        }
    }

    return ExtendedPattern(str, termSets, sortable, cacheable, fuzzy)
}

private fun parseTerms(
    fuzzy: Boolean,
    caseMode: Casing,
    normalize: Boolean,
    query: String,
): List<List<Term>> {
    // A backslash-escaped space is parked as a tab so that it survives the split on spaces.
    val tokens = query.replace("\\ ", "\t").split(SPACE_GROUP)

    val sets = mutableListOf<List<Term>>()
    var set = mutableListOf<Term>()
    var switchSet = false
    var afterBar = false

    for (token in tokens) {
        var type = TermType.FUZZY
        var inv = false
        var text = token.replace("\t", " ")
        val lowerText = text.lowercase()

        val caseSensitive = caseMode == Casing.CASE_SENSITIVE ||
                (caseMode == Casing.SMART_CASE && text != lowerText)

        val normalizeTerm = normalize &&
                lowerText == runesToStr(IntArray(lowerText.length) { normalizeRune(lowerText[it].code) })

        if (!caseSensitive) text = lowerText
        if (!fuzzy) type = TermType.EXACT

        if (set.isNotEmpty() && !afterBar && text == "|") {
            switchSet = false
            afterBar = true
            continue
        }
        afterBar = false

        if (text.startsWith("!")) {
            inv = true
            type = TermType.EXACT
            text = text.substring(1)
        }

        if (text != "$" && text.endsWith("$")) {
            type = TermType.SUFFIX
            text = text.dropLast(1)
        }

        if (text.startsWith("'")) {
            type = if (fuzzy && !inv) TermType.EXACT else TermType.FUZZY
            text = text.substring(1)
        } else if (text.startsWith("^")) {
            type = if (type == TermType.SUFFIX) TermType.EQUAL else TermType.PREFIX
            text = text.substring(1)
        }

        if (text.isEmpty()) continue

        if (switchSet) {
            sets.add(set)
            set = mutableListOf()
        }

        var textRunes = strToRunes(text)
        if (normalizeTerm) textRunes = IntArray(textRunes.size) { normalizeRune(textRunes[it]) }

        set.add(Term(type, inv, textRunes, caseSensitive, normalizeTerm))
        switchSet = true
    }

    if (set.isNotEmpty()) sets.add(set)

    return sets
}

internal class BasicPattern(val queryRunes: IntArray, val caseSensitive: Boolean)

internal fun buildPatternForBasicMatch(
    query: String,
    casing: Casing,
    normalize: Boolean,
): BasicPattern {
    var text = query
    val caseSensitive = when (casing) {
        Casing.SMART_CASE -> text.lowercase() != text
        Casing.CASE_SENSITIVE -> true
        Casing.CASE_INSENSITIVE -> {
            text = text.lowercase()
            false
        }
    }

    var queryRunes = strToRunes(text)
    if (normalize) queryRunes = IntArray(queryRunes.size) { normalizeRune(queryRunes[it]) }

    return BasicPattern(queryRunes, caseSensitive)
}
