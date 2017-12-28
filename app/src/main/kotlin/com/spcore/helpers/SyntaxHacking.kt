package com.spcore.helpers

import android.os.Parcel
import android.os.Parcelable

/**
 * A shorter way to type `x == y && y == z`
 *
 * Use [_and] to chain, and end the chain with [_is]
 * E.g.: `x _and y _and blah _and asdfhjkl _is z`
 */
class CompoundEqualityBuilder<V>(val value: V, val stillRemainsTrue: Boolean) {
    fun _and(b: V): CompoundEqualityBuilder<V> =
            CompoundEqualityBuilder(value, stillRemainsTrue && value == b)

    infix fun _is(other: V): Boolean = stillRemainsTrue && value == other
}

infix fun <T> T._and(b: T) : CompoundEqualityBuilder<T> = CompoundEqualityBuilder(this, this == b)

/**
 * A shorter way to type `x == c || y == c || z == c || ...`
 *
 * Use [_and] to chain, and end the chain with [_is]
 * E.g.: `x _or y _or z _or asdfhjkl _is c`
 */
class OptionalEqualityBuilder<V>(val value: V, val wasTrueAlready: Boolean) {
    fun _or(b: V): OptionalEqualityBuilder<V> =
            OptionalEqualityBuilder(value, wasTrueAlready || value == b)

    infix fun _is(other: V): Boolean = wasTrueAlready || value == other
}

infix fun <T> T._or(b: T) : OptionalEqualityBuilder<T> = OptionalEqualityBuilder(this, false)

// Prepare uranuses for the ultimate hack...

inline fun <reified T> parcelableCreator(crossinline create: (Parcel) -> T) =
        object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel) = create(source)
            override fun newArray(size: Int) = arrayOfNulls<T>(size)
        }