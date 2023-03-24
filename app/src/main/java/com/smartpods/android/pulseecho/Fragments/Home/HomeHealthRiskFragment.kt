package com.smartpods.android.pulseecho.Fragments.Home

import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartpods.android.pulseecho.Adapters.RiskAssessmentListAdapter
import com.smartpods.android.pulseecho.BLEScanner.ScanResultAdapter
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.Model.RiskRecommendations
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.ViewModel.HomeHealthRiskViewModel
import kotlinx.android.synthetic.main.activity_device_list.*
import kotlinx.android.synthetic.main.desk_mode_fragment.*
import kotlinx.android.synthetic.main.home_health_risk_fragment.*
import kotlinx.android.synthetic.main.sp_with_menu_action_bar.*
import org.jetbrains.anko.backgroundDrawable
import timber.log.Timber

class HomeHealthRiskFragment : BaseFragment() {

    companion object {
        fun newInstance() = HomeHealthRiskFragment()
    }

    private lateinit var viewModel: HomeHealthRiskViewModel
    private var riskList = listOf<RiskRecommendations>()
    private val riskRecommendationAdapter: RiskAssessmentListAdapter by lazy {
        RiskAssessmentListAdapter(riskList) { result, idx ->

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_health_risk_fragment, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeHealthRiskViewModel::class.java)
        riskList = viewModel.getRiskRecommendationList(appContext.resources, 0)
        initilizeUI()
    }

    fun initilizeUI() {
        risk_assessment_recycler_view.apply {
            adapter = riskRecommendationAdapter
            layoutManager = LinearLayoutManager(
                appContext,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        currentRiskAssessment(10)

        btnLowRisk.setOnClickListener{
            riskAssessmentUpdate(0)
        }

        btnMediumRisk.setOnClickListener{
            riskAssessmentUpdate(1)
        }

        btnHighRisk.setOnClickListener{
            riskAssessmentUpdate(2)
        }
    }

    override fun onResume() {
        super.onResume()
        println("HeartAccumulationFragment onResume")
        riskNavigationControl()

    }

    override fun onStart() {
        super.onStart()
        println("HomeHealthRiskFragment onStart")
    }

    override fun onStop() {
        super.onStop()
        println("HomeHealthRiskFragment onStop")

    }

    fun riskAssessmentUpdate(tag: Int) {
        riskList = viewModel.getRiskRecommendationList(appContext.resources, tag)
        currentRiskAssessment(tag)
    }

    fun currentRiskAssessment(tag: Int) {
        when(tag) {
            0 -> {
                highRiskContentLabelTitle.text = getString(R.string.low_risk_description)
                riskContentLayout.setBackgroundResource(R.drawable.low_risk_layout_border)
                btnLowRisk.setBackgroundResource(R.drawable.sp_low_risk_assessment_btn)
                btnMediumRisk.setBackgroundResource(R.drawable.sp_rounded_button)
                btnHighRisk.setBackgroundResource(R.drawable.sp_rounded_button)
            }

            1-> {
                highRiskContentLabelTitle.text = getString(R.string.medium_risk_description)
                riskContentLayout.setBackgroundResource(R.drawable.medium_risk_layout_border)
                btnLowRisk.setBackgroundResource(R.drawable.sp_rounded_button)
                btnMediumRisk.setBackgroundResource(R.drawable.sp_medium_risk_assessment_btn)
                btnHighRisk.setBackgroundResource(R.drawable.sp_rounded_button)

            }

            2 -> {
                highRiskContentLabelTitle.text = getString(R.string.high_risk_description)
                riskContentLayout.setBackgroundResource(R.drawable.high_risk_layout_border)
                btnLowRisk.setBackgroundResource(R.drawable.sp_rounded_button)
                btnMediumRisk.setBackgroundResource(R.drawable.sp_rounded_button)
                btnHighRisk.setBackgroundResource(R.drawable.sp_high_risk_assessment_btn)
            }

            else -> {
                highRiskContentLabelTitle.text = getString(R.string.low_risk_description)
                riskContentLayout.setBackgroundResource(R.drawable.low_risk_layout_border)
                btnLowRisk.setBackgroundResource(R.drawable.sp_rounded_button)
                btnMediumRisk.setBackgroundResource(R.drawable.sp_rounded_button)
                btnHighRisk.setBackgroundResource(R.drawable.sp_rounded_button)
            }
        }

    }

    fun riskNavigationControl() {
        arguments?.get("risk_level")?.let {
            val mLevel = it as String
            currentRiskAssessment(mLevel.toInt())
        }

        if (pulseMainActivity != null) {
            fragmentNavTitle("Health Risk", true, true)
            this.pulseMainActivity.btnBluetooth.setOnClickListener{
                val action = HomeHealthRiskFragmentDirections.actionHomeHealthRiskFragmentToHeartAccumulationFragment()
                findNavController().navigate(action)
            }
        }
    }

}