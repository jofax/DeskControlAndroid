package com.smartpods.android.pulseecho.Adapters

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.smartpods.android.pulseecho.Model.RiskRecommendations
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import kotlinx.android.synthetic.main.risk_assessment_list_row_item.view.*
import info.androidhive.fontawesome.FontDrawable
import org.jetbrains.anko.layoutInflater

class RiskAssessmentListAdapter(
    private val items: List<RiskRecommendations>,
    private val onClickListener: ((riskRecommend: RiskRecommendations, idx: Int) -> Unit)
) : RecyclerView.Adapter<RiskAssessmentListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.context.layoutInflater.inflate(
            R.layout.risk_assessment_list_row_item,
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
        private val view: View,
        private val onClickListener: (riskRecommend: RiskRecommendations, idx: Int) -> Unit

    ) : RecyclerView.ViewHolder(view) {

        fun bind(result: RiskRecommendations, position: Int) {
            view.riskAssessmentContentLabel.text = result.content
            val checkIcon = FontDrawable(appContext, R.string.fa_check_solid, true, false)
            checkIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_blue))

            val uncheckIcon = FontDrawable(appContext, R.string.fa_minus_solid, true, false)
            uncheckIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_gray))

            if (result.status == 1) {
                view.btnRiskStatus.setImageDrawable(checkIcon)
            } else {
                view.btnRiskStatus.setImageDrawable(uncheckIcon)
            }


            result.image?.let { view.btnRiskTypeButton.setImageResource(it) }
            view.setOnClickListener{
                onClickListener.invoke(result, position)
            }
        }
    }
}
