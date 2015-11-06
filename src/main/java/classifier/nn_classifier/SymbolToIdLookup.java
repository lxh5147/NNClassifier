package classifier.nn_classifier;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import com.google.common.collect.Maps;

public class SymbolToIdLookup {
    public static final int UNK = 0;

    public int getId(String symbol) {
        checkNotNull(symbol, "symbol");
        // first try the memory optimized lookup
        if (canEncode(symbol)) {
            long encodedSymbol = encode(symbol);
            int id = this.symbolToIdLookupWithLongArray.get(encodedSymbol);
            return id >= 0 ? id : UNK;
        }
        return this.symbolToIdMap.containsKey(symbol)
                ? this.symbolToIdMap.get(symbol) : UNK;
    }

    public static SymbolToIdLookup load(File file) throws IOException {
        checkNotNull(file, "file");
        String symbolToIdLookupKey = file.getCanonicalPath();
        if (SYMBOL_TO_ID_LOOKUP_TABLES.containsKey(symbolToIdLookupKey)) {
            return SYMBOL_TO_ID_LOOKUP_TABLES.get(symbolToIdLookupKey);
        }

        int encodedLongArraySize = 0;
        Map<String, Integer> symbolToIdMap = Maps.newHashMap();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF8"))) {
            String line = null;
            int id=-1;
            while ((line = reader.readLine()) != null) {
                String symbol=line;
                ++id;
                if (canEncode(symbol)) {
                    encodedLongArraySize++;
                } else {
                    symbolToIdMap.put(symbol, id);
                }
            }
        }
        long[] symbols = new long[encodedLongArraySize];
        int[] ids = new int[encodedLongArraySize];
        int i = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF8"))) {
            String line = null;
            int id=-1;
            while ((line = reader.readLine()) != null) {
                String symbol=line;
                ++id;
                if (canEncode(symbol)) {
                    symbols[i] = encode(symbol);
                    ids[i] = id;
                    i++;
                }
            }
            SymbolToIdLookup symbolToIdLookup = new SymbolToIdLookup(
                    buildSymboToIdLookupWithSortedLongArray(symbols, ids),
                    symbolToIdMap);
            SYMBOL_TO_ID_LOOKUP_TABLES.put(symbolToIdLookupKey,
                    symbolToIdLookup);
            return symbolToIdLookup;
        }
    }

    private SymbolToIdLookup(
            SymboToIdLookupWithSortedLongArray symbolToIdLookupWithArray,
            Map<String, Integer> symbolToIdMap) {
        this.symbolToIdLookupWithLongArray = symbolToIdLookupWithArray;
        this.symbolToIdMap = Maps
                .newHashMap(checkNotNull(symbolToIdMap, "symbolToIdMap"));
    }

    /**
     * Defines the lookup that uses sorted long array to represent the string symbols.
     * @author xiaohua_liu
     *
     */
    static class SymboToIdLookupWithSortedLongArray {
        private final long[] symbols;
        private final int[] ids;

        SymboToIdLookupWithSortedLongArray(long[] symbols, int[] ids) {
            this.symbols = symbols;
            this.ids = ids;
        }

        int get(long symbol) {
            int pos = Arrays.binarySearch(this.symbols, symbol);
            if (pos < 0) {
                return -1;
            }
            return this.ids[pos];
        }
    }

    static long encode(String s) {
        int length = s.length();
        if (length > 8) {
            return -1;
        }
        long encoded = 0;
        for (int i = 0; i < length; ++i) {
            encoded += s.charAt(i) << (8 * i);
        }
        return encoded;
    }

    static boolean canEncode(String s) {
        int length = s.length();
        if (length > 8) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            char ch = s.charAt(i);
            if (ch > 255) {
                return false;
            }
        }
        return true;
    }

    private final Map<String, Integer> symbolToIdMap;
    private final SymboToIdLookupWithSortedLongArray symbolToIdLookupWithLongArray;

    private static Map<String, SymbolToIdLookup> SYMBOL_TO_ID_LOOKUP_TABLES = Maps
            .newConcurrentMap();

    static SymboToIdLookupWithSortedLongArray buildSymboToIdLookupWithSortedLongArray(
            long[] symbols, int[] ids) {
        int[] index = new DualPivotQuicksort(symbols, symbols.length).sort();
        long[] sortedSymbols = new long[symbols.length];
        int[] sortedIds = new int[ids.length];
        for (int i = 0; i < index.length; ++i) {
            sortedSymbols[i] = symbols[index[i]];
            sortedIds[i] = ids[index[i]];
        }
        return new SymboToIdLookupWithSortedLongArray(sortedSymbols, sortedIds);
    }

    /**
     * Updated JDK class to support index based sort for memory efficiency
     * consideration. This class implements the Dual-Pivot Quicksort algorithm
     * by Vladimir Yaroslavskiy, Jon Bentley, and Josh Bloch. The algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically faster
     * than traditional (one-pivot) Quicksort implementations.
     *
     * All exposed methods are package-private, designed to be invoked from
     * public methods (in class Arrays) after performing any necessary array
     * bounds checks and expanding parameters into the required forms.
     *
     * @author Vladimir Yaroslavskiy
     * @author Jon Bentley
     * @author Josh Bloch
     *
     * @version 2011.02.11 m765.827.12i:5\7pm
     * @since 1.7
     */
    static class DualPivotQuicksort {
        private final long[] values;
        private final int size;

        DualPivotQuicksort(long[] values, int size) {
            this.values = values;
            this.size = size;
        }

        int[] sort() {
            int[] a = new int[this.size];
            for (int i = 0; i < this.size; ++i) {
                a[i] = i;
            }

            if (this.size <= 1) {
                return a;
            }

            sort(a, 0, this.size - 1, null, 0, 0);
            return a;
        }

        private final long compare(int i, int j) {
            long v1 = this.values[i];
            long v2 = this.values[j];
            if (v1 >= 0) {
                if (v2 >= 0) {
                    return v1 - v2;
                }
                return 1;
            } else {
                if (v2 <= 0) {
                    return v1 - v2;
                }
                return -1;
            }
        }

        /*
         * Tuning parameters.
         */

        /**
         * The maximum number of runs in merge sort.
         */
        private static final int MAX_RUN_COUNT = 67;

        /**
         * The maximum length of run in merge sort.
         */
        private static final int MAX_RUN_LENGTH = 33;

        /**
         * If the length of an array to be sorted is less than this constant,
         * Quicksort is used in preference to merge sort.
         */
        private static final int QUICKSORT_THRESHOLD = 286;

        /**
         * If the length of an array to be sorted is less than this constant,
         * insertion sort is used in preference to Quicksort.
         */
        private static final int INSERTION_SORT_THRESHOLD = 47;

        /**
         * Sorts the specified range of the array using the given workspace
         * array slice if possible for merging
         *
         * @param a
         *            the array to be sorted
         * @param left
         *            the index of the first element, inclusive, to be sorted
         * @param right
         *            the index of the last element, inclusive, to be sorted
         * @param work
         *            a workspace array (slice)
         * @param workBase
         *            origin of usable space in work array
         * @param workLen
         *            usable size of work array
         */
        private void sort(int[] a, int left, int right, int[] work,
                int workBase, int workLen) {
            // Use Quicksort on small arrays
            if (right - left < QUICKSORT_THRESHOLD) {
                sort(a, left, right, true);
                return;
            }

            /*
             * Index run[i] is the start of i-th run (ascending or descending
             * sequence).
             */
            int[] run = new int[MAX_RUN_COUNT + 1];
            int count = 0;
            run[0] = left;

            // Check if the array is nearly sorted
            for (int k = left; k < right; run[count] = k) {
                if (this.compare(a[k], a[k + 1]) < 0) { // ascending
                    while (++k <= right && this.compare(a[k - 1], a[k]) <= 0)
                        ;
                } else if (this.compare(a[k], a[k + 1]) > 0) { // descending
                    while (++k <= right && this.compare(a[k - 1], a[k]) >= 0)
                        ;
                    for (int lo = run[count] - 1, hi = k; ++lo < --hi;) {
                        int t = a[lo];
                        a[lo] = a[hi];
                        a[hi] = t;
                    }
                } else { // equal
                    for (int m = MAX_RUN_LENGTH; ++k <= right
                            && this.compare(a[k - 1], a[k]) == 0;) {
                        if (--m == 0) {
                            sort(a, left, right, true);
                            return;
                        }
                    }
                }

                /*
                 * The array is not highly structured, use Quicksort instead of
                 * merge sort.
                 */
                if (++count == MAX_RUN_COUNT) {
                    sort(a, left, right, true);
                    return;
                }
            }

            // Check special cases
            // Implementation note: variable "right" is increased by 1.
            if (run[count] == right++) { // The last run contains one element
                run[++count] = right;
            } else if (count == 1) { // The array is already sorted
                return;
            }

            // Determine alternation base for merge
            byte odd = 0;
            for (int n = 1; (n <<= 1) < count; odd ^= 1)
                ;

            // Use or create temporary array b for merging
            int[] b; // temp array; alternates with a
            int ao, bo; // array offsets from 'left'
            int blen = right - left; // space needed for b
            if (work == null || workLen < blen
                    || workBase + blen > work.length) {
                work = new int[blen];
                workBase = 0;
            }
            if (odd == 0) {
                System.arraycopy(a, left, work, workBase, blen);
                b = a;
                bo = 0;
                a = work;
                ao = workBase - left;
            } else {
                b = work;
                ao = 0;
                bo = workBase - left;
            }

            // Merging
            for (int last; count > 1; count = last) {
                for (int k = (last = 0) + 2; k <= count; k += 2) {
                    int hi = run[k], mi = run[k - 1];
                    for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                        if (q >= hi || p < mi
                                && this.compare(a[p + ao], a[q + ao]) <= 0) {
                            b[i + bo] = a[p++ + ao];
                        } else {
                            b[i + bo] = a[q++ + ao];
                        }
                    }
                    run[++last] = hi;
                }
                if ((count & 1) != 0) {
                    for (int i = right, lo = run[count - 1]; --i >= lo; b[i
                            + bo] = a[i + ao])
                        ;
                    run[++last] = right;
                }
                int[] t = a;
                a = b;
                b = t;
                int o = ao;
                ao = bo;
                bo = o;
            }
        }

        /**
         * Sorts the specified range of the array by Dual-Pivot Quicksort.
         *
         * @param a
         *            the array to be sorted
         * @param left
         *            the index of the first element, inclusive, to be sorted
         * @param right
         *            the index of the last element, inclusive, to be sorted
         * @param leftmost
         *            indicates if this part is the leftmost in the range
         */
        private void sort(int[] a, int left, int right, boolean leftmost) {
            int length = right - left + 1;

            // Use insertion sort on tiny arrays
            if (length < INSERTION_SORT_THRESHOLD) {
                if (leftmost) {
                    /*
                     * Traditional (without sentinel) insertion sort, optimized
                     * for server VM, is used in case of the leftmost part.
                     */
                    for (int i = left, j = i; i < right; j = ++i) {
                        int ai = a[i + 1];
                        while (this.compare(ai, a[j]) < 0) {
                            a[j + 1] = a[j];
                            if (j-- == left) {
                                break;
                            }
                        }
                        a[j + 1] = ai;
                    }
                } else {
                    /*
                     * Skip the longest ascending sequence.
                     */
                    do {
                        if (left >= right) {
                            return;
                        }
                    } while (this.compare(a[++left], a[left - 1]) >= 0);

                    /*
                     * Every element from adjoining part plays the role of
                     * sentinel, therefore this allows us to avoid the left
                     * range check on each iteration. Moreover, we use the more
                     * optimized algorithm, so called pair insertion sort, which
                     * is faster (in the context of Quicksort) than traditional
                     * implementation of insertion sort.
                     */
                    for (int k = left; ++left <= right; k = ++left) {
                        int a1 = a[k], a2 = a[left];

                        if (this.compare(a1, a2) < 0) {
                            a2 = a1;
                            a1 = a[left];
                        }
                        while (this.compare(a1, a[--k]) < 0) {
                            a[k + 2] = a[k];
                        }
                        a[++k + 1] = a1;

                        while (this.compare(a2, a[--k]) < 0) {
                            a[k + 1] = a[k];
                        }
                        a[k + 1] = a2;
                    }
                    int last = a[right];

                    while (this.compare(last, a[--right]) < 0) {
                        a[right + 1] = a[right];
                    }
                    a[right + 1] = last;
                }
                return;
            }

            // Inexpensive approximation of length / 7
            int seventh = (length >> 3) + (length >> 6) + 1;

            /*
             * Sort five evenly spaced elements around (and including) the
             * center element in the range. These elements will be used for
             * pivot selection as described below. The choice for spacing these
             * elements was empirically determined to work well on a wide
             * variety of inputs.
             */
            int e3 = (left + right) >>> 1; // The midpoint
            int e2 = e3 - seventh;
            int e1 = e2 - seventh;
            int e4 = e3 + seventh;
            int e5 = e4 + seventh;

            // Sort these elements using insertion sort
            if (this.compare(a[e2], a[e1]) < 0) {
                int t = a[e2];
                a[e2] = a[e1];
                a[e1] = t;
            }

            if (this.compare(a[e3], a[e2]) < 0) {
                int t = a[e3];
                a[e3] = a[e2];
                a[e2] = t;
                if (this.compare(t, a[e1]) < 0) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
            if (this.compare(a[e4], a[e3]) < 0) {
                int t = a[e4];
                a[e4] = a[e3];
                a[e3] = t;
                if (this.compare(t, a[e2]) < 0) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (this.compare(t, a[e1]) < 0) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
            if (this.compare(a[e5], a[e4]) < 0) {
                int t = a[e5];
                a[e5] = a[e4];
                a[e4] = t;
                if (this.compare(t, a[e3]) < 0) {
                    a[e4] = a[e3];
                    a[e3] = t;
                    if (this.compare(t, a[e2]) < 0) {
                        a[e3] = a[e2];
                        a[e2] = t;
                        if (this.compare(t, a[e1]) < 0) {
                            a[e2] = a[e1];
                            a[e1] = t;
                        }
                    }
                }
            }

            // Pointers
            int less = left; // The index of the first element of center part
            int great = right; // The index before the first element of right
                               // part

            if (this.compare(a[e1], a[e2]) != 0
                    && this.compare(a[e2], a[e3]) != 0
                    && this.compare(a[e3], a[e4]) != 0
                    && this.compare(a[e4], a[e5]) != 0) {
                /*
                 * Use the second and fourth of the five sorted elements as
                 * pivots. These values are inexpensive approximations of the
                 * first and second terciles of the array. Note that pivot1 <=
                 * pivot2.
                 */
                int pivot1 = a[e2];
                int pivot2 = a[e4];

                /*
                 * The first and the last elements to be sorted are moved to the
                 * locations formerly occupied by the pivots. When partitioning
                 * is complete, the pivots are swapped back into their final
                 * positions, and excluded from subsequent sorting.
                 */
                a[e2] = a[left];
                a[e4] = a[right];

                /*
                 * Skip elements, which are less or greater than pivot values.
                 */
                while (this.compare(a[++less], pivot1) < 0)
                    ;
                while (this.compare(a[--great], pivot2) > 0)
                    ;

                /*
                 * Partitioning:
                 *
                 * left part center part right part
                 * +----------------------------
                 * ----------------------------------+ | < pivot1 | pivot1 <= &&
                 * <= pivot2 | ? | > pivot2 |
                 * +----------------------------------
                 * ----------------------------+ ^ ^ ^ | | | less k great
                 *
                 * Invariants:
                 *
                 * all in (left, less) < pivot1 pivot1 <= all in [less, k) <=
                 * pivot2 all in (great, right) > pivot2
                 *
                 * Pointer k is the first index of ?-part.
                 */
                outer: for (int k = less - 1; ++k <= great;) {
                    int ak = a[k];
                    if (this.compare(ak, pivot1) < 0) { // Move a[k] to left
                                                        // part
                        a[k] = a[less];
                        /*
                         * Here and below we use "a[i] = b; i++;" instead of
                         * "a[i++] = b;" due to performance issue.
                         */
                        a[less] = ak;
                        ++less;
                    } else if (this.compare(ak, pivot2) > 0) { // Move a[k] to
                                                               // right part
                        while (this.compare(a[great], pivot2) > 0) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (this.compare(a[great], pivot1) < 0) { // a[great] <=
                                                                  // pivot2
                            a[k] = a[less];
                            a[less] = a[great];
                            ++less;
                        } else { // pivot1 <= a[great] <= pivot2
                            a[k] = a[great];
                        }
                        /*
                         * Here and below we use "a[i] = b; i--;" instead of
                         * "a[i--] = b;" due to performance issue.
                         */
                        a[great] = ak;
                        --great;
                    }
                }

                // Swap pivots into their final positions
                a[left] = a[less - 1];
                a[less - 1] = pivot1;
                a[right] = a[great + 1];
                a[great + 1] = pivot2;

                // Sort left and right parts recursively, excluding known pivots
                sort(a, left, less - 2, leftmost);
                sort(a, great + 2, right, false);

                /*
                 * If center part is too large (comprises > 4/7 of the array),
                 * swap internal pivot values to ends.
                 */
                if (this.compare(less, e1) < 0 && this.compare(e5, great) < 0) {
                    /*
                     * Skip elements, which are equal to pivot values.
                     */
                    while (this.compare(a[less], pivot1) == 0) {
                        ++less;
                    }

                    while (this.compare(a[great], pivot2) == 0) {
                        --great;
                    }

                    /*
                     * Partitioning:
                     *
                     * left part center part right part
                     * +------------------------
                     * ----------------------------------+ | == pivot1 | pivot1
                     * < && < pivot2 | ? | == pivot2 |
                     * +--------------------------
                     * --------------------------------+ ^ ^ ^ | | | less k
                     * great
                     *
                     * Invariants:
                     *
                     * all in (*, less) == pivot1 pivot1 < all in [less, k) <
                     * pivot2 all in (great, *) == pivot2
                     *
                     * Pointer k is the first index of ?-part.
                     */
                    outer: for (int k = less - 1; ++k <= great;) {
                        int ak = a[k];
                        if (this.compare(ak, pivot1) == 0) { // Move a[k] to
                                                             // left part
                            a[k] = a[less];
                            a[less] = ak;
                            ++less;
                        } else if (this.compare(ak, pivot2) == 0) { // Move a[k]
                                                                    // to right
                                                                    // part
                            while (this.compare(a[great], pivot2) == 0) {
                                if (great-- == k) {
                                    break outer;
                                }
                            }
                            if (this.compare(a[great], pivot1) == 0) { // a[great]
                                                                       // <
                                                                       // pivot2
                                a[k] = a[less];
                                /*
                                 * Even though a[great] equals to pivot1, the
                                 * assignment a[less] = pivot1 may be incorrect,
                                 * if a[great] and pivot1 are floating-point
                                 * zeros of different signs. Therefore in float
                                 * and double sorting methods we have to use
                                 * more accurate assignment a[less] = a[great].
                                 */
                                a[less] = pivot1;
                                ++less;
                            } else { // pivot1 < a[great] < pivot2
                                a[k] = a[great];
                            }
                            a[great] = ak;
                            --great;
                        }
                    }
                }

                // Sort center part recursively
                sort(a, less, great, false);

            } else { // Partitioning with one pivot
                /*
                 * Use the third of the five sorted elements as pivot. This
                 * value is inexpensive approximation of the median.
                 */
                int pivot = a[e3];

                /*
                 * Partitioning degenerates to the traditional 3-way (or
                 * "Dutch National Flag") schema:
                 *
                 * left part center part right part
                 * +-------------------------------------------------+ | < pivot
                 * | == pivot | ? | > pivot |
                 * +-------------------------------------------------+ ^ ^ ^ | |
                 * | less k great
                 *
                 * Invariants:
                 *
                 * all in (left, less) < pivot all in [less, k) == pivot all in
                 * (great, right) > pivot
                 *
                 * Pointer k is the first index of ?-part.
                 */
                for (int k = less; k <= great; ++k) {
                    if (this.compare(a[k], pivot) == 0) {
                        continue;
                    }
                    int ak = a[k];
                    if (this.compare(ak, pivot) < 0) { // Move a[k] to left part
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else { // a[k] > pivot - Move a[k] to right part
                        while (this.compare(a[great], pivot) > 0) {
                            --great;
                        }
                        if (this.compare(a[great], pivot) < 0) { // a[great] <=
                                                                 // pivot
                            a[k] = a[less];
                            a[less] = a[great];
                            ++less;
                        } else { // a[great] == pivot
                            /*
                             * Even though a[great] equals to pivot, the
                             * assignment a[k] = pivot may be incorrect, if
                             * a[great] and pivot are floating-point zeros of
                             * different signs. Therefore in float and double
                             * sorting methods we have to use more accurate
                             * assignment a[k] = a[great].
                             */
                            a[k] = pivot;
                        }
                        a[great] = ak;
                        --great;
                    }
                }

                /*
                 * Sort left and right parts recursively. All elements from
                 * center part are equal and, therefore, already sorted.
                 */
                sort(a, left, less - 1, leftmost);
                sort(a, great + 1, right, false);
            }
        }

    }
}
