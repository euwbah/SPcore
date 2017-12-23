package com.spcore.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import com.spcore.R
import com.spcore.helpers.dpToPx
import com.spcore.helpers.pxToDp
import com.spcore.helpers.resizeImage
import com.spcore.models.Lesson

import kotlinx.android.synthetic.main.activity_lesson_details.*
import kotlinx.android.synthetic.main.content_lesson_details.*

class LessonDetailsActivity : AppCompatActivity() {

    private lateinit var mEvent : Lesson

    override fun onCreate(savedInstanceState: Bundle?) {

        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson_details)

        setSupportActionBar(lesson_details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        mEvent = intent.extras.getParcelable("event")

        lesson_details_toolbar_title.text = "[${mEvent.moduleCode}] ${mEvent.name}"

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val _24dp = 24.dpToPx()
        val scaledAtsIcon = getDrawable(R.drawable.ats).resizeImage(_24dp, _24dp, resources)

        scaledAtsIcon.setColorFilter(0xcc333333.toInt(), PorterDuff.Mode.MULTIPLY)

        lesson_details_ats_status.setCompoundDrawablesWithIntrinsicBounds(scaledAtsIcon, null, null, null)

    }

}
