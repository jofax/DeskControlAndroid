package com.smartpods.android.pulseecho.Adapters

import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.smartpods.android.pulseecho.Model.UserSurveyQuestions
import com.smartpods.android.pulseecho.Model.UserSurveyScaleOptions
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.addLayoutMargins
import com.smartpods.android.pulseecho.Utilities.setMargin
import com.smartpods.android.pulseecho.Utilities.setTopMargin
import kotlinx.android.synthetic.main.survey_question.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.topPadding
import android.widget.EditText
import androidx.core.view.forEachIndexed


class SurveyQuestionsListAdapter(private var items: List<UserSurveyQuestions>,
                                 private var onClickListener: ((question: UserSurveyQuestions, idx: Int, answer: Int) -> Unit)
) : RecyclerView.Adapter<SurveyQuestionsListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.context.layoutInflater.inflate(
            R.layout.survey_question,
            parent,
            false
        )
        return ViewHolder(view, onClickListener)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
    }

    class ViewHolder(
        private var view: View,
        private var onClickListener: (question: UserSurveyQuestions, idx: Int, answer: Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        fun bind(result: UserSurveyQuestions, position: Int) {
            view.lblSurveyQuestions.text = result.Text
            view.surveyAnswerButtonsLayout.removeAllViews()

            (0 until result.ScaleOptions.count()).forEach{
                val item = result.ScaleOptions[it]
                val themeWrapper = ContextThemeWrapper(
                    appContext,
                    R.style.PulseRoundedButton
                )
                val btnAnswer = Button(themeWrapper)
                btnAnswer.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                btnAnswer.addLayoutMargins(top = 20)

                if (result.SelectedAnswer == item.ID) {
                    btnAnswer.setBackgroundResource(R.drawable.sp_rounded_button_selected)
                } else {
                    btnAnswer.setBackgroundResource(R.drawable.sp_rounded_button)
                }

                btnAnswer.tag = item.ID
                btnAnswer.text = item.Text
                btnAnswer.setOnClickListener{ btn ->
                    resetButtons(view.surveyAnswerButtonsLayout)
                    onClickListener.invoke(result, position, item.ID)
                    btn.setBackgroundResource(R.drawable.sp_rounded_button_selected)
                }
                view.surveyAnswerButtonsLayout.addView(btnAnswer)
            }
        }

        fun resetButtons(container: ViewGroup) {
            container.forEachIndexed { idx,view ->
                if (view is Button) {
                    view.setBackgroundResource(R.drawable.sp_rounded_button)
                }
            }
        }
    }
}