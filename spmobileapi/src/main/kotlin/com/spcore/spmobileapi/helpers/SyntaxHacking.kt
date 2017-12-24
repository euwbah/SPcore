package com.spcore.spmobileapi.helpers

import android.os.Parcel
import android.os.Parcelable

infix fun<V, T, R> Function1<T, R>.then(before: (V) -> T): (V) -> R {
    return { v: V -> this(before(v)) }
}

inline fun <reified T> parcelableCreator(crossinline create: (Parcel) -> T) =
        object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel) = create(source)
            override fun newArray(size: Int) = arrayOfNulls<T>(size)
        }