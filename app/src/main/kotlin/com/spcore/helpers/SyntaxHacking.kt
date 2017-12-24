package com.spcore.helpers

import java.util.*

/**
 * A shorter way to type `x == y && y == z`
 *
 * Use [_and] to chain, and end the chain with [_is]
 * E.g.: `x _and y _and blah _and asdfhjkl _is z`
 */
class CompoundEqualityBuilder<V>(val value: V, val stillRemainsTrue: Boolean) {
    fun _and(b: V): CompoundEqualityBuilder<V> {
        return CompoundEqualityBuilder(value, stillRemainsTrue && value == b)
    }

    infix fun _is(other: V): Boolean {
        return stillRemainsTrue && value == other
    }
}

infix fun <T> T._and(b: T) : CompoundEqualityBuilder<T> {
    return CompoundEqualityBuilder(this, this == b)
}

/**
 * A shorter way to type `x == c || y == c || z == c || ...`
 *
 * Use [_and] to chain, and end the chain with [_is]
 * E.g.: `x _or y _or z _or asdfhjkl _is c`
 */
class OptionalEqualityBuilder<V>(val value: V, val stillRemainsTrue: Boolean) {
    fun _or(b: V): OptionalEqualityBuilder<V> {
        return OptionalEqualityBuilder(value, stillRemainsTrue || value == b)
    }

    infix fun _is(other: V): Boolean {
        return stillRemainsTrue || value == other
    }
}

infix fun <T> T._or(b: T) : OptionalEqualityBuilder<T> {
    return OptionalEqualityBuilder(this, this == b)
}
