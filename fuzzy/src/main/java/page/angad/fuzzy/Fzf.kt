package page.angad.fuzzy

/** How a query decides whether it should match case. */
enum class Casing {
    /** Case-sensitive only when the query itself contains an uppercase character. */
    SMART_CASE,
    CASE_SENSITIVE,
    CASE_INSENSITIVE,
}

/** Which scoring algorithm [Fzf] runs. */
enum class FuzzyAlgo {
    /** Single pass, cheaper, and does not always find the best match. */
    V1,

    /** Optimal scoring via a Smith-Waterman style matrix. The default. */
    V2,

    /** No fuzzy matching at all: the query has to appear as a contiguous substring. */
    EXACT,
}

/** A matched item, its score, and the item offsets that were matched. */
class FzfResultItem<T>(
    val item: T,
    /** Index of the first matched character, or -1 when nothing was matched. */
    val start: Int,
    /** Index one past the last matched character, or -1 when nothing was matched. */
    val end: Int,
    val score: Int,
    /** Every matched index, useful for highlighting. Not necessarily in ascending order. */
    val positions: Set<Int>,
)

/**
 * Breaks ties between two entries with the same score, like a [Comparator] with the option's
 * selector handed to it. Tiebreakers are tried left to right until one returns non-zero.
 */
typealias Tiebreaker<T> = (a: FzfResultItem<T>, b: FzfResultItem<T>, selector: (T) -> String) -> Int

/** Decides how a query string is turned into results. See [basicMatch] and [extendedMatch]. */
typealias Matcher<T> = (fzf: Fzf<T>, query: String) -> List<FzfResultItem<T>>

/** Orders shorter items first. */
fun <T> byLengthAsc(a: FzfResultItem<T>, b: FzfResultItem<T>, selector: (T) -> String): Int =
    selector(a.item).length - selector(b.item).length

/** Orders items whose match begins earlier first. */
fun <T> byStartAsc(a: FzfResultItem<T>, b: FzfResultItem<T>, selector: (T) -> String): Int =
    a.start - b.start

/**
 * @param selector picks the string to search out of each list item.
 * @param limit how many entries [Fzf.find] returns at most.
 * @param casing see [Casing].
 * @param normalize strips diacritics from list items, so a plain A-Z query still matches them.
 * @param fuzzy see [FuzzyAlgo].
 * @param tiebreakers see [Tiebreaker]. Ignored when [sort] is false.
 * @param sort orders results by descending score. When false, results keep their list order.
 * @param forward when false, matching runs from the end of the item, so "/breeds/pyrenees"
 *   queried with "re" highlights "pyrenees" rather than "breeds".
 * @param match see [Matcher]. Pass [extendedMatch] to enable fzf's search syntax.
 */
class FzfOptions<T>(
    val selector: (T) -> String,
    val limit: Int = Int.MAX_VALUE,
    val casing: Casing = Casing.SMART_CASE,
    val normalize: Boolean = true,
    val fuzzy: FuzzyAlgo = FuzzyAlgo.V2,
    val tiebreakers: List<Tiebreaker<T>> = emptyList(),
    val sort: Boolean = true,
    val forward: Boolean = true,
    val match: Matcher<T> = { fzf, query -> basicMatch(fzf, query) },
)

/**
 * A fuzzy finder over a fixed list.
 *
 * Items are converted to their searchable form once, up front, so reuse an instance across
 * queries and rebuild it when the list changes.
 */
class Fzf<T>(internal val items: List<T>, val options: FzfOptions<T>) {
    internal val runesList: List<IntArray> = items.map { strToRunes(options.selector(it).nfc()) }
    internal val algoFn: AlgoFn = when (options.fuzzy) {
        FuzzyAlgo.V1 -> ::fuzzyMatchV1
        FuzzyAlgo.V2 -> ::fuzzyMatchV2
        FuzzyAlgo.EXACT -> ::exactMatchNaive
    }

    fun find(query: String): List<FzfResultItem<T>> {
        if (query.isEmpty() || items.isEmpty()) {
            return items.take(options.limit).map { FzfResultItem(it, -1, -1, 0, emptySet()) }
        }

        return postProcess(options.match(this, query.nfc()), options)
    }
}

/** Builds a finder over plain strings. */
fun Fzf(items: List<String>): Fzf<String> = Fzf(items, FzfOptions(selector = { it }))

private fun <T> postProcess(
    result: List<FzfResultItem<T>>,
    options: FzfOptions<T>,
): List<FzfResultItem<T>> {
    var out = result

    if (options.sort && options.tiebreakers.isNotEmpty()) {
        // The matcher already emits entries grouped by descending score, so tiebreakers only
        // ever reorder within a run of equal scores. fzf-for-js gets this by handing JS a
        // comparator that returns 0 whenever the scores differ; that comparator is not
        // transitive and TimSort rejects it, hence sorting run by run instead.
        val sorted = ArrayList<FzfResultItem<T>>(result.size)
        var runStart = 0
        while (runStart < result.size) {
            var runEnd = runStart + 1
            while (runEnd < result.size && result[runEnd].score == result[runStart].score) runEnd++
            val run = result.subList(runStart, runEnd)
            sorted += if (run.size == 1) run else run.sortedWith(tieComparator(options))
            runStart = runEnd
        }
        out = sorted
    }

    return if (out.size > options.limit) out.subList(0, options.limit) else out
}

private fun <T> tieComparator(options: FzfOptions<T>) = Comparator<FzfResultItem<T>> { a, b ->
    for (tiebreaker in options.tiebreakers) {
        val diff = tiebreaker(a, b, options.selector)
        if (diff != 0) return@Comparator diff
    }
    0
}
