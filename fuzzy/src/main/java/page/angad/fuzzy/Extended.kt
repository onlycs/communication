package page.angad.fuzzy

internal class ExtendedMatch(
    val offsets: List<IntArray>,
    val totalScore: Int,
    val allPos: MutableSet<Int>,
)

private class TextToken(val text: IntArray, val prefixLength: Int)

private class IterResult(val start: Int, val end: Int, val score: Int, val positions: Set<Int>?)

private fun iter(
    algoFn: AlgoFn,
    tokens: List<TextToken>,
    caseSensitive: Boolean,
    normalize: Boolean,
    forward: Boolean,
    pattern: IntArray,
    slab: Slab,
): IterResult {
    for (part in tokens) {
        val res = algoFn(caseSensitive, normalize, forward, part.text, pattern, true, slab)
        if (res.start >= 0) {
            val sidx = res.start + part.prefixLength
            val eidx = res.end + part.prefixLength
            val positions = res.positions?.mapTo(LinkedHashSet()) { part.prefixLength + it }
            return IterResult(sidx, eidx, res.score, positions)
        }
    }
    return IterResult(-1, -1, 0, null)
}

internal fun computeExtendedMatch(
    text: IntArray,
    pattern: ExtendedPattern,
    fuzzyAlgo: AlgoFn,
    forward: Boolean,
): ExtendedMatch {
    val input = listOf(TextToken(text, 0))

    val offsets = mutableListOf<IntArray>()
    var totalScore = 0
    val allPos = LinkedHashSet<Int>()

    for (termSet in pattern.termSets) {
        var offset = intArrayOf(0, 0)
        var currentScore = 0
        var matched = false

        for (term in termSet) {
            val algoFn =
                if (term.type == TermType.FUZZY) fuzzyAlgo else algoFnForTermType(term.type)
            val res = iter(
                algoFn, input, term.caseSensitive, term.normalize, forward, term.text, slab,
            )

            if (res.start >= 0) {
                if (term.inv) continue

                offset = intArrayOf(res.start, res.end)
                currentScore = res.score
                matched = true

                if (res.positions != null) {
                    allPos.addAll(res.positions)
                } else {
                    for (idx in res.start until res.end) allPos.add(idx)
                }
                break
            } else if (term.inv) {
                offset = intArrayOf(0, 0)
                currentScore = 0
                matched = true
                continue
            }
        }

        if (matched) {
            offsets.add(offset)
            totalScore += currentScore
        }
    }

    return ExtendedMatch(offsets, totalScore, allPos)
}
