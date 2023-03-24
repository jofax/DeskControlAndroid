package com.smartpods.android.pulseecho.CustomUI

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.PulseCommands
import com.smartpods.android.pulseecho.Utilities.Utilities

class InteractivePopDialog(movement: String): DialogFragment() {
    var nextMovement: String = movement
    private lateinit var nextMovementTextView: TextView
    private lateinit var imgInteractiveHeart: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            val dialogView = inflater.inflate(R.layout.interactive_mode_popup, null)
            builder.setCancelable(false)
            nextMovementTextView = dialogView.findViewById<TextView>(R.id.interactiveMovement)
            imgInteractiveHeart = dialogView.findViewById<ImageView>(R.id.imgInteractiveHeart)

            nextMovementTextView.text = nextMovement

            if (this::imgInteractiveHeart.isInitialized) {
                imgInteractiveHeart.setOnClickListener{
                    var command = PulseCommands()
                    val acknowledgeStatus = command.GetAcknowkedgePendingMovement()
                    println("InteractivePopDialog GetAcknowkedgePendingMovement")
                    SPBleManager.sendCommandToCharacteristic(Utilities.getSmartpodsDevice(), acknowledgeStatus)
                    this.dismiss()
                }
            }

            builder.setView(dialogView)
            builder.setCancelable(false)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}