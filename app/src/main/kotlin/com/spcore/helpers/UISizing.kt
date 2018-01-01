package com.spcore.helpers

import android.util.Log
import android.widget.ListView

/**
 * Sets ListView height dynamically based on the height of the items.
 *
 * @return true if the listView is successfully resized, false otherwise
 */
fun ListView.setHeightToWrapContent(): Boolean {

    val listAdapter = this.adapter

    if (listAdapter != null) {

        val numberOfItems = listAdapter.count

        // Get total height of all items.
        var totalItemsHeight = 0
        for (itemPos in 0 until numberOfItems) {
            val item = listAdapter.getView(itemPos, null, this)
            item.measure(0, 0)
            totalItemsHeight += item.measuredHeight
            Log.d("measured height", item.measuredHeight.toString())
        }

        // Get total height of all item dividers.
        val totalDividersHeight = this.dividerHeight * (numberOfItems - 1)

        // Set list height.
        val params = this.layoutParams
        params.height = totalItemsHeight + totalDividersHeight
        this.layoutParams = params
        this.requestLayout()

        return true

    } else {
        return false
    }

}