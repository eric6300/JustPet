package com.taiwan.justvet.justpet.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentChartBinding

class ChartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentChartBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_chart, container, false
        )

        return binding.root
    }
}