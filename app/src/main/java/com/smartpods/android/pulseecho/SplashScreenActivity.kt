package com.smartpods.android.pulseecho
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.smartpods.android.pulseecho.Activity.PulseLoginActivity
import com.smartpods.android.pulseecho.Utilities.Utilities
import android.view.WindowManager

class SplashScreenActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val backgroundImage:ImageView = findViewById(R.id.imgSplashIcon)
        val sideAnimation = AnimationUtils.loadAnimation(this, R.anim.side_slide)
        backgroundImage.startAnimation(sideAnimation)
        val email: String? = Utilities.getLoggedEmail()

        Handler(Looper.getMainLooper()).postDelayed({
            if (email == null || email.isEmpty()) {
                val intent = Intent(this, PulseLoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 4000) // delaying for 4 seconds...
    }
}
