package com.example.knowledgememorizationapp.user.Fragment

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.knowledgememorizationapp.databinding.FragmentStatisticsBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Flashcard filter
    private var flashcardDay = -1
    private var isFlashcardDaySelected = false

    // Quiz filter
    private var quizDay = -1
    private var isQuizDaySelected = false

    private var isMonthlyMode = true



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(userId)
            .child("flashcard_folders")

        setupFlashcardSpinners()
        setupQuizSpinners()
        binding.radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            isMonthlyMode = checkedId == binding.radioMonth.id
            loadStatistics()
            loadQuizStatistics()
        }



        return binding.root
    }

    // Flashcard spinner
    private var flashcardMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var flashcardYear = Calendar.getInstance().get(Calendar.YEAR)
    private var isFlashcardMonthSelected = false
    private var isFlashcardYearSelected = false

    // Quiz spinner
    private var quizMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var quizYear = Calendar.getInstance().get(Calendar.YEAR)
    private var isQuizMonthSelected = false
    private var isQuizYearSelected = false

    private fun setupFlashcardSpinners() {
        val days = (1..31).map { it.toString().padStart(2, '0') }
        val months = (1..12).map { it.toString().padStart(2, '0') }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear).map { it.toString() }

        val dayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, days)
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        binding.spinnerFlashcardDay.adapter = dayAdapter
        binding.spinnerFlashcardDay.setSelection(currentDay - 1)
        flashcardDay = currentDay


        binding.spinnerFlashcardMonth.adapter = monthAdapter
        binding.spinnerFlashcardMonth.setSelection(flashcardMonth - 1)

        binding.spinnerFlashcardYear.adapter = yearAdapter
        binding.spinnerFlashcardYear.setSelection(years.indexOf(flashcardYear.toString()))

        binding.spinnerFlashcardDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                flashcardDay = days[position].toInt()
                isFlashcardDaySelected = true
                if (isFlashcardMonthSelected && isFlashcardYearSelected) loadStatistics()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerFlashcardMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                flashcardMonth = months[position].toInt()
                isFlashcardMonthSelected = true
                if (isFlashcardYearSelected && isFlashcardDaySelected) loadStatistics()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerFlashcardYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                flashcardYear = years[position].toInt()
                isFlashcardYearSelected = true
                if (isFlashcardMonthSelected && isFlashcardDaySelected) loadStatistics()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupQuizSpinners() {
        val days = (1..31).map { it.toString().padStart(2, '0') }
        val months = (1..12).map { it.toString().padStart(2, '0') }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear).map { it.toString() }

        val dayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, days)
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        binding.spinnerQuizDay.adapter = dayAdapter
        binding.spinnerQuizDay.setSelection(currentDay - 1)
        quizDay = currentDay


        binding.spinnerQuizMonth.adapter = monthAdapter
        binding.spinnerQuizMonth.setSelection(quizMonth - 1)

        binding.spinnerQuizYear.adapter = yearAdapter
        binding.spinnerQuizYear.setSelection(years.indexOf(quizYear.toString()))

        binding.spinnerQuizDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                quizDay = days[position].toInt()
                isQuizDaySelected = true
                if (isQuizMonthSelected && isQuizYearSelected) loadQuizStatistics()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerQuizMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                quizMonth = months[position].toInt()
                isQuizMonthSelected = true
                if (isQuizYearSelected && isQuizDaySelected) loadQuizStatistics()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerQuizYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                quizYear = years[position].toInt()
                isQuizYearSelected = true
                if (isQuizMonthSelected && isQuizDaySelected) loadQuizStatistics()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun loadStatistics() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val flashcardList = mutableListOf<KnowledgeFlashcardModel>()
                for (folderSnapshot in snapshot.children) {
                    val knowledgesSnapshot = folderSnapshot.child("knowledges")
                    for (cardSnapshot in knowledgesSnapshot.children) {
                        val flashcard = cardSnapshot.getValue(KnowledgeFlashcardModel::class.java)
                        if (flashcard != null) {
                            flashcardList.add(flashcard)
                        }
                    }
                }

                val total = flashcardList.size
                val learned = flashcardList.count { it.learned }
                val notLearned = total - learned

                binding.tvTotalCards.text = "Tổng số thẻ: $total"
                binding.tvDoneCards.text = "Đã học: $learned"
                binding.tvNotDoneCards.text = "Chưa học: $notLearned"

                showPieChart(learned, notLearned)
                calculateAveragePerDay(flashcardList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showPieChart(learned: Int, notLearned: Int) {
        val entries = listOf(
            PieEntry(learned.toFloat(), "Đã học"),
            PieEntry(notLearned.toFloat(), "Chưa học")
        )
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(Color.GREEN, Color.LTGRAY)
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.invalidate()
    }

    private fun calculateAveragePerDay(flashcardList: List<KnowledgeFlashcardModel>) {
        val filteredList = flashcardList
            .filter { it.learned && it.doneTimestamp != null }

        val doneByPeriod = if (isMonthlyMode) {
            // Thống kê theo ngày trong tháng
            filteredList
                .filter {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.doneTimestamp!! }
                    val month = cal.get(Calendar.MONTH) + 1
                    val year = cal.get(Calendar.YEAR)
                    month == flashcardMonth && year == flashcardYear
                }
                .groupBy {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.doneTimestamp!! }
                    cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                }
        } else {
            // Thống kê theo tháng trong năm
            filteredList
                .filter {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.doneTimestamp!! }
                    val year = cal.get(Calendar.YEAR)
                    year == flashcardYear
                }
                .groupBy {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.doneTimestamp!! }
                    (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
                }
        }

        val average = if (doneByPeriod.isNotEmpty()) {
            doneByPeriod.map { it.value.size }.average()
        } else {
            0.0
        }

        val unit = if (isMonthlyMode) "ngày" else "tháng"
        binding.tvAveragePerDay.text = "Trung bình mỗi $unit: ${"%.2f".format(average)} thẻ"
        showBarChart(doneByPeriod)
    }



    private fun showBarChart(doneByPeriod: Map<String, List<KnowledgeFlashcardModel>>) {
        val sortedLabels = doneByPeriod.keys.sorted()
        val entries = sortedLabels.mapIndexed { index, label ->
            BarEntry(index.toFloat(), doneByPeriod[label]?.size?.toFloat() ?: 0f)
        }

        if (entries.isEmpty()) {
            binding.barChart.clear()
            binding.barChart.setNoDataText("Chưa có dữ liệu để hiển thị.")
            return
        }

        val labelDescription = if (isMonthlyMode) "Số thẻ đã học theo ngày" else "Số thẻ đã học theo tháng"
        val dataSet = BarDataSet(entries, labelDescription)
        dataSet.color = Color.BLUE

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        with(binding.barChart) {
            data = barData
            setFitBars(true)
            description.isEnabled = false

            xAxis.valueFormatter = IndexAxisValueFormatter(sortedLabels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            invalidate()
        }
    }


    private fun loadQuizStatistics() {
        val quizRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(auth.currentUser?.uid ?: "")
            .child("quiz_results")

        quizRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val scores = mutableListOf<Pair<Long, Int>>()

                for (quizSnap in snapshot.children) {
                    val score = quizSnap.child("score").getValue(Int::class.java)
                    val timestamp = quizSnap.child("timestamp").getValue(Long::class.java)

                    if (score != null && timestamp != null) {
                        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                        val month = cal.get(Calendar.MONTH) + 1
                        val year = cal.get(Calendar.YEAR)

                        if (isMonthlyMode) {
                            // Chế độ xem theo ngày trong tháng
                            if (month == quizMonth && year == quizYear) {
                                scores.add(timestamp to score)
                            }
                        } else {
                            // Chế độ xem theo tháng trong năm
                            if (year == quizYear) {
                                scores.add(timestamp to score)
                            }
                        }
                    }
                }

                val grouped = if (isMonthlyMode) {
                    // Nhóm theo ngày
                    scores.groupBy {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.first }
                        cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                    }
                } else {
                    // Nhóm theo tháng
                    scores.groupBy {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.first }
                        (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
                    }
                }

                val totalScore = scores.sumOf { it.second }
                val average = if (grouped.isNotEmpty()) totalScore.toDouble() / grouped.size else 0.0
                val unit = if (isMonthlyMode) "ngày" else "tháng"

                binding.tvTotalQuizzes.text = "Số bài quiz đã làm: ${scores.size}"
                binding.tvAverageScore.text = "Trung bình mỗi $unit: ${"%.2f".format(average)}"

                showQuizBarChart(grouped)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }



    private fun showQuizBarChart(quizData: Map<String, List<Pair<Long, Int>>>) {
        val sortedLabels = quizData.keys.sorted()
        val entries = sortedLabels.mapIndexed { index, label ->
            val averageScore = quizData[label]?.map { it.second }?.average() ?: 0.0
            BarEntry(index.toFloat(), averageScore.toFloat())
        }

        if (entries.isEmpty()) {
            binding.barChartQuiz.clear()
            binding.barChartQuiz.setNoDataText("Chưa có dữ liệu để hiển thị.")
            return
        }

        val labelDescription = if (isMonthlyMode) "Điểm quiz theo ngày" else "Điểm quiz theo tháng"
        val dataSet = BarDataSet(entries, labelDescription)
        dataSet.color = Color.MAGENTA

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        with(binding.barChartQuiz) {
            data = barData
            setFitBars(true)
            description.isEnabled = false

            xAxis.valueFormatter = IndexAxisValueFormatter(sortedLabels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            invalidate()
        }
    }

    private fun timestampToDate(ts: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(ts))
    }
}
