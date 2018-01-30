package com.spcore.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.spcore.R
import com.spcore.apis.FrontendInterface
import com.spcore.helpers.Auth
import com.spcore.helpers.HardcodedStuff
import com.spcore.helpers.isOnline
import kotlinx.android.synthetic.main.activity_initial_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.longToast

class InitialLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_login)

        /** @return Returns true if [username_input] has errors */
        fun checkUsernameErrors() : Boolean {
            username_input.apply {
                error = when {
                    text.trim().isEmpty() ->
                        "This field is required"
                    text.length < 6 ->
                        "Username needs to be at least 6 characters long"
                    text.length > 25 ->
                        "Username cannot be more than 25 characters long"
                    !text.matches(Regex("""^(\w|[._])*[a-zA-Z]+(\w|[._])*$""")) ->
                        "Username must have at least one letter, and may consist of alphanumerics, dots, or underscores only"
                    else -> null
                }
                if (error != null)
                    requestFocus()
            }

            return username_input.error != null
        }

        /** @return Returns true if [displayed_name_input] has errors */
        fun checkDisplayNameErrors() : Boolean {
            displayed_name_input.apply {
                error =
                        if (text.isBlank() && text.isNotEmpty())
                            "Why are you doing this?"
                        else if (text.matches(Regex("""[a-zA-Z ]""")))
                            "Only alphabets and spaces allowed in names"
                        else
                            null
                if (error != null)
                    requestFocus()
            }

            return displayed_name_input.error != null
        }

        /**
         * Start the [SubmitInitTask]. Note that this will also call and evaluate all client-side
         * validation checkers
         */
        fun submitInfo() {

            if (!isOnline(this)) {
                longToast("Internet connection required")
                return
            }

            // the usage of the 'or' infix function instead of the lazy disjunction '||' operator
            // is such that both operands will be evaluated, whether or not the first operand
            // yields a value, in this case 'true', that will make the second ineffective to the
            // result of the disjunction
            val error = checkDisplayNameErrors() or checkUsernameErrors()

            if(!error) {

                // Make sure the user doesn't try to BM and put in a shit ton of spaces
                displayed_name_input.apply {
                    text = SpannableStringBuilder(
                            text
                                    .split(' ')
                                    .filter{it.isNotEmpty()}
                                    .let {
                                        if (it.isNotEmpty())
                                            it.reduce { acc, s -> acc + " " + s }
                                        else
                                            ""
                                    })
                }

                initial_login_progress_indicator.visibility = View.VISIBLE
                loadingText.visibility = View.VISIBLE
                form.visibility = View.GONE

                async(UI) {
                    val asyncRes =
                            bg {
                                FrontendInterface.setUserInitializedOnServer(
                                        username_input.text.toString(),
                                        displayed_name_input.text.toString())
                            }
                    val res = asyncRes.await()

                    initial_login_progress_indicator.visibility = View.GONE
                    loadingText.visibility = View.GONE
                    form.visibility = View.VISIBLE

                    when(res) {
                        SubmitInitStatus.USERNAME_TAKEN -> {
                            username_input.error = "Username taken"
                            username_input.requestFocus()
                        }
                        is SubmitInitStatus.UNKNOWN_ERROR -> {
                            Toast.makeText(this@InitialLoginActivity, "Unknown error: ${res.errmsg}", Toast.LENGTH_SHORT)
                                    .show()
                        }
                        SubmitInitStatus.SUCCESS -> {
                            HardcodedStuff.initialize(Auth.user)
                            startActivity(Intent(this@InitialLoginActivity, HomeActivity::class.java))
                        }
                    }
                }
            }
        }

        username_input.setOnFocusChangeListener { view, isFocused ->
            username_description.visibility = if(isFocused)
                View.VISIBLE
            else
                View.GONE
        }


        username_input.setOnEditorActionListener listener@ { textView, i, keyEvent ->
            if(i == EditorInfo.IME_ACTION_NEXT) {
                if(!checkUsernameErrors())
                    displayed_name_input.requestFocus()
                return@listener true
            }

            false
        }

        displayed_name_input.setOnFocusChangeListener { view, isFocused ->
            displayed_name_description.visibility = if(isFocused)
                View.VISIBLE
            else {
                View.GONE
            }
        }

        displayed_name_input.setOnEditorActionListener listener@ { textView, i, keyEvent ->
            if(i == EditorInfo.IME_ACTION_GO) {
                submitInfo()

                return@listener true
            }

            false
        }

        submit_button.setOnClickListener {
            submitInfo()
        }


    }

    sealed class SubmitInitStatus {
        object SUCCESS : SubmitInitStatus()
        object USERNAME_TAKEN : SubmitInitStatus()
        class UNKNOWN_ERROR(val errmsg: String) : SubmitInitStatus()
    }
}
