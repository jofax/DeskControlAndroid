package com.smartpods.android.pulseecho.CustomUI

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.smartpods.android.pulseecho.BLEScanner.DeviceListActivity
import com.smartpods.android.pulseecho.Constants.POP_UP_VIEW_TYPE
import com.smartpods.android.pulseecho.R
import kotlinx.android.synthetic.main.activity_custom_pop_up.*
import info.androidhive.fontawesome.FontDrawable

class   CustomPopUpActivity : AppCompatActivity() {

    var popUpType = POP_UP_VIEW_TYPE.UNKNOWN
    private lateinit var btnClosePopUp: ImageButton
    private lateinit var mLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_custom_pop_up)

        val popUpCloseIcon = FontDrawable(this, R.string.fa_times_solid, true, false)

        btnClosePopUp =  findViewById(R.id.btnPopUpClose)
        btnClosePopUp.setImageDrawable(popUpCloseIcon)

        mLinearLayout = findViewById(R.id.popup_window_background_container)

        fadeAnimation()

        var bundle = intent.extras
        if (bundle != null) {
            popUpType = bundle.get("popUpType") as POP_UP_VIEW_TYPE

//            if (popUpType == POP_UP_VIEW_TYPE.DEVICE_LIST) {
//                val fm = supportFragmentManager
//                var fragment = fm.findFragmentByTag("deviceListFragment")
//                if (fragment == null) {
//                    val ft = fm.beginTransaction()
//                    fragment = CustomFragmentContainer()
//                    ft.add(android.R.id.content, fragment, "deviceListFragment")
//                    val intent = Intent(this, DeviceListActivity::class.java)
//                    startActivity(intent)
//
//                    ft.commit()
//                }
//            }
        }


        // Close the Popup Window when you press the button
        btnClosePopUp.setOnClickListener {
            onBackPressed()
        }
    }

    private fun fadeAnimation() {
        // Fade animation for the background of Popup Window
        val alpha = 100 //between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, alphaColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            custom_popup_window.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()


        // Fade animation for the Popup Window
        popup_window_view_with_border.alpha = 0f
        popup_window_view_with_border.animate().alpha(1f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()
    }

    private fun setWindowFlag(activity: Activity, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        } else {
            winParams.flags = winParams.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        }
        win.attributes = winParams
    }


    override fun onBackPressed() {
        // Fade animation for the background of Popup Window when you press the back button
        val alpha = 100 // between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
        colorAnimation.duration = 100 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            custom_popup_window.setBackgroundColor(
                animator.animatedValue as Int
            )
        }

        // Fade animation for the Popup Window when you press the back button
        popup_window_view_with_border.animate().alpha(0f).setDuration(100).setInterpolator(
            DecelerateInterpolator()
        ).start()

        // After animation finish, close the Activity
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finish()
                overridePendingTransition(0, 0)
            }
        })
        colorAnimation.start()
    }
}