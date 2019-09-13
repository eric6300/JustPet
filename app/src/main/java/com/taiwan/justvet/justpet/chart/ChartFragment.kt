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
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.taiwan.justvet.justpet.data.PetEvent
import kotlin.collections.ArrayList
import com.github.mikephil.charting.components.AxisBase
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import com.github.mikephil.charting.formatter.ValueFormatter
import com.taiwan.justvet.justpet.R


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
            viewModel.getSyndromeData(it)
        })

        viewModel.syndromeData.observe(this, Observer {
//            showSyndromeChart(it)
            showSyndromeChart(it)
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
        calendar.set(2019, 8, 17)
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

    fun showSyndromeChart(syndromeData: Map<Date, ArrayList<PetEvent>>) {
        val barChart = binding.graphSyndrome

        barChart.setExtraOffsets(10f, 0f, 10f, 10f)

        // enable scaling and dragging
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(true)

        // disable grid background
        barChart.setDrawGridBackground(false)

        barChart.setPinchZoom(true)

        // disable legend
        barChart.legend.isEnabled = false

        // set X axis
        val xAxis = barChart.xAxis
        xAxis.textSize = 16f
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = CustomValueFormatter()
        xAxis.labelCount = 4

        // set Y axis
        val yAxisRight = barChart.axisRight
        yAxisRight.isEnabled = false
        val yAxisLeft = barChart.axisLeft
        yAxisLeft.isEnabled = false

        // disable description
        val description = Description()
        description.isEnabled = false
        barChart.description = description

        // Setting Data
        val entries = ArrayList<BarEntry>()
        var i = 1f
        for (date in syndromeData.keys) {
            syndromeData[date]?.size?.let {
                entries.add(BarEntry(i, it.toFloat()))
                i += 1
            }
        }

        // set DataSet
        val dataset = BarDataSet(entries, "症狀")
        dataset.setDrawValues(true)
        dataset.valueFormatter = CustomValueFormatter()
        dataset.valueTextSize = 14f
        dataset.color = JustPetApplication.appContext.getColor(R.color.colorDiary)

        val data = BarData(dataset)
        barChart.data = data

        // refresh
        barChart.invalidate()
    }
}

class CustomValueFormatter : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val calendar = Calendar.getInstance()
        calendar.roll(Calendar.MONTH, value.toInt())
        val date = Date(calendar.timeInMillis)
        val sdf = SimpleDateFormat("M月", Locale.TAIWAN)
        return sdf.format(date)
    }

    override fun getBarLabel(barEntry: BarEntry?): String {
        barEntry?.y?.let {
            return if (it == 0f) {
                ""
            } else {
                it.toInt().toString()
            }
        }
        return barEntry?.y?.toInt().toString()
    }
}
