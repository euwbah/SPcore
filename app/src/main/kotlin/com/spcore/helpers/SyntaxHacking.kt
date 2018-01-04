package com.spcore.helpers

import android.os.Parcel
import android.os.Parcelable
import android.support.design.widget.TextInputEditText
import android.text.Editable
import android.view.View
import android.widget.EditText
import com.spcore.R
import kotlinx.android.synthetic.main.activity_login.view.*
import java.security.KeyStore
import kotlin.reflect.KProperty

/**
 * A shorter way to type `x == y && y == z`
 *
 * Use [_and] to chain, and end the chain with [_is]
 * E.g.: `x _and y _and blah _and asdfhjkl _is z`
 */
class CompoundEqualityBuilder<V>(val value: V, val stillRemainsTrue: Boolean) {
    infix fun _and(b: V): CompoundEqualityBuilder<V> =
            CompoundEqualityBuilder(value, stillRemainsTrue && value == b)

    infix fun _is(other: V): Boolean = stillRemainsTrue && value == other
}

infix fun <T> T._and(b: T) : CompoundEqualityBuilder<T> = CompoundEqualityBuilder(this, this == b)

/**
 * A shorter way to type `x == c || y == c || z == c || ...`
 *
 * **NOTE that this is NOT lazily-evaluated**
 *
 * Use [_or] to chain, and end the chain with [_is]
 * E.g.: `x _or y _or z _or asdfhjkl _is c`
 */
class OptionalEqualityBuilder<V>(val value: MutableList<V>) {
    infix fun _or(b: V): OptionalEqualityBuilder<V> =
            OptionalEqualityBuilder(value.apply { add(b) })

    infix fun _is(constantValue: V): Boolean = value.any { it == constantValue }
}

infix fun <T> T._or(b: T) : OptionalEqualityBuilder<T> = OptionalEqualityBuilder(mutableListOf(this, b))


/**
 * Allows for using the array index access operator to
 * perform a findViewById on children of a [View] using [resourceID] values
 *
 * Usage:
 *
 * ```kotlin
 * val x: TextView = myParentView[R.id.some_text, TextView::class.java]
 * // OR
 * val y: EditText = myParentView.get<EditText>(R.id.some_edit_text)
 * ```
 *
 * @param resourceID The resource ID of the View to get (as per `R.id.<view id>`
 * @param clazz      Provide a class in order for type inference to work if using the
 *                   array element accessor syntax
 *
 * @return Returns the child of this [View] with the given [resourceID], or **null** if no
 *         child matches the [resourceID]
 */
operator fun <T : View> View.get(resourceID: Int, clazz: Class<T>? = null) : T? {
    return this.findViewById(resourceID) as T
}

/**
 * Instead of having to pass an [Editable] to set text on a [TextInputEditText] field,
 * use [textStr] and give it a [String] instead.
 */
var TextInputEditText.textStr : String
    get() = this.text.substring(0)
    set(value) {
        this.text.apply {
            clear()
            insert(0, value)
        }
    }

/**
 * Instead of having to pass an [Editable] to set text on a [EditText] field,
 * use [textStr] and give it a [String] instead.
 */
var EditText.textStr : String
    get() = this.text.substring(0)
    set(value) {
        this.text.apply {
            clear()
            insert(0, value)
        }
    }


inline fun <reified T> parcelableCreator(crossinline create: (Parcel) -> T) =
        object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel) = create(source)
            override fun newArray(size: Int) = arrayOfNulls<T>(size)
        }