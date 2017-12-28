package com.spcore.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.spcore.R
import kotlinx.android.synthetic.main.activity_initial_login.*

class InitialLogin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_login)

        username_input.setOnFocusChangeListener { view, isFocused ->
            username_description.visibility = if(isFocused)
                View.VISIBLE
            else
                View.GONE
        }

        displayed_name_input.setOnFocusChangeListener { view, isFocused ->
            displayed_name_description.visibility = if(isFocused)
                View.VISIBLE
            else
                View.GONE
        }
    }
}
