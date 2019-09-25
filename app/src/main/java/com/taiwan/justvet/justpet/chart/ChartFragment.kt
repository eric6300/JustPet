package com.taiwan.justvet.justpet.chart

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
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
import com.taiwan.justvet.justpet.databinding.FragmentChartBinding
import java.util.*

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
        setupSegmentedButtonGroup()

        viewModel.selectedProfile.observe(this, Observer {
            it?.let {
                weightChart.data = LineData()
                weightChart.invalidate()
                syndromeChart.data = BarData()
                syndromeChart.invalidate()
                if (binding.filterChartGroup.position != 0) {
                    binding.filterChartGroup.setPosition(0, true)
                }
                viewModel.getSyndromeData(it)
                viewModel.getWeightData(it)
            }
        })

        viewModel.weightEntries.observe(this, Observer {
            it?.let {
                showWeightData(it)
            }
        })

        viewModel.syndromeEntries.observe(this, Observer {
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
        weightChart.setExtraOffsets(10f, 30f, 10f, 10f)
        // enable scaling and dragging
        weightChart.isDragEnabled = false
        weightChart.setScaleEnabled(false)
        // disable grid background
        weightChart.setDrawGridBackground(false)
        // disable legend
        weightChart.legend.isEnabled = false
        weightChart.setNoDataText("")

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
        xAxis.axisMaximum = (viewModel.nowTimestamp * 1.0007).toFloat()

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
        syndromeChart.setExtraOffsets(10f, 30f, 10f, 10f)
        // disable scaling, dragging and zooming
        syndromeChart.isDragEnabled = false
        syndromeChart.setScaleEnabled(false)
        syndromeChart.setPinchZoom(false)
        // disable grid background
        syndromeChart.setDrawGridBackground(false)
        // disable legend
        syndromeChart.legend.isEnabled = false
        syndromeChart.setNoDataText("")

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

    private fun showWeightData(entries: List<Entry>) {

        if (viewModel.threeMonthsWeight.value == 0 || viewModel.oneYearWeight.value == null) {
            binding.textNoWeightData.visibility = View.VISIBLE
        } else {
            binding.textNoWeightData.visibility = View.GONE
        }

        // set DataSet
        val dataset = LineDataSet(entries, "體重")
        dataset.setDrawValues(true)
        dataset.valueFormatter = SyndromeFormatter()
        dataset.valueTextSize = 20f
        dataset.color = JustPetApplication.appContext.getColor(R.color.grey_bdbdbd)
        dataset.circleHoleColor = JustPetApplication.appContext.getColor(R.color.grey_bdbdbd)
        dataset.lineWidth = 2f
        dataset.setCircleColor(JustPetApplication.appContext.getColor(R.color.grey_bdbdbd))
        dataset.circleRadius = 4f
        dataset.valueFormatter = WeightChartFormatter()
        weightChart.axisLeft.axisMaximum = ((dataset.yMax) * 1.05).toFloat()

        weightChart.data = LineData(dataset)
        val markerView = WeightMarkerView()
        weightChart.marker = markerView

        // refresh
        weightChart.notifyDataSetChanged()
        weightChart.invalidate()
    }

    private fun showSyndromeData(entries: List<BarEntry>) {

        if (viewModel.threeMonthsSyndrome.value == 0 || viewModel.threeMonthsSyndrome.value == null) {
            binding.textNoSyndromeData.visibility = View.VISIBLE
        } else {
            binding.textNoSyndromeData.visibility = View.GONE
        }

        // set DataSet
        val dataset = BarDataSet(entries, "症狀")
        dataset.setDrawValues(true)
        dataset.valueFormatter = SyndromeFormatter()
        dataset.valueTextSize = 14f
        dataset.color = JustPetApplication.appContext.getColor(R.color.grey_bdbdbd)

        syndromeChart.data = BarData(dataset)
        syndromeChart.moveViewToX(9.5f)
        syndromeChart.setVisibleXRangeMaximum(3f)

        // refresh
        syndromeChart.notifyDataSetChanged()
        syndromeChart.invalidate()
    }

    private fun setupSegmentedButtonGroup() {
        val noSyndromeDataText = binding.textNoSyndromeData
        val noWeightDataText = binding.textNoWeightData
        binding.filterChartGroup.setOnPositionChangedListener { index ->
            when (index) {
                0 -> {
                    weightChart.xAxis.axisMinimum = viewModel.threeMonthsAgoTimestamp.toFloat()
                    weightChart.fitScreen()
                    syndromeChart.let {
                        it.fitScreen()
                        it.moveViewToX(9.5f)
                        it.setVisibleXRangeMaximum(3f)
                    }

                    if (viewModel.threeMonthsWeight.value == 0 || viewModel.threeMonthsWeight.value == null) {
                        noWeightDataText.visibility = View.VISIBLE
                    } else {
                        noWeightDataText.visibility = View.GONE
                    }

                    if (viewModel.threeMonthsSyndrome.value == 0 || viewModel.threeMonthsSyndrome.value == null) {
                        noSyndromeDataText.visibility = View.VISIBLE
                    } else {
                        noSyndromeDataText.visibility = View.GONE
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

                    if (viewModel.sixMonthsWeight.value == 0 || viewModel.sixMonthsWeight.value == null) {
                        noWeightDataText.visibility = View.VISIBLE
                    } else {
                        noWeightDataText.visibility = View.GONE
                    }

                    if (viewModel.sixMonthsWeight.value == 0 || viewModel.sixMonthsWeight.value == null) {
                        noSyndromeDataText.visibility = View.VISIBLE
                    } else {
                        noSyndromeDataText.visibility = View.GONE
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

                    if (viewModel.oneYearWeight.value == 0 || viewModel.oneYearWeight.value == null) {
                        noWeightDataText.visibility = View.VISIBLE
                    } else {
                        noWeightDataText.visibility = View.GONE
                    }

                    if (viewModel.oneYearWeight.value == 0 || viewModel.oneYearWeight.value == null) {
                        noSyndromeDataText.visibility = View.VISIBLE
                    } else {
                        noSyndromeDataText.visibility = View.GONE
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
        val date = Date(value.toLong() * 1000)
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
            val date = Date(it * 1000)
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