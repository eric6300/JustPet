package com.taiwan.justvet.justpet.chart

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.taiwan.justvet.justpet.DateFormatter
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.TAG
import com.taiwan.justvet.justpet.databinding.FragmentChartBinding


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
            inflater, com.taiwan.justvet.justpet.R.layout.fragment_chart, container, false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupChart()

        return binding.root
    }

    fun setupChart() {
        // generate Dates
        val calendar = Calendar.getInstance()
        val d1 = calendar.getTime()
        calendar.add(Calendar.DATE, 1)
        val d2 = calendar.getTime()
        calendar.add(Calendar.DATE, 1)
        val d3 = calendar.getTime()

        val graph = binding.graph

        // you can directly pass Date objects to DataPoint-Constructor
        // this will convert the Date to double via Date#getTime()
        val series = LineGraphSeries(
            arrayOf<DataPoint>(
                DataPoint(d1, 4.0),
                DataPoint(d2, 5.0),
                DataPoint(d2, 6.0),
                DataPoint(d3, 3.0)
            )
        )

        series.isDrawDataPoints = true
        series.setOnDataPointTapListener { series, dataPoint ->
            Log.d(TAG, "dataPoint : ${dataPoint.y}")
        }

        graph.addSeries(series)

        graph.viewport.isScalable = true

        // set date label formatter
        graph.gridLabelRenderer.labelFormatter = DateFormatter(JustPetApplication.appContext)
        graph.gridLabelRenderer.numHorizontalLabels = 3 // only 4 because of the space

        // set manual x bounds to have nice steps
        graph.viewport.setMinX(d1.time.toDouble())
        graph.viewport.setMaxX(d3.time.toDouble())
        graph.viewport.isXAxisBoundsManual = true

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graph.gridLabelRenderer.setHumanRounding(false, true)
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.gridLabelRenderer.labelsSpace = 20
    }
}