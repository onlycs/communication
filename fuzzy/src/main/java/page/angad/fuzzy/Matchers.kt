package page.angad.fuzzy

private fun <T> resultFromScoreMap(
    scoreMap: Map<Int, List<FzfResultItem<T>>>,
    limit: Int,
): List<FzfResultItem<T>> {
    val result = mutableListOf<FzfResultItem<T>>()
    for (score in scoreMap.keys.sortedDescending()) {
        result += scoreMap.getValue(score)
        if (result.size >= limit) break
    }
    return result
}

/** Matches the query as one fuzzy term. This is the default [Matcher]. */
fun <T> basicMatch(fzf: Fzf<T>, query: String): List<FzfResultItem<T>> {
    val options = fzf.options
    val pattern = buildPatternForBasicMatch(query, options.casing, options.normalize)
    val scoreMap = HashMap<Int, MutableList<FzfResultItem<T>>>()

    for (idx in fzf.runesList.indices) {
        val itemRunes = fzf.runesList[idx]
        if (pattern.queryRunes.size > itemRunes.size) continue

        val match = fzf.algoFn(
            pattern.caseSensitive,
            options.normalize,
            options.forward,
            itemRunes,
            pattern.queryRunes,
            true,
            slab,
        )
        if (match.start == -1) continue

        // Exact matching does not report positions, so derive them from the matched span.
        val positions = if (options.fuzzy == FuzzyAlgo.EXACT) {
            (match.start until match.end).toCollection(LinkedHashSet())
        } else {
            match.positions ?: emptySet()
        }

        // Unsorted results all land in one bucket so that they come back in list order.
        val scoreKey = if (options.sort) match.score else 0
        scoreMap.getOrPut(scoreKey) { mutableListOf() }
            .add(FzfResultItem(fzf.items[idx], match.start, match.end, match.score, positions))
    }

    return resultFromScoreMap(scoreMap, options.limit)
}

/**
 * Matches fzf's extended search syntax, where a query is a set of space separated terms:
 * `'exact`, `^prefix`, `suffix$`, `^exact$`, `!inverse`, and `term1 | term2` for alternatives.
 */
fun <T> extendedMatch(fzf: Fzf<T>, query: String): List<FzfResultItem<T>> {
    val options = fzf.options
    val pattern = buildPatternForExtendedMatch(
        options.fuzzy != FuzzyAlgo.EXACT, options.casing, options.normalize, query,
    )
    val scoreMap = HashMap<Int, MutableList<FzfResultItem<T>>>()

    for (idx in fzf.runesList.indices) {
        val match = computeExtendedMatch(fzf.runesList[idx], pattern, fzf.algoFn, options.forward)
        if (match.offsets.size != pattern.termSets.size) continue

        var sidx = -1
        var eidx = -1
        if (match.allPos.isNotEmpty()) {
            sidx = match.allPos.min()
            eidx = match.allPos.max() + 1
        }

        val scoreKey = if (options.sort) match.totalScore else 0
        scoreMap.getOrPut(scoreKey) { mutableListOf() }
            .add(FzfResultItem(fzf.items[idx], sidx, eidx, match.totalScore, match.allPos))
    }

    return resultFromScoreMap(scoreMap, options.limit)
}
