package com.smartpods.android.pulseecho.Fragments.Statistics

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartpods.android.pulseecho.Adapters.ModePercenstageAdapter
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.Model.UserReport
import com.smartpods.android.pulseecho.Model.UserStatisticItem
import com.smartpods.android.pulseecho.OtherLibs.EmptyState.EmptyState
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.hide
import com.smartpods.android.pulseecho.Utilities.show
import com.smartpods.android.pulseecho.ViewModel.UserDeskStatisticsViewModel
import kotlinx.android.synthetic.main.user_desk_statistics_fragment.*
import java.io.IOException

class UserDeskStatisticsFragment : BaseFragment() {

    companion object {
        fun newInstance() = UserDeskStatisticsFragment()
    }

    private lateinit var viewModel: UserDeskStatisticsViewModel
    private var modePercentageList = mutableListOf<UserStatisticItem>()
    private var activityByDeskList = mutableListOf<UserStatisticItem>()

    private val emptyState: EmptyState by lazy {
        val emptyState = EmptyState()
        emptyState.imageRes = R.drawable.ic_search
        emptyState.title = "No data available."
        emptyState.message = "Sorry, no results found."
        emptyState.labelButton = "Try again?"
        emptyState.labelButtonColor = context?.getColor(R.color.smartpods_blue)
        emptyState.actionHandler = {
                fetchData()
        }

        emptyState
    }

    var isLoading: Boolean = true
        set(value) {
            field = value
            if (value) {
                progressBar?.visibility = View.VISIBLE
                emptyView?.visibility = View.GONE
                userDeskStatisticLayout.hide()
            } else {
                progressBar?.visibility = View.GONE
                emptyView?.visibility = View.VISIBLE
                userDeskStatisticLayout.show()
            }
        }

    private val modePercentageItemAdapter: ModePercenstageAdapter by lazy {
        ModePercenstageAdapter(modePercentageList) { result, idx ->

        }
    }

    private val activityByDeskItemAdapter: ModePercenstageAdapter by lazy {
        ModePercenstageAdapter(activityByDeskList) { result, idx ->

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_desk_statistics_fragment, container, false)
    }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            viewModel = ViewModelProvider(this).get(UserDeskStatisticsViewModel::class.java)
            // TODO: Use the ViewModel
            initializeUI()
        }

    override fun onResume() {
        println("UserDeskStatisticsFragment onResume")
        super.onResume()

        requestStatisticData()
    }

    private fun initializeUI() {

        emptyView?.emptyState = emptyState
        isLoading = false
        refreshStatisticsPage.setOnRefreshListener {
            requestStatisticData()
        }

        initializeRecycler()
    }

    private fun initializeRecycler() {
        percentagRecycler.apply {
            adapter = modePercentageItemAdapter
            layoutManager = LinearLayoutManager(
                PulseApp.appContext,
                RecyclerView.HORIZONTAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        deskActivityRecycler.apply {
            adapter = activityByDeskItemAdapter
            layoutManager = LinearLayoutManager(
                PulseApp.appContext,
                RecyclerView.HORIZONTAL,
                false
            )
            isNestedScrollingEnabled = false
        }
    }

    override fun fetchData() {
        emptyView?.emptyState = emptyState
        isLoading = false
        requestStatisticData()
    }

    private fun requestStatisticData() {
        val email = Utilities.getLoggedEmail()
        emptyView?.emptyState = emptyState

        if (view != null) else return

        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
                this.isLoading = true
                viewModel.requestUserStatisticSummary(email).observe(viewLifecycleOwner, Observer {
                    pulseMainActivity.showActivityLoader(false)
                    if (it.GenericResponse.Success) {
                        println("REPORT RESPONSE: $it")
                        if (it.report != null) {
                            modePercentageList.clear()
                            activityByDeskList.clear()
                            this.isLoading = false
                            this.emptyView.hide()
                            this.userDeskStatisticLayout.show()
                            println("REPORT DATA : ${it.report}")
                            updateUI(it)
                        }
                    } else {
                        refreshStatisticsPage.isRefreshing = false
                        this.isLoading = false
                        this.emptyView.show()
                        this.userDeskStatisticLayout.hide()
//                        if (it.GenericResponse.Message == "Unauthorized") {
//                            println("redirect to login")
//                            pulseMainActivity.redirectToLoginPage(email)
//                        }
                        errorResponse(it.GenericResponse, email)
                    }
                })
            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }
    }

    private fun updateUI(report: UserReport) {
        upDownPerHourValue.text = report.UpDownPerHour.toString()
        totalActivityValue.text = Utilities.doubleToPercentage(report.TotalActivity)
        modePercentageList.addAll(report.ModePercentageList)
        activityByDeskList.addAll(report.ActivityByDesk)

        percentagRecycler.adapter?.notifyDataSetChanged()
        deskActivityRecycler.adapter?.notifyDataSetChanged()

        refreshStatisticsPage.isRefreshing = false
    }

}

