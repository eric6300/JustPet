package com.taiwan.justvet.justpet.chart

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentChartBinding
import com.taiwan.justvet.justpet.databinding.FragmentEditEventBinding
import com.taiwan.justvet.justpet.event.EditEventViewModel

class ChartFragment : Fragment() {

    private lateinit var binding: FragmentChartBinding
    private val viewModel: ChartViewModel by lazy {
        ViewModelProviders.of(this).get(ChartViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_chart, container, false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupChart()

        return binding.root
    }

    fun setupChart() {
        val lineChart = binding.chartLine

        lineChart.setExtraOffsets(5f, 0f, 5f, 5f)

        // enable scaling and dragging
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)

        // disable grid background
        lineChart.setDrawGridBackground(false)

        lineChart.setPinchZoom(true)

        // disable legend
        lineChart.legend.isEnabled = false

        // set X axis
        val xAxis = lineChart.xAxis
        xAxis.textSize = 16f
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 31f
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        // set Y axis
        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.setDrawLabels(true)
        yAxisLeft.setDrawZeroLine(true)
        yAxisLeft.textSize = 14f
        yAxisLeft.setDrawGridLinesBehindData(true)
        yAxisLeft.granularity = 1f

        // disable description
        val description = Description()
        description.isEnabled = false
        lineChart.description = description

        // Setting Data
        val entries = ArrayList<Entry>()
        entries.add(Entry(1f, 4.5f))
        entries.add(Entry(6f, 4.6f))
        entries.add(Entry(10f, 5.2f))
        entries.add(Entry(20f, 3.9f))

        // set lineDataSet
        val dataset = LineDataSet(entries, "體重")
        dataset.setDrawCircles(true)
        dataset.setDrawValues(true)
        dataset.lineWidth = 3f
        dataset.circleRadius = 6f
        dataset.circleHoleRadius = 3f
        dataset.valueTextSize = 16f
        dataset.color = this.context!!.getColor(R.color.colorDiaryDark)
        dataset.setCircleColor(this.context!!.getColor(R.color.colorDiaryDark))

        val data = LineData(dataset)
        lineChart.data = data

        // refresh
        lineChart.invalidate()
    }
}