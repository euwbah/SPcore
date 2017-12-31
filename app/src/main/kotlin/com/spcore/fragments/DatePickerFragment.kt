package com.spcore.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import com.spcore.helpers.newCalendar
import java.util.*

private val ARG_TIMESTAMP = "timestamp"

/**
 * Use [DatePickerFragment.newInstance] to construct this, do NOT use the default constructor.
 * All contexts attaching the [DatePickerFragment] must also implement
 * [DateSetListener].
 */
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var listener: DateSetListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is DateSetListener)
            this.listener = context
        else
            throw RuntimeException("$context attaching DatePickerFragment should implement DateSetListener")
    }

    /**
     * @param month Note that the month param is 0-based
     */
    override fun onDateSet(picker: DatePicker?, year: Int, month: Int, day: Int) {
        listener?.onDatePicked(newCalendar(year, month + 1, day), tag ?: "")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = Calendar.getInstance().apply {
            timeInMillis = arguments!!.getLong(ARG_TIMESTAMP)
        }

        return DatePickerDialog(activity, this, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
    }

    interface DateSetListener {
        fun onDatePicked(calendar: Calendar, tag: String)
    }

    companion object {
        fun newInstance(timestamp: Long) =
            DatePickerFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TIMESTAMP, timestamp)
                }
            }

    }

}