package com.smartpods.android.pulseecho.CustomUI

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.content.DialogInterface




abstract class SPCustomDialog {
    abstract  val dialogView: View
    abstract  val builder: AlertDialog.Builder

    open var cancelable: Boolean = true
    open var isBgTransparent: Boolean = true

    open var dialog: AlertDialog? = null

    open fun create(): AlertDialog {
        dialog = builder
            .setCancelable(cancelable)
            .create()

        if (isBgTransparent)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog!!
    }

    open fun onCancelListener(func: () -> Unit): AlertDialog.Builder? =
        builder.setOnCancelListener {
            func()
        }
}

object SPAlertDialogView {
    fun showDialog(context: Context?,
        title: String?,
        message: String?,
                   positiveButton: String?,
                   positiveOnClickListener: DialogInterface.OnClickListener?,
                   negativeButton: String? = null,
                   negativeOnClickListener: DialogInterface.OnClickListener? = null,
    ) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setPositiveButton(positiveButton, positiveOnClickListener)

        if (negativeButton != null && negativeOnClickListener != null) {
            dialog.setNegativeButton(negativeButton, negativeOnClickListener)
        }


        dialog.show()
    }
}
