package com.smartpods.android.pulseecho.Adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smartpods.android.pulseecho.Model.UserStatisticItem
import com.smartpods.android.pulseecho.R
import kotlinx.android.synthetic.main.mode_percentage_item.view.*
import org.jetbrains.anko.layoutInflater

class ModePercenstageAdapter(
        private val items: MutableList<UserStatisticItem>,
        private val onClickListener: ((data: UserStatisticItem, idx: Int) -> Unit)
) : RecyclerView.Adapter<ModePercenstageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = parent.context.layoutInflater.inflate(
                R.layout.mode_percentage_item,
                parent,
                false
        )

        view.post{
            view.layoutParams.width = view.width / 2
            view.requestLayout()
        }

        return ViewHolder(view, onClickListener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
//        holder.itemView.post{
//            holder.itemView.layoutParams.width = holder.itemView.width / 2
//            holder.itemView.requestLayout()
//        }
        holder.bind(item, position)
    }

    class ViewHolder(
            private val view: View,
            private val onClickListener: (data: UserStatisticItem, idx: Int) -> Unit

    ) : RecyclerView.ViewHolder(view) {

        fun bind(result: UserStatisticItem, position: Int) {
            view.statisticPercentageTitle.text = result.Title
            view.statisticPercentageValue.text = result.ItemValue
            view.setOnClickListener{
                onClickListener.invoke(result, position)
            }
        }
    }
}