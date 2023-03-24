package com.smartpods.android.pulseecho.Fragments.Home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.CustomUI.CustomShapeProgressView
import com.smartpods.android.pulseecho.Model.UserModel
import com.smartpods.android.pulseecho.Model.UserObject
import com.smartpods.android.pulseecho.Model.UserRiskManagement
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.UserPreference
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.Utilities.format
import com.smartpods.android.pulseecho.Utilities.hide
import com.smartpods.android.pulseecho.ViewModel.HeartAccumulationViewModel
import com.smartpods.android.pulseecho.ViewModel.UserProfileViewModel
import kotlinx.android.synthetic.main.fragment_home_navigation.*
import kotlinx.android.synthetic.main.heart_accumulation_fragment.*
import kotlinx.android.synthetic.main.home_health_risk_fragment.*
import kotlinx.android.synthetic.main.home_heart_fragment.*
import kotlinx.android.synthetic.main.home_heart_fragment.heartAnimated
import kotlinx.android.synthetic.main.sp_with_menu_action_bar.*
import kotlinx.android.synthetic.main.user_desk_statistics_fragment.*
import org.jetbrains.anko.support.v4.act
import org.json.JSONObject
import java.io.IOException
import java.lang.Math.abs
import java.math.BigDecimal

class HeartAccumulationFragment : BaseFragment() {

    companion object {
        fun newInstance() = HeartAccumulationFragment()
    }

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var UserRisk: UserRiskManagement

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.heart_accumulation_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        // TODO: Use the ViewModel
        customizeUI()
    }

    fun customizeUI() {

        heartAccumulationLeft.setMax(100)
        heartAccumulationLeft.setShape(CustomShapeProgressView.Shape.HEART)
        heartAccumulationLeft.setFrontWaveColor(Utilities.getCustomRawColor(R.color.smartpods_bluish_white))
        heartAccumulationLeft.setBehindWaveColor(Utilities.getCustomRawColor(R.color.smartpods_white_text))
        heartAccumulationLeft.setBorderColor(Utilities.getCustomRawColor(R.color.smartpods_gray))
        heartAccumulationLeft.setTextColor(Utilities.getCustomRawColor(R.color.smartpods_blue))
        heartAccumulationLeft.setAnimationSpeed(100)
        heartAccumulationLeft.setWaveOffset(0)
        heartAccumulationLeft.setWaveStrong(20)
        heartAccumulationLeft.setHideText(false)
        heartAccumulationLeft.startAnimation()
        heartAccumulationLeft.mProgress = 0


        heartAccumulationRight.setMax(100)
        heartAccumulationRight.setShape(CustomShapeProgressView.Shape.HEART)
        heartAccumulationRight.setFrontWaveColor(Utilities.getCustomRawColor(R.color.smartpods_bluish_white))
        heartAccumulationRight.setBehindWaveColor(Utilities.getCustomRawColor(R.color.smartpods_white_text))
        heartAccumulationRight.setBorderColor(Utilities.getCustomRawColor(R.color.smartpods_gray))
        heartAccumulationRight.setTextColor(Utilities.getCustomRawColor(R.color.smartpods_blue))
        heartAccumulationRight.setAnimationSpeed(100)
        heartAccumulationRight.setWaveOffset(0)
        heartAccumulationRight.setWaveStrong(20)
        heartAccumulationRight.setHideText(false)
        heartAccumulationRight.startAnimation()
        heartAccumulationRight.mProgress = 0


        btnRiskAssessment.setOnClickListener{
            if (pulseMainActivity != null) {
                if (UserRisk.Level != -1) {
                    val action = HeartAccumulationFragmentDirections.actionHeartAccumulationFragmentToHomeHealthRiskFragment(UserRisk.Level.toString())
                        findNavController().navigate(action)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        println("HeartAccumulationFragment onResume")
        accumulationNavigationControl()
        requestUserInfo()
    }

    override fun onStart() {
        super.onStart()
        println("HeartAccumulationFragment onStart")

    }

    override fun onStop() {
        super.onStop()
        println("HeartAccumulationFragment onStop")

    }

    override fun onDestroyView() {
        super.onDestroyView()

        println("HeartAccumulationFragment onDestroyView")
    }

    fun requestUserInfo() {
        if (view != null) else return
        val email = Utilities.getLoggedEmail()

        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
                val hasNetwork = PulseApp.appContext.let { this.pulseMainActivity.isNetworkAvailable(it) }
                println("hasNetwork : $hasNetwork")

                if (hasNetwork) {
                    //request user information from cloud
                    viewModel.requestUserDetails(email).observe(viewLifecycleOwner, Observer {
                        pulseMainActivity.showActivityLoader(false)

                        if (it.GenericResponse.Success) {
                            if (it != null) {
                                updateUI(it)
                            }
                        } else {
                            errorResponse(it.GenericResponse, email)
                        }
                    })

                    viewModel.requestUserRiskAssement(email).observe(viewLifecycleOwner, {
                        if (it.GenericResponse.Success) {
                            UserRisk = it
                            updateUserRiskManagement(it)
                        } else {
                            UserRisk = UserRiskManagement(JSONObject())
                        }
                    })

                } else {
                    //fetch locally saved profile settings
                    val mUser = viewModel.getUserLocally(email)
                    val mUserObj = UserObject(Utilities.toJSONParameters(mUser))
                    updateUI(mUserObj)
                }
            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }
    }

    fun updateUI(user: UserObject) {
        activity?.runOnUiThread {
            heartAccumulationLeft.setTextLabel(user.Hearts.Today.format(0))
            heartAccumulationRight.setTextLabel(user.Hearts.Total.toString())
            val mAverageTime = user.Hearts.AvgHoursFillHeart

            val bigDecimal = BigDecimal(mAverageTime.toString())
            val hour: Int = bigDecimal.toInt()
            if (mAverageTime == 0.0) {
                heartAverageValueLabel.text = "$hour hour"
            } else {
                val mins = (bigDecimal.subtract(BigDecimal(hour)).toInt() * 10).toString()
                val mHourText = if (hour > 1) "hour" else "hours"
                heartAverageValueLabel.text = "$hour $mHourText and $mins mins"
            }

        }
    }

    fun updateUserRiskManagement(risk: UserRiskManagement) {
        activity?.runOnUiThread {
            val mProgress = kotlin.math.abs(risk.Progress).format(1)
            heartProgressValueLabel.text = "$mProgress %"

            when(risk.Level) {
                0 -> {
                    btnRiskAssessment.text = getString(R.string.risk_low)
                    btnRiskAssessment.setBackgroundResource(R.drawable.sp_low_risk_assessment_btn)
                }
                1 -> {
                    btnRiskAssessment.text = getString(R.string.risk_medium)
                    btnRiskAssessment.setBackgroundResource(R.drawable.sp_medium_risk_assessment_btn)
                }
                2 -> {
                    btnRiskAssessment.text = getString(R.string.risk_high)
                    btnRiskAssessment.setBackgroundResource(R.drawable.sp_high_risk_assessment_btn)
                }
                else ->  {
                    btnRiskAssessment.text = getString(R.string.text_empty)
                    btnRiskAssessment.setBackgroundResource(R.drawable.sp_gray_btn_bg)
                }
            }
        }
    }

    fun accumulationNavigationControl() {
        if (pulseMainActivity != null) {
            fragmentNavTitle("Heart Details", true, true)
            this.pulseMainActivity.btnBluetooth.setOnClickListener{
                val action = HeartAccumulationFragmentDirections.actionHeartAccumulationFragmentToHomeHeartFragment()
                findNavController().navigate(action)
            }
        }
    }

}