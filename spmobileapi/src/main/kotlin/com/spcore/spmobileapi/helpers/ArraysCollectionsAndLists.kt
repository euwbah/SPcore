package com.spcore.spmobileapi.helpers


fun <E> List<E>.subList(fromIndex: Int): List<E> {
    return this.subList(fromIndex, this.size)
}

/**
 * Reduce with initial value (useful for reducing collections of a different type than the reduced output)
 */
inline fun <I, T> Iterable<T>.reduce(initValue: I, operation: (acc: I, value: T) -> I): I {
    val iterator = this.iterator()
    if (!iterator.hasNext()) throw UnsupportedOperationException("Empty collection can't be reduced.")

    var accumulator = initValue
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

fun <T> Iterable<T>.reduceToString(operation: (acc: String, value: T) -> String): String {
    val iterator = this.iterator()
    if (!iterator.hasNext()) throw UnsupportedOperationException("Empty collection can't be reduced.")

    var accumulator: String = ""
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

inline fun <E> MutableList<E>.mutate(fromIndex: Int = 0, toIndex: Int = this.size, mutator: (E) -> E) {
    for(i in fromIndex until toIndex)
        this[i] = mutator(this[i])
}

inline fun <E> MutableList<E>.mutate(range: IntRange, mutator: (E) -> E)
    = mutate(range.start, range.last + 1, mutator)

inline fun <E> MutableList<E>.mutate(mutator: (E) -> E)
    = mutate(0, this.size, mutator)