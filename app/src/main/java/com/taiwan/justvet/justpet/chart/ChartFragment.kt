package com.taiwan.justvet.justpet.chart

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.ERIC
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.databinding.FragmentChartBinding
import java.util.*
import kotlin.collections.ArrayList


class ChartFragment : Fragment() {

    private lateinit var binding: FragmentChartBinding
    private lateinit var avatarAdapterChart: ChartPetAvatarAdapter
    private lateinit var weightChart: LineChart
    private lateinit var syndromeChart: BarChart
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

        setupPetProfile()
        setupSyndromeChart()
        setupWeightChart()

        viewModel.selectedProfile.observe(this, Observer {
            Log.d(ERIC, "ChartFragment selected profile : ${it.profileId}")
            viewModel.getSyndromeData(it)
            viewModel.getYearData(it)
        })

        viewModel.yearData.observe(this, Observer {
            showWeightData(it)
        })

        viewModel.syndromeData.observe(this, Observer {
            it?.let {
                showSyndromeData(it)
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

    private fun setupWeightChart() {
        weightChart = binding.graphWeight
//        weightChart.setExtraOffsets(0f, 0f, 0f, 0f)
        // enable scaling and dragging
        weightChart.isDragEnabled = false
        weightChart.setScaleEnabled(false)
        // disable grid background
        weightChart.setDrawGridBackground(false)
        // disable legend
        weightChart.legend.isEnabled = false

        // set X axis
        val xAxis = weightChart.xAxis
        xAxis.textSize = 16f
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.labelCount = 2
//        xAxis.valueFormatter = WeightChartFormatter()
        xAxis.granularity = 86400f
        xAxis.axisMinimum = viewModel.threeMonthsAgoTimestamp.toFloat()
        xAxis.axisMaximum = (viewModel.nowTimestamp).toFloat()

        // set Y axis
        val yAxisRight = weightChart.axisRight
        yAxisRight.isEnabled = false
        val yAxisLeft = weightChart.axisLeft
        yAxisLeft.isEnabled = false

        // disable description
        val description = Description()
        description.isEnabled = false
        weightChart.description = description
    }

    private fun setupSyndromeChart() {
        syndromeChart = binding.graphSyndrome

        syndromeChart.setExtraOffsets(10f, 0f, 10f, 10f)

        // disable scaling, dragging and zooming
        syndromeChart.isDragEnabled = false
        syndromeChart.setScaleEnabled(false)
        syndromeChart.setPinchZoom(false)

        // disable grid background
        syndromeChart.setDrawGridBackground(false)

        // disable legend
        syndromeChart.legend.isEnabled = false

        // set X axis
        val xAxis = syndromeChart.xAxis
        xAxis.textSize = 16f
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = SyndromeFormatter()
        xAxis.granularity = 1f

        // set Y axis
        val yAxisRight = syndromeChart.axisRight
        yAxisRight.isEnabled = false
        val yAxisLeft = syndromeChart.axisLeft
        yAxisLeft.isEnabled = false

        // disable description
        val description = Description()
        description.isEnabled = false
        syndromeChart.description = description
    }

    fun showWeightData(yearData: List<PetEvent>) {
        // Setting Data
        val weightData = yearData.filter {
            it.weight != null
        }
        val entries = ArrayList<Entry>()
        for (event in weightData) {
            event.timestamp?.let { timestamp ->

                event.weight?.let { weight ->
                    entries.add(Entry(timestamp.toFloat(), weight.toFloat()))
                }
            }
        }

        // set DataSet
        val dataset = LineDataSet(entries, "體重")
        dataset.setDrawValues(true)
        dataset.valueFormatter = SyndromeFormatter()
        dataset.valueTextSize = 16f
        dataset.color = JustPetApplication.appContext.getColor(R.color.colorDiaryDark)
        dataset.circleHoleColor = JustPetApplication.appContext.getColor(R.color.colorDiaryDark)
        dataset.lineWidth = 2f
        dataset.setCircleColor(JustPetApplication.appContext.getColor(R.color.colorDiaryDark))
        dataset.circleRadius = 4f
        dataset.valueFormatter = WeightChartFormatter()
        weightChart.axisLeft.axisMaximum = ((dataset.yMax) * 1.05).toFloat()

        weightChart.data = LineData(dataset)
        val markerView = WeightMarkerView()
        weightChart.marker = markerView

        // refresh
        weightChart.invalidate()
    }

    fun showSyndromeData(syndromeData: Map<Date, ArrayList<PetEvent>>) {
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
        dataset.valueFormatter = SyndromeFormatter()
        dataset.valueTextSize = 14f
        dataset.color = JustPetApplication.appContext.getColor(R.color.colorDiary)

        syndromeChart.data = BarData(dataset)
        syndromeChart.moveViewToX(12f)
        syndromeChart.setVisibleXRangeMaximum(3f)

        // refresh
        syndromeChart.invalidate()
        setupSegmentedButtonGroup()
    }

    private fun setupSegmentedButtonGroup() {
        binding.filterChartGroup.setOnPositionChangedListener { index ->
            when (index) {
                0 -> {
                    weightChart.xAxis.axisMinimum = viewModel.threeMonthsAgoTimestamp.toFloat()
                    weightChart.fitScreen()
                    syndromeChart.let {
                        it.fitScreen()
                        it.moveViewToX(12f)
                        it.setVisibleXRangeMaximum(3f)
                    }
                }
                1 -> {
                    weightChart.xAxis.axisMinimum = viewModel.sixMonthsAgoTimestamp.toFloat()
                    weightChart.fitScreen()
                    syndromeChart.let {
                        it.fitScreen()
                        it.moveViewToX(6.5f)
                        it.setVisibleXRangeMaximum(6f)
                    }
                }
                2 -> {
                    weightChart.xAxis.axisMinimum = viewModel.oneYearAgoTimestamp.toFloat()
                    weightChart.fitScreen()
                    syndromeChart.let {
                        it.fitScreen()
                        it.moveViewToX(0f)
                        it.setVisibleXRangeMaximum(12f)
                    }
                }
            }
        }
    }
}

class SyndromeFormatter : ValueFormatter() {

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

class WeightChartFormatter : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val date = Date(value.toLong())
        val sdf = SimpleDateFormat("M月", Locale.TAIWAN)
        return sdf.format(date)
    }

    override fun getFormattedValue(value: Float): String {
        return "$value kg"
    }
}

class WeightMarkerView :
    MarkerView(JustPetApplication.appContext, R.layout.item_chart_marker_view) {

    val text = findViewById<TextView>(R.id.text_marker_view)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.x?.toLong()?.let {
            val date = Date(it)
            val sdf = SimpleDateFormat("M月dd日", Locale.TAIWAN)
            val displayString = sdf.format(date)
            text.text = displayString
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat() * 2)
    }


}