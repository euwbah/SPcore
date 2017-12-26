package com.spcore.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
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
 * [ATSEntryDialogFragment.ATSDialogEventsHandler] interface
 * to handle interaction events.
 * Use the [ATSEntryDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ATSEntryDialogFragment : DialogFragment() {

    private var listener: ATSDialogEventsHandler? = null

    var errmsg: String
        get() = ats_error_message?.text?.toString() ?: ""
        set(value) {
            Handler(Looper.getMainLooper()).post {
                ats_error_message?.text = value
            }
            value
        }

    lateinit var lesson: Lesson


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("CREATE", "oaiegaerg")

        arguments?.let {
            lesson = it.getParcelable(ARG_LESSON)
            errmsg = it.getString(ARG_ERR_MSG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("CREATE VIEW", "oaiegaerg")

        val view = inflater.inflate(R.layout.fragment_atsentry_dialog, container, false)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        // This async is necessary as it functions akin to Platform.runLater in JavaFX,
        // giving the synthetic properties time to load
        Handler(Looper.getMainLooper()).post {
            ats_input.requestFocus()
            submit_ats_button.setOnClickListener {


                ats_error_message.text = ats_input.text.length.let {
                    when {
                        it == 0 -> "Input is empty"
                        it != 6 -> "Invalid code"
                        else -> {
                            listener?.onSuccessfulRequest()
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

        if(context is ATSDialogEventsHandler) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement ATSDialogEventsHandler")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("DETACH", "oaiegaerg")
    }

    /**
     * Also in attempt to work around #19917
     * [See StackOverflow](https://stackoverflow.com/a/15229490)
     */
    override fun show(manager: FragmentManager, tag: String?) {
        manager
                .beginTransaction()
                .add(this, tag)
                .commitAllowingStateLoss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // BUG #19917 Support Package
        // Causes java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        // super.onSaveInstanceState(outState)
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

    interface ATSDialogEventsHandler {
        /**
         * i.e., no client-side detected errors
         */
        fun onSuccessfulRequest()
    }
}
