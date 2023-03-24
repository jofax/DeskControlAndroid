package com.smartpods.android.pulseecho.CustomUI

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.smartpods.android.pulseecho.R
import kotlinx.android.synthetic.main.bmi_details_dialog.*

class BMIDialogFragment: DialogFragment() {
    lateinit var spannableStringBuilder: SpannableStringBuilder
    private lateinit var strText: String
    private lateinit var formulatTextView: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            val dialogView = inflater.inflate(R.layout.bmi_details_dialog, null)
            formulatTextView = dialogView.findViewById<TextView>(R.id.bmi_dialog_formula)

            strText = getString(R.string.bmi_formular)
            spannableStringBuilder = SpannableStringBuilder(strText)
            val superscriptSpan = SuperscriptSpan()
            spannableStringBuilder.setSpan(superscriptSpan, strText.indexOf("2"), strText.indexOf("2") +
                    "2".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            showSmallSizeText("2")
            formulatTextView.text = spannableStringBuilder

            builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton(R.string.btn_ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()

                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun showSmallSizeText(string: String) {
        val relativeSizeSpan = RelativeSizeSpan(.5f)
        spannableStringBuilder.setSpan(relativeSizeSpan, strText.indexOf(string),
            strText.indexOf(string) + string.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

}