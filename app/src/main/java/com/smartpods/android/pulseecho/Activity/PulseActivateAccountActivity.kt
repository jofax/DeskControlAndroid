package com.smartpods.android.pulseecho.Activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smartpods.android.pulseecho.BaseActivity
import com.smartpods.android.pulseecho.MainActivity
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.ViewModel.AuthenticateViewModel
import com.smartpods.android.pulseecho.databinding.ActivityPulseActivateAccountBinding
import com.smartpods.android.pulseecho.databinding.ActivityPulseRegistrationActivityBinding
import kotlinx.android.synthetic.main.activity_pulse_activate_account.*

class PulseActivateAccountActivity : BaseActivity() {

    private lateinit var binding: ActivityPulseActivateAccountBinding
    private lateinit var viewModel: AuthenticateViewModel
    private  lateinit var email: String
    private lateinit var userpass: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPulseActivateAccountBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(AuthenticateViewModel::class.java)
        email = intent.getStringExtra("email").toString()
        userpass = intent.getStringExtra("userPass").toString()


        this.setCustomActionBar(
            title = getString(R.string.activate_title),
            user = "",
            cloud = false,
            back = false,
            ble = false
        )

        customizeUI()
    }

    private fun customizeUI() {
        //Activate account
        btnActivateAccount.setOnClickListener{

            val activateCode = txtActivateCode.text.toString()

            if (email != "" && activateCode != "") {
                this.showStatusLoader(this, true)
                viewModel.requestActivateAccount(email, userpass, activateCode).observe(this, Observer {
                    this.showStatusLoader(this, false)
                    if (it != null) {
                        if (it.ResultCode == 0 && it.Success) {
                            val intent = Intent().apply {
                                putExtra("status", "true")
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        } else {
                            this.toast(this, it.Message)
                        }
                    } else {
                        this.toast(this, getString(R.string.unable_to_login))
                    }
                })
            } else {
                this.showStatusLoader(this, false)
                this.toast(this, getString(R.string.login_empty_code))
            }
        }

        //Back to login
        btnActivateBack.setOnClickListener {
            finish()
        }

        //Resend activation code
        btnResendActivation.setOnClickListener{
            this.showStatusLoader(this, true)
            viewModel.requestResendActivationCode(email).observe(this, Observer {
                this.showStatusLoader(this, false)
                if (it != null) {
                    if (it.Success) {
                        this.toast(this, getString(R.string.login_activate_resend_code))
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