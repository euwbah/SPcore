package com.spcore.activities

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService

import android.os.AsyncTask

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast


import com.spcore.R
import com.spcore.helpers.Auth
import com.spcore.apis.FrontendInterface
import com.spcore.apis.LoginResponse
import com.spcore.helpers.onAnimationEnd
import com.spcore.spmobileapi.SPMobileAPI
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.contentView

class LoginActivity : AppStateTrackerActivity("LoginActivity") {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        // Assuming LoginActivity is the first activity:

        SPMobileAPI.inititialize(getSharedPreferences(getString(R.string.cookie_storage_shared_preference_id), Context.MODE_PRIVATE))

        password.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        sign_in_button.setOnClickListener { attemptLogin() }

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        admin_no.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val adminNoStr = admin_no.text.toString()
        val passwordStr = password.text.toString()

        var problem = false
        var focusView: View? = null

        val isValidAdminNo = {
            x: String -> x.matches(Regex("[pP]?\\d{7}"))
        }

        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_field_required)
            focusView = password
            problem = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(adminNoStr)) {
            admin_no.error = getString(R.string.error_field_required)
            focusView = admin_no
            problem = true
        } else if (!isValidAdminNo(adminNoStr)) {
            admin_no.error = getString(R.string.error_invalid_admin_no)
            focusView = admin_no
            problem = true
        }

        if (problem) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            val adminNoStrWithoutP = adminNoStr.replace("p", "", true)

            // Make sure to clear focus and hide keyboard before attempting to log in
            // This fixes a bug in which the constraint layout will screw up
            // if the soft input is open whilst moving between activities.

            focusView?.clearFocus()
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(contentView?.windowToken, 0)

            mAuthTask = UserLoginTask(adminNoStrWithoutP, passwordStr)
            mAuthTask?.execute()
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        login_form_layout.visibility = if (show) View.GONE else View.VISIBLE
        login_form_layout.animate()
                .setDuration(shortAnimTime.toLong())
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(
                    onAnimationEnd {
                        login_form_layout.visibility = if (show) View.GONE else View.VISIBLE
                    }
                )

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime.toLong())
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(
                    onAnimationEnd {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                )
    }

    sealed class LoginStatus {
        class SUCCESS(val response: LoginResponse) : LoginStatus()
        object INVALID_CREDENTIALS : LoginStatus()
        object SP_SERVER_DOWN : LoginStatus()
        object VOID : LoginStatus()
        class UNEXPECTED_ERROR(val code: Int, val message: String) : LoginStatus()
    }

    /**
     *
     */
    inner class UserLoginTask internal constructor(private val adminNoStr: String, private val passwordStr: String) :
            AsyncTask<Void, Void, Pair<LoginStatus, Boolean>>() {

        override fun doInBackground(vararg params: Void) : Pair<LoginStatus, Boolean> {
            val status = FrontendInterface.performLogin(adminNoStr, passwordStr)
            if(status is LoginStatus.SUCCESS) {
                return Pair(status, FrontendInterface.isUserInitializedOnServer())
            }

            return Pair(status, false)
        }

        override fun onPostExecute(resp: Pair<LoginStatus, Boolean>) {
            mAuthTask = null

            // Ignore userInitialized if status isnt LoginStatus.SUCCESS
            val (status, userInitialized) = resp

            when(status) {
                is LoginStatus.SUCCESS -> {
                    Auth.login(status.response.token, adminNoStr, passwordStr)

                    if(!userInitialized)
                        this@LoginActivity.startActivity(Intent(this@LoginActivity, InitialLogin::class.java))
                    else
                        this@LoginActivity.startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                }

                LoginStatus.INVALID_CREDENTIALS -> {
                    password.error = getString(R.string.error_incorrect_password)
                    password.requestFocus()
                }

                LoginStatus.SP_SERVER_DOWN ->
                    Toast.makeText(this@LoginActivity.applicationContext,
                            "SP's server is down, try again later",
                            Toast.LENGTH_SHORT)
                            .show()

                is LoginStatus.UNEXPECTED_ERROR -> {
                    Toast.makeText(this@LoginActivity.applicationContext,
                            "Unexpected Error [${status.code}]: ${status.message}",
                            Toast.LENGTH_SHORT)
                            .show()
                }

                LoginStatus.VOID -> {
                    Toast.makeText(this@LoginActivity.applicationContext,
                            "Unknown error",
                            Toast.LENGTH_SHORT)
                            .show()
                }
            }

            showProgress(false)
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }
}

