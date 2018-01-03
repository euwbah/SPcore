package com.spcore.helpers

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.spcore.R
import org.jetbrains.anko.backgroundColor


object DoubleSnack {
    fun show(view: View, text: String, action1Text: String, action2Text: String,
             action1CB: (Snackbar) -> Unit, action2CB: (Snackbar) -> Unit,
             highlightAction1: Boolean = false, highlightAction2: Boolean = false,
             snackbarLength: Int = Snackbar.LENGTH_INDEFINITE): Snackbar {

        val ctx = view.context
        // Create the Snackbar
        val objLayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val snackbar = Snackbar.make(view, "", snackbarLength)

        // Get the Snackbar's layout view
        val layout = snackbar.view as Snackbar.SnackbarLayout
        layout.setPadding(0, 0, 0, 0)

        layout.backgroundColor = 0
        // Hide the original text
        val textView = layout.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE


        // Inflate custom view
        val mInflater = ctx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val snackView = mInflater.inflate(R.layout.template_double_action_snackbar, null)

        snackView[R.id.dbl_snack_text, TextView::class.java]?.apply {
            this.text = text
        }

        snackView[R.id.dbl_snack_action1, TextView::class.java]?.apply {
            this.text = action1Text
            this.backgroundColor = if(highlightAction1) 0xFF555555.toInt() else 0x00000000
            setOnClickListener {
                action1CB(snackbar)
            }
        }

        snackView[R.id.dbl_snack_action2, TextView::class.java]?.apply {
            this.text = action2Text
            this.backgroundColor = if(highlightAction2) 0xFF555555.toInt() else 0x00000000
            setOnClickListener {
                action2CB(snackbar)
            }
        }

        // Add the view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams)
        // Show the Snackbar
        snackbar.show()

        return snackbar
    }
}