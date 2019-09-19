package com.taiwan.justvet.justpet.family

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogFamilyBinding

class FamilyDialog : AppCompatDialogFragment() {

    private val viewModel: FamilyViewModel by lazy {
        ViewModelProviders.of(this).get(FamilyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: DialogFamilyBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_family, container, false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.sendInviteCompleted.observe(this, Observer {
            if (it) {
                findNavController().navigate(R.id.navigate_to_homeFragment)
                viewModel.sendInviteCompleted()
            }
        })

        return binding.root
    }
}