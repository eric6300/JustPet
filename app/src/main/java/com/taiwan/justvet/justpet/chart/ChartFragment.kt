package com.taiwan.justvet.justpet.chart

import android.icu.text.DecimalFormat
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
import com.taiwan.justvet.justpet.EMPTY_STRING
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentChartBinding
import com.taiwan.justvet.justpet.util.Util.getString
import com.taiwan.justvet.justpet.util.toChartDateFormat
import com.taiwan.justvet.justpet.util.toMonthOnlyFormat

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

        viewModel.selectedPetProfile.observe(this, Observer {
            it?.let {
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
        weightChart.let {
            it.setExtraOffsets(10f, 30f, 10f, 10f)

            // enable scaling and dragging
            it.isDragEnabled = false
            it.setScaleEnabled(false)

            // disable grid background
            it.setDrawGridBackground(false)

            // disable legend
            it.legend.isEnabled = false

            // disable data text
            it.setNoDataText(EMPTY_STRING)
        }

        // set X axis
        val xAxis = weightChart.xAxis
        xAxis.let {
            it.textSize = 16f
            it.setDrawAxisLine(true)
            it.setDrawGridLines(false)
            it.setDrawLabels(false)
            it.position = XAxis.XAxisPosition.BOTTOM
            it.granularity = 86400f
            it.axisMinimum = (viewModel.threeMonthsAgoTimestamp * 0.9995).toFloat()
            it.axisMaximum = (viewModel.nowTimestamp * 1.0005).toFloat()
        }

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
        syndromeChart.let {
            it.setExtraOffsets(10f, 30f, 10f, 10f)

            // disable scaling, dragging and zooming
            it.isDragEnabled = false
            it.setScaleEnabled(false)
            it.setPinchZoom(false)

            // disable grid background
            it.setDrawGridBackground(false)

            // disable legend
            it.legend.isEnabled = false

            // disable data text
            it.setNoDataText(EMPTY_STRING)
        }


        // set X axis
        val xAxis = syndromeChart.xAxis
        xAxis.let {
            xAxis.textSize = 16f
            xAxis.setDrawAxisLine(true)
            xAxis.setDrawGridLines(false)
            xAxis.setDrawLabels(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = SyndromeFormatter()
            xAxis.granularity = 1f
        }

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
        weightChart.data = LineData()
        weightChart.invalidate()

        // set DataSet
        val dataset = LineDataSet(entries, getString(R.string.title_body_weight))
        dataset.let {
            it.setDrawValues(true)
            it.valueFormatter = SyndromeFormatter()
            it.valueTextSize = 20f
            it.color = JustPetApplication.appContext.getColor(R.color.grey_bdbdbd)
            it.circleHoleColor = JustPetApplication.appContext.getColor(R.color.grey_bdbdbd)
            it.lineWidth = 2f
            it.setCircleColor(JustPetApplication.appContext.getColor(R.color.grey_bdbdbd))
            it.circleRadius = 4f
            it.valueFormatter = WeightChartFormatter()
        }

        weightChart.axisLeft.axisMaximum = ((dataset.yMax) * 1.05).toFloat()

        weightChart.data = LineData(dataset)
        val markerView = WeightMarkerView()
        weightChart.marker = markerView

        // refresh
        weightChart.notifyDataSetChanged()
        weightChart.invalidate()
    }

    private fun showSyndromeData(entries: List<BarEntry>) {
        syndromeChart.data = BarData()
        syndromeChart.invalidate()

        // set DataSet
        val dataset = BarDataSet(entries, getString(R.string.text_syndrome))
        dataset.let {
            it.setDrawValues(true)
            it.valueFormatter = SyndromeFormatter()
            it.valueTextSize = 14f
            it.color = JustPetApplication.appContext.getColor(R.color.grey_bdbdbd)
        }

        syndromeChart.data = BarData(dataset)
        if (binding.filterChartGroup.position == 0) {
            syndromeChart.moveViewToX(9.5f)
            syndromeChart.setVisibleXRangeMaximum(3f)
        }
        // refresh
        syndromeChart.notifyDataSetChanged()
        syndromeChart.invalidate()
    }

    private fun setupSegmentedButtonGroup() {
        binding.filterChartGroup.setOnPositionChangedListener { index ->
            when (index) {
                0 -> {
                    weightChart.xAxis.axisMinimum =
                        (viewModel.threeMonthsAgoTimestamp * 0.9995).toFloat()
                    weightChart.xAxis.axisMaximum =
                        (viewModel.nowTimestamp * 1.0005).toFloat()
                    weightChart.fitScreen()
                    syndromeChart.let {
                        it.fitScreen()
                        it.moveViewToX(9.5f)
                        it.setVisibleXRangeMaximum(3f)
                    }

                }
                1 -> {
                    weightChart.xAxis.axisMinimum =
                        (viewModel.sixMonthsAgoTimestamp * 0.9995).toFloat()
                    weightChart.xAxis.axisMaximum =
                        (viewModel.nowTimestamp * 1.001).toFloat()
                    weightChart.fitScreen()
                    syndromeChart.let {
                        it.fitScreen()
                        it.moveViewToX(6.5f)
                        it.setVisibleXRangeMaximum(6f)
                    }

                }
                2 -> {
                    weightChart.xAxis.axisMinimum =
                        (viewModel.oneYearAgoTimestamp * 0.9995).toFloat()
                    weightChart.xAxis.axisMaximum =
                        (viewModel.nowTimestamp * 1.0020).toFloat()
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
        return calendar.time.toMonthOnlyFormat()
    }

    override fun getBarLabel(barEntry: BarEntry?): String {
        barEntry?.y?.let {
            return if (it == 0f) {
                EMPTY_STRING
            } else {
                it.toInt().toString()
            }
        }
        return barEntry?.y?.toInt().toString()
    }
}

class WeightChartFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return JustPetApplication.appContext.getString(
            R.string.chart_weight_unit,
            DecimalFormat(getString(R.string.chart_weight_value_format)).format(value)
        )
    }
}

class WeightMarkerView :
    MarkerView(JustPetApplication.appContext, R.layout.item_chart_marker_view) {

    val text = findViewById<TextView>(R.id.text_marker_view)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.x?.toLong()?.let {
            text.text = it.toChartDateFormat()
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat() * 2)
    }


}