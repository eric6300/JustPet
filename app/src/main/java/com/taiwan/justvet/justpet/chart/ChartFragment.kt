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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.taiwan.justvet.justpet.DateFormatter
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.TAG
import com.taiwan.justvet.justpet.databinding.FragmentChartBinding
import java.util.*
import androidx.lifecycle.Observer


class ChartFragment : Fragment() {

    private lateinit var binding: FragmentChartBinding
    private lateinit var avatarAdapterChart: ChartPetAvatarAdapter
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
        setupPetProfile()

        viewModel.selectedProfile.observe(this, Observer {
            Log.d(TAG, "ChartFragment selected profile : $it")
            viewModel.getChartData(it)
        })

        viewModel.eventData.observe(this, Observer {

        })

        return binding.root
    }

    private fun setupPetProfile() {
        var lastPosition: Int? = -1

        val listOfProfile = binding.listOfProfileChart
        avatarAdapterChart = ChartPetAvatarAdapter(viewModel)

        listOfProfile.apply {
            PagerSnapHelper().attachToRecyclerView(this)

            this.adapter = avatarAdapterChart

            this.setOnScrollChangeListener { _, _, _, _, _ ->
                val newPosition = (listOfProfile.layoutManager as LinearLayoutManager)
                    .findFirstVisibleItemPosition()

                if (lastPosition != newPosition) {
                    viewModel.getProfileByPosition(newPosition)
                    lastPosition = newPosition
                }
            }
        }

        // set indicator of recyclerView
        val recyclerIndicator = binding.indicatorListOfProfileChart
        recyclerIndicator.apply {
            this.attachToRecyclerView(listOfProfile)
        }
    }

    fun setupChart() {
        // generate Dates
        val calendar = Calendar.getInstance()
        val d1 = calendar.time
        calendar.add(Calendar.DATE, 1)
        val d2 = calendar.time
        calendar.add(Calendar.DATE, 1)
        val d3 = calendar.time
        calendar.set(2019,8,17)
        val d4 = calendar.time

        val graph = binding.graphWeight

        // you can directly pass Date objects to DataPoint-Constructor
        // this will convert the Date to double via Date#getTime()
        val series = LineGraphSeries(
            arrayOf(
                DataPoint(d1, 4.0),
                DataPoint(d2, 6.0),
                DataPoint(d2, 5.0),
                DataPoint(d3, 3.0),
                DataPoint(d4, 9.0)
            )
        )

        series.isDrawDataPoints = true
        series.setOnDataPointTapListener { series, dataPoint ->
            val yyy = dataPoint.x
            Log.d(TAG, "dataPoint : ${dataPoint.y}")
            Log.d(TAG, "dataPoint : ${Date(yyy.toLong())}")
        }

        graph.addSeries(series)

        graph.viewport.isScalable = true

        // set date label formatter
        graph.gridLabelRenderer.labelFormatter = DateFormatter(JustPetApplication.appContext)
        graph.gridLabelRenderer.numHorizontalLabels = 4 // only 4 because of the space

        // set manual x bounds to have nice steps
        graph.viewport.setMinX(d1.time.toDouble())
        graph.viewport.setMaxX(d4.time.toDouble())
        graph.viewport.isXAxisBoundsManual = true

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graph.gridLabelRenderer.setHumanRounding(false, true)
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.gridLabelRenderer.labelsSpace = 20
    }
}