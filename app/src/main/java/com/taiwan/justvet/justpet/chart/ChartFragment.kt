package com.taiwan.justvet.justpet.chart

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.TAG
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.databinding.FragmentChartBinding
import java.util.*
import kotlin.collections.ArrayList


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
            inflater, R.layout.fragment_chart, container, false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

//        setupChart()
        setupPetProfile()

        viewModel.selectedProfile.observe(this, Observer {
            Log.d(TAG, "ChartFragment selected profile : ${it.profileId}")
            viewModel.getSyndromeData(it)
            viewModel.getYearData(it)
        })

        viewModel.yearData.observe(this, Observer {
            showWeightData(it)
        })

        viewModel.syndromeData.observe(this, Observer {
            it?.let {
                showSyndromeChart(it)
            }
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

    fun showWeightData(yearData: List<PetEvent>) {
        val lineChart = binding.graphWeight
        lineChart.setExtraOffsets(10f, 0f, 10f, 10f)
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
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelCount = 4
        xAxis.valueFormatter = WeightValueFormatter()

        // set Y axis
        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.isEnabled = false

        // disable description
        val description = Description()
        description.isEnabled = false
        lineChart.description = description

        // Setting Data
        val weightData = yearData.filter {
            it.weight != null
        }
        val entries = ArrayList<Entry>()
        for (event in weightData) {
            event.timestamp?.let { timestamp ->
                event.weight?.let { weight ->
                    entries.add(Entry(timestamp.toFloat(),weight.toFloat()))
                }
            }
        }

        // set DataSet
        val dataset = LineDataSet(entries, "體重")
        dataset.setDrawValues(true)
        dataset.valueFormatter = CustomValueFormatter()
        dataset.valueTextSize = 14f
        dataset.color = JustPetApplication.appContext.getColor(R.color.colorDiaryDark)
        dataset.circleHoleColor = JustPetApplication.appContext.getColor(R.color.colorDiaryDark)
        dataset.setCircleColor(JustPetApplication.appContext.getColor(R.color.transparent))

        val data = LineData(dataset)
        lineChart.data = data

        // refresh
        lineChart.invalidate()
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

class WeightValueFormatter : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val date = Date(value.toLong())
        val sdf = SimpleDateFormat("M/dd", Locale.TAIWAN)
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
