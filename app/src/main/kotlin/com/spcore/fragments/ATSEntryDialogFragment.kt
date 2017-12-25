package com.spcore.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.spcore.R
import com.spcore.services.intents.SendATSIntentService
import kotlinx.coroutines.experimental.*
import kotlinx.android.synthetic.main.fragment_atsentry_dialog.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_ERR_MSG = "errmsg"

/**
 * Dialog Fragment for ATS entry modal dialog
 * Activities that contain this fragment must implement the
 * [ATSEntryDialogFragment.OnATSEntryListener] interface
 * to handle interaction events.
 * Use the [ATSEntryDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ATSEntryDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var errmsg String = ""
    private var listener: OnATSEntryListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("CREATE", "oaiegaerg")

        arguments?.let {
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
        async {
            ats_input.requestFocus()
            ats_error_msg.text = errmsg
            submit_ats_button.setOnClickListener {
                ats_error_message.text = ats_input.text.length.let {
                    when {
                        it == 0 -> "Input is empty"
                        it != 6 -> "Invalid code"
                        else -> {
                            SendATSIntentService.startNew(this@ATSEntryDialogFragment.context!!, ats_input.text.toString())
                            ""
                        }
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        return view
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onATSSubmitted(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("ATTACH", "oaiegaerg")
        if (context is OnATSEntryListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnATSEntryListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("DETACH", "oaiegaerg")
        listener = null
    }

    interface OnATSEntryListener {
        // TODO: Update argument type and name
        fun onATSSubmitted(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(errmsg: String = "") =
                ATSEntryDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_ERR_MSG, param1)
                    }
                }
    }
}
