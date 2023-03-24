package com.smartpods.android.pulseecho.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smartpods.android.pulseecho.BaseActivity
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.isValidEmail
import com.smartpods.android.pulseecho.ViewModel.AuthenticateViewModel
import com.smartpods.android.pulseecho.databinding.ActivityPulseForgotPasswordBinding
import kotlinx.android.synthetic.main.activity_pulse_forgot_password.*


class PulseForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityPulseForgotPasswordBinding
    private lateinit var viewModel: AuthenticateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPulseForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(AuthenticateViewModel::class.java)

        this.setCustomActionBar(
            title = getString(R.string.forgot_title),
            user = "",
            cloud = false,
            back = false,
            ble = false
        )

        customizeUI()
    }

    private fun customizeUI() {
        //Forgot Password Back button
        btnForgotBack.setOnClickListener{
            finish()
        }

        //Forgot Password submit button
        btnForgotSubmit.setOnClickListener{
            this.showStatusLoader(this, true)
            val mForgotEmail = txtForgotEmail.text.toString()

            if (mForgotEmail.isValidEmail()) else {
                this.showStatusLoader(this, false)
                this.toast(this, getString(R.string.login_invalid_email))
                return@setOnClickListener
            }

            viewModel.requestForgotPasswordCode(mForgotEmail).observe(this, Observer {
                this.showStatusLoader(this, false)
                if (it != null) {
                    if (it.Success) {
                        val intent = Intent().apply {
                            putExtra("username", mForgotEmail)
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        this.toast(this, it.Message)
                    }
                } else {
                    this.toast(this, getString(R.string.other_error))
                }
            })

        }

    }
}