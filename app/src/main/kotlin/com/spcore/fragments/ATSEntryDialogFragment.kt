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
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ATSEntryDialogFragment.OnATSEntryListener] interface
 * to handle interaction events.
 * Use the [ATSEntryDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ATSEntryDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnATSEntryListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("CREATE", "oaiegaerg")

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ATSEntryDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ATSEntryDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
