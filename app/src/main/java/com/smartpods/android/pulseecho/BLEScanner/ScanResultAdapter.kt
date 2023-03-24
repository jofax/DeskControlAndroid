package com.smartpods.android.pulseecho.BLEScanner

import android.bluetooth.le.ScanResult
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.smartpods.android.pulseecho.R
import info.androidhive.fontawesome.FontDrawable
import org.jetbrains.anko.layoutInflater
import kotlinx.android.synthetic.main.row_scan_result.btnConnectDevice
import kotlinx.android.synthetic.main.row_scan_result.connectProgressIndicator
import kotlinx.android.synthetic.main.row_scan_result.view.*

class ScanResultAdapter(
    private val items: List<ScanResult>,
    private val onClickListener: ((device: ScanResult, idx: Int) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.context.layoutInflater.inflate(
            R.layout.row_scan_result,
            parent,
            false
        )
        view.btnConnectDevice.isEnabled = true
        return ViewHolder(view, onClickListener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
    }

    class ViewHolder(
        private val view: View,
        private val onClickListener: ((device: ScanResult, idx: Int) -> Unit)

    ) : RecyclerView.ViewHolder(view) {

        fun bind(result: ScanResult, position: Int) {
            view.device_name.text = result.device.name ?: "Unnamed"
            view.mac_address.text = result.device.address
            view.signal_strength.text = "${result.rssi} dBm"
            view.btnConnectDevice.isEnabled = true
            view.btnConnectDevice.visibility = View.VISIBLE
            view.connectProgressIndicator.visibility = View.GONE
            //view.setOnClickListener { onClickListener.invoke(result) }
            view.btnConnectDevice.setOnClickListener {
                view.btnConnectDevice.isEnabled = false
                view.btnConnectDevice.visibility = View.GONE
                view.connectProgressIndicator.visibility = View.VISIBLE
                onClickListener.invoke(result, position)
            }

        }
    }
}
