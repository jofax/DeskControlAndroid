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
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.MainActivity
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.guard
import com.smartpods.android.pulseecho.Utilities.isValidEmail
import com.smartpods.android.pulseecho.Utilities.setOnClickListenerThrottled
import com.smartpods.android.pulseecho.ViewModel.AuthenticateViewModel
import com.smartpods.android.pulseecho.databinding.ActivityPulseLoginBinding
import kotlinx.android.synthetic.main.activity_pulse_login.*


class PulseLoginActivity : BaseActivity(), PulseRegistrationHandler {

    private lateinit var binding: ActivityPulseLoginBinding
    private lateinit var txt_email: TextInputEditText
    private lateinit var txt_password: TextInputEditText
    private lateinit var btn_login: Button
    private lateinit var btn_register: Button
    private lateinit var btn_guest: Button
    private lateinit var btn_forgot: Button

    private lateinit var viewModel: AuthenticateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPulseLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(AuthenticateViewModel::class.java)

        txt_email = findViewById(R.id.txtUserEmail)
        txt_password = findViewById(R.id.txtUserPass)
        btn_login = findViewById(R.id.btnLogin)
        btn_register = findViewById(R.id.btnRegister)
        btn_guest = findViewById(R.id.btnGuest)
        btn_forgot = findViewById(R.id.btnForgot)

        this.setCustomActionBar(
                title = getString(R.string.welcome_string),
                user = "",
                cloud = false,
                back = false,
                ble = false
        )

        customizeUI()
    }

    private fun customizeUI() {
        //Login

        this.loginButtonListener(true)

        //Register
        btn_register.setOnClickListener {
            this.goToActivityWithResult(this, Constants.registrationCode, HashMap<String, Any>(), PulseRegistrationActivity::class.java)
        }

        //Login as Guest
        btn_guest.setOnClickListener{
            viewModel.guestLogin()
            this.goToActivity( this, HashMap<String, Any>(), MainActivity::class.java)
        }

        //Forgot password
        btnForgot.setOnClickListener{
            this.goToActivityWithResult(this, Constants.forgotPassword, HashMap<String, Any>(), PulseForgotPasswordActivity::class.java)
        }

    }

    override fun showActivateScreen() {
       println("should show the activation screen from registration call")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.registrationCode) {
            if (resultCode == Activity.RESULT_OK) {
                println("registration data: $data")
                if (data !== null) {
                    val username = data.getStringExtra("username")
                    val userpass = data.getStringExtra("userpass")
                    println("registration : $username | $userpass")
                    this.goToActivityWithResult(this,
                        Constants.activateCode, hashMapOf(
                        "email" to username,
                        "userPass" to userpass
                    ), PulseActivateAccountActivity::class.java)
                }
            }
        } else if (requestCode == Constants.activateCode) {
            if (resultCode == Activity.RESULT_OK) {
                println("activation data: $data")
                if (data !== null) {
                    val status = data.getStringExtra("status")
                    if (status == "true") {
                        this.goToActivity( this, HashMap<String, Any>(), MainActivity::class.java)
                    }
                }
            }

        } else if (requestCode == Constants.forgotPassword) {
            if (resultCode == Activity.RESULT_OK) {
                println("forgotPassword data: $data")
                if (data !== null) {
                    val username = data.getStringExtra("username")
                    this.goToActivityWithResult(
                        this,
                        Constants.resetPasswordCode, hashMapOf(
                            "email" to username
                        ), PulseResetPasswordActivity::class.java
                    )
                }
            }
        } else if (requestCode == Constants.resetPasswordCode) {
            if (resultCode == Activity.RESULT_OK) {
                println("resetPasswordCode data: $data")
                if (data !== null) {

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun requestLogin() {
        loginButtonListener(false)
        Log.i("email", txt_email.text.toString())
        Log.i("password", txt_password.text.toString())

        val username =  txt_email.text.toString()
        val userPass =  txt_password.text.toString()

        if (username.isValidEmail()) else {
            loginButtonListener(true)
            this.showStatusLoader(this, false)
            this.toast(this, getString(R.string.login_invalid_email))
            return
        }

        if (username != "" && userPass != "") {
            this.showStatusLoader(this, true)
            viewModel.requestLogin(username,userPass).observe(this, Observer {
                this.showStatusLoader(this, false)
                loginButtonListener(true)
                if (it != null) {
                    if (it.ResultCode == 0 && it.Success) {
                        this.goToActivity( this, HashMap<String, Any>(), MainActivity::class.java)
                    } else  if (it.ResultCode == 1 || it.ResultCode == 8) {

                        this.goToActivityWithResult(
                            this,
                            Constants.activateCode, hashMapOf(
                                "email" to username,
                                "userPass" to userPass
                            ), PulseActivateAccountActivity::class.java
                        )
                    } else if (it.ResultCode == 2) {
                        this.toast(this, getString(R.string.account_locked))
                    } else if (it.ResultCode == 3) {
                        this.toast(this, getString(R.string.account_verification_failed))
                    } else if (it.ResultCode == 4) {
                        this.toast(this, getString(R.string.invalid_org_or_desk))
                    } else {
                        this.toast(this, getString(R.string.unable_to_login))
                    }
                } else {
                    this.toast(this, getString(R.string.unable_to_login))
                }
            })
        } else {
            loginButtonListener(true)
            this.showStatusLoader(this, false)
            this.toast(this, getString(R.string.email_password_required))
        }
    }

    fun loginButtonListener(add: Boolean) {
        if (add) {
            btn_login.setOnClickListener {
                requestLogin()
            }
        } else {
            btn_login.setOnClickListener(null)
        }
    }
}