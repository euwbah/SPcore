package com.spcore.helpers

/**
 * Basically allows for `x == y == z` kind of syntax via infix functions
 *
 * Use [_and] to chain, and end the chain with [equalTo]
 * E.g.: `x _and y _and blah _and asdfhjkl equalTo z`
 */
class CompoundEqualityBuilder<V>(val value: V, val stillRemainsTrue: Boolean) {
    fun _and(b: V): CompoundEqualityBuilder<V> {
        return CompoundEqualityBuilder(value, stillRemainsTrue && value == b)
    }

    infix fun equalTo(other: V): Boolean {
        return stillRemainsTrue && value == other
    }
}

infix fun <T> T._and(b: T) : CompoundEqualityBuilder<T> {
    return CompoundEqualityBuilder(this, this == b)
}