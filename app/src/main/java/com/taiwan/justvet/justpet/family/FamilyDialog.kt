package com.taiwan.justvet.justpet.family

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogFamilyBinding

class FamilyDialog : AppCompatDialogFragment() {

    private lateinit var binding: DialogFamilyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val petProfile = FamilyDialogArgs.fromBundle(arguments!!).petProfile
        val viewModel =
            ViewModelProviders.of(this, FamilyViewModelFactory(petProfile))
                .get(FamilyViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_family, container, false
        )

        binding.let {
            it.viewModel = viewModel
            it.lifecycleOwner = this
            it.listFamily.adapter = FamilyEmailAdapter(viewModel)
        }

        viewModel.leaveDialog.observe(this, Observer {
            if (it) {
                dismiss()
                viewModel.leaveDialogComplete()
            }
        })

        return binding.root
    }
}