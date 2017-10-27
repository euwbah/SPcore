package com.spcore.spmobileapi.helpers

infix fun<V, T, R> Function1<T, R>.then(before: (V) -> T): (V) -> R {
    return { v: V -> this(before(v)) }
}
