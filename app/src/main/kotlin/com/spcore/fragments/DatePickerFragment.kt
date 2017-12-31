package com.spcore.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onAttach(context: Context) {
        super.onAttach(context)



    }

    /**
     * @param month Note that the month param is 0-based
     */
    override fun onDateSet(picker: DatePicker?, year: Int, month: Int, day: Int) {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val now = Calendar.getInstance()
        return DatePickerDialog(activity, this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
    }


}