package com.smartpods.android.pulseecho.Fragments.Home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.smartpods.android.pulseecho.Adapters.SurveyQuestionsListAdapter
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.CustomUI.SnapOnScrollListener
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.ViewModel.SurveyViewModel
import info.androidhive.fontawesome.FontDrawable
import kotlinx.android.synthetic.main.survey_fragment.*
import java.io.IOException
import android.view.Gravity
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.OtherLibs.EmptyState.EmptyState
import com.smartpods.android.pulseecho.Utilities.*
import kotlinx.android.synthetic.main.user_desk_statistics_fragment.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.support.v4.toast
import org.json.JSONObject


interface OnSnapPositionChangeListener {

    fun onSnapPositionChange(position: Int)
}

class Survey : BaseFragment() {

    companion object {
        fun newInstance() = Survey()
    }

    private lateinit var viewModel: SurveyViewModel
    private lateinit var surveyObj: UserSurvey
    var currentPage: Int = 0
    var questionCount: Int = 0
    var itemIdx: Int = 0

    private val emptyState: EmptyState by lazy {
        val emptyState = EmptyState()
        emptyState.imageRes = R.drawable.ic_search
        emptyState.title = "No data available."
        emptyState.message = "Sorry, no results found."
        emptyState.labelButton = "Try again?"
        emptyState.labelButtonColor = context?.getColor(R.color.smartpods_blue)
        emptyState.actionHandler = {
            refreshSurveyPage.isRefreshing = true
            getUserSurvey()
        }

        emptyState
    }

    var isLoading: Boolean = true
        set(value) {
            field = value
            if (value) {
                progressBar?.visibility = View.VISIBLE
                emptySurveyView?.visibility = View.GONE
                surveyQuestions.hide()
            } else {
                progressBar?.visibility = View.GONE
                emptySurveyView?.visibility = View.VISIBLE
                surveyQuestions.show()
            }
        }



    private val surveyQuestionItemAdapter: SurveyQuestionsListAdapter by lazy {
        SurveyQuestionsListAdapter(viewModel.surveyQuestions) { result, idx, answer ->
            //println("question: $result | position: $idx")
            println("answer: $answer")
            itemIdx = idx
            var mQuestion = result
            mQuestion.SelectedAnswer = answer
            viewModel.updateSurveyQuestion(mQuestion, idx)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.survey_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(SurveyViewModel::class.java)

        initializeUI()
    }

    override fun onResume() {
        super.onResume()
        fragmentNavTitle("Survey", true, false)
        getUserSurvey()
    }

    fun initializeUI() {

        emptySurveyView?.emptyState = emptyState
        isLoading = false
        refreshSurveyPage.setOnRefreshListener {
            getUserSurvey()
        }

        nextButtonConfiguration(true)
        val previousIcon = FontDrawable(appContext, R.string.fa_chevron_left_solid, true, false)
        previousIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_blue))
        surveyQuestionPrevious.setImageDrawable(previousIcon)

