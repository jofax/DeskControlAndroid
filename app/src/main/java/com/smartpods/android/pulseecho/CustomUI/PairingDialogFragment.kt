package com.smartpods.android.pulseecho.CustomUI

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface

import android.widget.VideoView
import android.os.Bundle
import android.net.Uri
import android.os.Environment
import android.net.Uri.*
import android.widget.MediaController
import androidx.fragment.app.DialogFragment
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext

class PairingDialogFragment: DialogFragment() {
    private lateinit var mPairVideoView: VideoView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            val dialogView = inflater.inflate(com.smartpods.android.pulseecho.R.layout.pairing_video_dialog, null)
            mPairVideoView = dialogView.findViewById<VideoView>(com.smartpods.android.pulseecho.R.id.videoView)
            val mediaController = MediaController(appContext)
            mediaController.setAnchorView(mPairVideoView)
            //specify the location of media file
            //val uri: Uri = parse(Environment.getExternalStorageDirectory().path + "/OtherResources/videos/pairing_instruction.mp4")
            val uri: Uri = Uri.parse("android.resource://com.smartpods.android.pulseecho/raw/pairing_instruction.mp4")
            //mediaController.setDataSource(applicationContext, Uri.parse("android.resource://$packageName/raw/test_video"))
            //mediaController.setDataSource(applicationContext, selectedVideoUri)

            //Setting MediaController and URI, then starting the videoView
            mPairVideoView.setMediaController(mediaController)

            mPairVideoView.setVideoURI(uri)
            mPairVideoView.requestFocus()
            mPairVideoView.start()

            builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton(
                    com.smartpods.android.pulseecho.R.string.btn_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()

                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}