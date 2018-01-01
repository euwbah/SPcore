package com.spcore.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.TimePicker
import com.spcore.helpers.Duration

private const val ARG_MIN = "minutes"
private const val ARG_HOUR = "hours"

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private var listener: TimeSetListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if(context is TimeSetListener)
            listener = context
        else
            throw RuntimeException("$context attaching TimePickerFragment must implement TimeSetListener!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(
                activity,
                this,
                arguments?.getInt(ARG_HOUR) ?: 0,
                arguments?.getInt(ARG_MIN) ?: 0,
                true)
    }

    override fun onTimeSet(view: TimePicker?, h: Int, m: Int) {
        listener?.onTimePicked(Duration(0, h, m), tag ?: "")
    }

    interface TimeSetListener {
        fun onTimePicked(duration: Duration, tag: String)
    }

    companion object {
        fun newInstance(duration: Duration) =
                TimePickerFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_MIN, duration.minutes)
                        putInt(ARG_HOUR, duration.hours)
                    }
                }
    }

}