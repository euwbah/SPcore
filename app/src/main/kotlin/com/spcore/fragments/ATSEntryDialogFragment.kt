package com.spcore.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.spcore.R
import com.spcore.models.Lesson
import com.spcore.services.intents.SendATSIntentService
import kotlinx.coroutines.experimental.*
import kotlinx.android.synthetic.main.fragment_atsentry_dialog.*

/** String */
private const val ARG_ERR_MSG = "errmsg"
/** Long */
private const val ARG_LESSON = "lesson"

/**
 * Dialog Fragment for ATS entry modal dialog
 * Activities that contain this fragment must implement the
 * [ATSEntryDialogFragment.ATSDialogEventsHander] interface
 * to handle interaction events.
 * Use the [ATSEntryDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ATSEntryDialogFragment : DialogFragment() {

    private var listener: ATSDialogEventsHander? = null

    var errmsg: String
        get() = ats_error_message?.text?.toString() ?: ""
        set(value) {
            ats_error_message?.text = value
            value
        }

    lateinit var lesson: Lesson


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("CREATE", "oaiegaerg")

        arguments?.let {
            errmsg = it.getString(ARG_ERR_MSG)
            lesson = it.getParcelable(ARG_LESSON)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("CREATE VIEW", "oaiegaerg")

        val view = inflater.inflate(R.layout.fragment_atsentry_dialog, container, false)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        // This async is necessary as it functions akin to Platform.runLater in JavaFX,
        // giving the synthetic properties time to load
        async {
            ats_input.requestFocus()
            ats_error_message.text = errmsg
            submit_ats_button.setOnClickListener {

                listener?.onSubmitATSButtonClick()

                ats_error_message.text = ats_input.text.length.let {
                    when {
                        it == 0 -> "Input is empty"
                        it != 6 -> "Invalid code"
                        else -> {
                            SendATSIntentService.startNew(this@ATSEntryDialogFragment.context!!, lesson, ats_input.text.toString())
                            ""
                        }
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("ATTACH", "oaiegaerg")

        if(context is ATSDialogEventsHander) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement ATSDialogEventsHandler")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("DETACH", "oaiegaerg")
    }

    companion object {
        @JvmStatic
        fun newInstance(lesson: Lesson, errmsg: String = "") =
                ATSEntryDialogFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_LESSON, lesson)
                        putString(ARG_ERR_MSG, errmsg)
                    }
                }
    }

    interface ATSDialogEventsHander {
        fun onSubmitATSButtonClick()
    }
}