        surveyQuestionPrevious.setOnClickListener{
            //println("surveyQuestionPrevious : $currentPage")
            println("previous question current page : ${currentPage}")

            if (currentPage == 0)
                currentPage = 0
            else
                currentPage -= 1

            surveyQuestions.scrollToPosition(currentPage)
            nextButtonConfiguration(true)
            updateCountPage()
        }

        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { spEventHandler ->
            pulseMainActivity.showActivityLoader(false)
            spEventHandler.getContentIfNotHandled()?.let {
                toast(it)
            }
        })

    }

    fun getUserSurvey() {
        emptySurveyView?.emptyState = emptyState
        isLoading = false

        if (view != null) else return

        val email = Utilities.getLoggedEmail()

        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
                this.isLoading = true
                val hasNetwork = PulseApp.appContext.let { this.pulseMainActivity.isNetworkAvailable(it) }
                println("hasNetwork : $hasNetwork")

                if (hasNetwork) {
                    //request user survey from cloud
                    viewModel.requestUserSurvey(email).observe(viewLifecycleOwner, Observer {
                        pulseMainActivity.showActivityLoader(false)
                        this.isLoading = false
                        if (it.GenericResponse.Success) {
                            if (it.response.has("Survey") && !it.response.isNull("Survey")) {
                                updateUI(it)
                            } else {
                                refreshSurveyPage.isRefreshing = false
                                this.emptySurveyView.show()
                                this.surveyQuestions.hide()
                                this.surveyNavBottom.hide()
                            }
                        } else {
                            this.isLoading = false
                            refreshSurveyPage.isRefreshing = false
                            errorResponse(it.GenericResponse, email)
                        }
                    })

                } else {
                    this.isLoading = false
                    pulseMainActivity.showToastView(getString(R.string.no_survey_available))
                }
            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }
    }

    fun submitAnswers() {
        val email = Utilities.getLoggedEmail()

        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
                val hasNetwork = PulseApp.appContext.let { this.pulseMainActivity.isNetworkAvailable(it) }

                if (hasNetwork) {
                    viewModel.requestSubmitSurveyAnswers(email)

                } else {
                    pulseMainActivity.showToastView(getString(R.string.no_survey_available))
                }
            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }
    }

    fun RecyclerView.attachSnapHelperWithListener(
        snapHelper: SnapHelper,
        behavior: SnapOnScrollListener.Behavior = SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL,
        onSnapPositionChangeListener: OnSnapPositionChangeListener
    ) {
        snapHelper.attachToRecyclerView(this)
        val snapOnScrollListener = SnapOnScrollListener(snapHelper, behavior)
        addOnScrollListener(snapOnScrollListener)
    }

    fun updateUI(survey: UserSurvey) {
        surveyObj = survey
        questionCount = survey.QuestionCount
        currentPage = 0
        updateCountPage()
        this.emptySurveyView.hide()
        this.surveyQuestions.show()
        this.surveyNavBottom.show()
        refreshSurveyPage.isRefreshing = false
        surveyQuestions.apply {
            adapter = surveyQuestionItemAdapter
            layoutManager = LinearLayoutManager(
                appContext,
                RecyclerView.HORIZONTAL,
                false
            )

            this.isNestedScrollingEnabled = false
        }

        val snapHelper: SnapHelper = GravitySnapHelper(Gravity.START)
        snapHelper.attachToRecyclerView(surveyQuestions)

        val myLinearLayoutManager = object : LinearLayoutManager(appContext) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        surveyQuestions.layoutManager = myLinearLayoutManager
    }

    fun nextButtonConfiguration(isNavButton: Boolean) {

        if (isNavButton) {
            val nextIcon = FontDrawable(appContext, R.string.fa_chevron_right_solid, true, false)
            nextIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_blue))
            surveyQuestionNext.setImageDrawable(nextIcon)
            surveyQuestionNext.setBackgroundResource(0)
            surveySubmitTextView.hide()

            surveyQuestionNext.setOnClickListener {
                if (currentPage == questionCount)
                    currentPage = questionCount
                else
                    currentPage += 1
                //currentPage = surveyQuestions.getCurrentPosition()
                println("next question current page : ${currentPage}")
                surveyQuestions.scrollToPosition(currentPage)
                updateCountPage()

            }
        } else {
            surveySubmitTextView.show()
            surveyQuestionNext.setBackgroundResource(R.drawable.sp_rounded_button)
            surveyQuestionNext.setOnClickListener{
                submitAnswers()
            }
        }
    }

    fun updateCountPage() {

        if (viewModel.surveyQuestions.count() > 0) {
            println("updateCountPage_SelectedScaleOptionID: ${viewModel.surveyQuestions[itemIdx].SelectedScaleOptionID}")
            val mQuestion = viewModel.surveyQuestions[currentPage]
            lblSurveyPage.text = "${mQuestion.Rank} / $questionCount"
            if (mQuestion.Rank == 1) {
                surveyQuestionPrevious.hide()
            } else {
                surveyQuestionPrevious.show()
            }

            if (mQuestion.Rank == questionCount) {
                nextButtonConfiguration(false)
            } else {
                nextButtonConfiguration(true)
            }
        }


    }
}