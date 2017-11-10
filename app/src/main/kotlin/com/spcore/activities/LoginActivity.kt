package com.spcore.activities

import android.annotation.TargetApi
import android.content.Context
import android.support.v7.app.AppCompatActivity

import android.os.AsyncTask

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast


import com.spcore.R
import com.spcore.backend.Backend
import com.spcore.backend.LoginResponse
import com.spcore.helpers.Auth.retrieveJWTTokenSP
import com.spcore.helpers.backendErrorAdapter
import com.spcore.helpers.onAnimationEnd
import com.spcore.spmobileapi.SPMobileAPI
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
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
            x: String -> x.matches(Regex("p?\\d{7}"))
        }

        if (!TextUtils.isEmpty(passwordStr)) {
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
            mAuthTask = UserLoginTask(adminNoStr, passwordStr)
            mAuthTask?.execute()
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        login_form_scroll.visibility = if (show) View.GONE else View.VISIBLE
        login_form_scroll.animate()
                .setDuration(shortAnimTime.toLong())
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(
                    onAnimationEnd {
                        login_form_scroll.visibility = if (show) View.GONE else View.VISIBLE
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
            AsyncTask<Void, Void, LoginStatus>() {

        override fun doInBackground(vararg params: Void): LoginStatus {
            // TODO: attempt authentication against a network service.

            val resp = Backend.performLogin(adminNoStr, passwordStr).execute()

            if(!resp.isSuccessful) {
                resp.errorBody()?.string()?.let {
                    val err = backendErrorAdapter.fromJson(it)

                    err?.code?.toInt()?.let {
                        return when(it) {
                            2 -> LoginStatus.INVALID_CREDENTIALS
                            3 -> LoginStatus.SP_SERVER_DOWN
                            else -> LoginStatus.UNEXPECTED_ERROR(it, err.message)
                        }
                    }
                }

                return LoginStatus.VOID
            }

            resp.body()?.let {
                return LoginStatus.SUCCESS(it)
            }

            return LoginStatus.VOID
        }

        override fun onPostExecute(status: LoginStatus) {
            mAuthTask = null
            showProgress(false)
            when(status) {
                is LoginStatus.SUCCESS -> {
                    this@LoginActivity.retrieveJWTTokenSP()
                            .edit()
                            .putString("token", status.response.token)
                            .apply()
                }

                is LoginStatus.INVALID_CREDENTIALS -> {
                    password.error = getString(R.string.error_incorrect_password)
                    password.requestFocus()
                }

                is LoginStatus.SP_SERVER_DOWN ->
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

                is LoginStatus.VOID -> {
                    Toast.makeText(this@LoginActivity.applicationContext,
                            "Unknown error",
                            Toast.LENGTH_SHORT)
                            .show()
                }
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }
}

