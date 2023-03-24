package com.smartpods.android.pulseecho.Fragments.Settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.ViewModel.PasswordChangeViewModel

class PasswordChangeFragment : Fragment() {

    companion object {
        fun newInstance() = PasswordChangeFragment()
    }

    private lateinit var viewModel: PasswordChangeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.password_change_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PasswordChangeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}