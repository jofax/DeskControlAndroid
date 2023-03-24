package com.smartpods.android.pulseecho.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.smartpods.android.pulseecho.BaseActivity
import com.smartpods.android.pulseecho.MainActivity
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.isValidEmail
import com.smartpods.android.pulseecho.ViewModel.AuthenticateViewModel
import com.smartpods.android.pulseecho.databinding.ActivityPulseRegistrationActivityBinding

interface PulseRegistrationHandler {
    fun showActivateScreen()
}

class PulseRegistrationActivity : BaseActivity() {

    private lateinit var binding: ActivityPulseRegistrationActivityBinding
    private lateinit var txt_email: TextInputEditText
    private lateinit var txt_password: TextInputEditText
    private lateinit var txt_verify_password: TextInputEditText
    private lateinit var btn_register: Button
    private lateinit var btn_back: Button

    private lateinit var viewModel: AuthenticateViewModel
    private lateinit var registrationHander: PulseRegistrationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_pulse_registration_activity)

        binding = ActivityPulseRegistrationActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(AuthenticateViewModel::class.java)

        txt_email = findViewById(R.id.txtRegisterEmail)
        txt_password = findViewById(R.id.txtRegisterPass)
        txt_verify_password = findViewById(R.id.txtRegisterVerifyPass)
        btn_register = findViewById(R.id.btnRegister)
        btn_back = findViewById(R.id.btnRegisterBack)

        this.setCustomActionBar(
            title = getString(R.string.lbl_register),
            user = "",
            cloud = false,
            back = false,
            ble = false
        )

        customizeUI()
    }

    private fun customizeUI() {
        btn_register.setOnClickListener {
            Log.i("email", txt_email.text.toString())
            Log.i("password", txt_password.text.toString())
            Log.i("verify password", txt_verify_password.text.toString())

            val username =  txt_email.text.toString()
            val userPass =  txt_password.text.toString()
            val verifyPass = txt_verify_password.toString()

            if (username.isValidEmail()) else {
                this.showStatusLoader(this, false)
                this.toast(this, getString(R.string.login_invalid_email))
                return@setOnClickListener
            }

            if (username != "" && userPass != "" && verifyPass != "") {
                this.showStatusLoader(this, true)
                viewModel.requestRegister(username, userPass).observe(this, Observer {
                    this.showStatusLoader(this, false)
                    if (it != null) {
                        if (it.ResultCode == 0 && it.Success) {
                            val intent = Intent().apply {
                                putExtra("username", username)
                                putExtra("userpass", userPass)
                           }
                            setResult(Activity.RESULT_OK, intent)
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
                if (username === "") {
                    this.toast(this, getString(R.string.login_email_required))
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

        //Register
        btn_back.setOnClickListener {
            finish()
        }
    }
}