package com.smartpods.android.pulseecho.Activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smartpods.android.pulseecho.BaseActivity
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.isValidEmail
import com.smartpods.android.pulseecho.ViewModel.AuthenticateViewModel
import com.smartpods.android.pulseecho.databinding.ActivityPulseResetPasswordBinding
import kotlinx.android.synthetic.main.activity_pulse_reset_password.*

class PulseResetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityPulseResetPasswordBinding
    private lateinit var viewModel: AuthenticateViewModel
    private  lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPulseResetPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(AuthenticateViewModel::class.java)
        email = intent.getStringExtra("email")

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
        //Submit reset password
        btnResetSave.setOnClickListener{

            val resetCode =  txtResetPasswordCode.text.toString()
            val userPass =  txtResetPass.text.toString()
            val verifyPass = txtResetVerifyPass.toString()

            if (resetCode != "" && userPass != "" && verifyPass != "") {
                this.showStatusLoader(this, true)
                viewModel.requestResetPassword(email, userPass,resetCode).observe(this, Observer {
                    this.showStatusLoader(this, false)
                    if (it != null) {
                        if (it.ResultCode == 0 && it.Success) {
                            this.toast(this, getString(R.string.forgot_success_reset_password))
                            finish()
                        } else {
                            this.toast(this, it.Message)
                        }
                    } else {
                        this.toast(this, getString(R.string.unable_to_register))
                    }
                })

            } else {
                this.showStatusLoader(this, false)
                if (resetCode === "") {
                    this.toast(this, getString(R.string.login_empty_code))
                }

                if (userPass === "") {
                    this.toast(this, getString(R.string.login_password_required))
                }

                if (verifyPass === "") {
                    this.toast(this, getString(R.string.login_verify_password_required))
                }

                if (verifyPass !== userPass) {
                    this.toast(this, getString(R.string.verify_password_password_not_equal))
                }
            }
        }

        //Back to previous screen
        btnResetCancel.setOnClickListener{
            finish()
        }

    }
}