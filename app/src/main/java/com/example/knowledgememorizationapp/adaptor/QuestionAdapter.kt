package com.example.knowledgememorizationapp.adaptor

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.QuestionDetailActivity
import com.example.knowledgememorizationapp.databinding.QuestionItemBinding
import com.example.knowledgememorizationapp.model.QuestionModel
import com.google.gson.Gson
import com.google.gson.JsonObject

class QuestionAdapter(
    private var questionList: List<QuestionModel>,
    private val categoryId: String,
    private val requireActivity: FragmentActivity
) : RecyclerView.Adapter<QuestionAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: QuestionItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = QuestionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = questionList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val question = questionList[position]
        holder.binding.questionText.text = question.question ?: "Không có dữ liệu"

        holder.itemView.setOnClickListener {
            val intent = Intent(requireActivity, QuestionDetailActivity::class.java).apply {
                putExtra("categoryId", categoryId)
                putExtra("questionId", question.questionId)
                putExtra("questionText", question.question ?: "Không có dữ liệu")
                putExtra("optionA", question.answers.getOrNull(0) ?: "")
                putExtra("optionB", question.answers.getOrNull(1) ?: "")
                putExtra("optionC", question.answers.getOrNull(2) ?: "")
                putExtra("optionD", question.answers.getOrNull(3) ?: "")
                putExtra("correctAnswer", question.correctAnswer ?: "")
            }
            requireActivity.startActivity(intent)
        }
    }

    fun setDataFromJson(jsonData: String) {
        val gson = Gson()
        val jsonObject = gson.fromJson(jsonData, JsonObject::class.java)
        val questions = mutableListOf<QuestionModel>()

        jsonObject.getAsJsonObject("Users")?.entrySet()?.forEach { userEntry ->
            val userJson = userEntry.value.asJsonObject
            userJson.getAsJsonObject("quiz_categories")?.entrySet()?.forEach { categoryEntry ->
                val categoryKey = categoryEntry.key
                val categoryJson = categoryEntry.value.asJsonObject
                categoryJson.getAsJsonObject("questions")?.entrySet()?.forEach { questionEntry ->
                    val questionJson = questionEntry.value.asJsonObject
                    val question = QuestionModel(
                        questionId = questionEntry.key ?: "",
                        categoryId = categoryKey,
                        question = questionJson.get("question")?.asString ?: "",
                        answers = questionJson.getAsJsonArray("answers")?.map { it.asString } ?: listOf("", "", "", ""),
                        correctAnswer = questionJson.get("correctAnswer")?.asString ?: ""
                    )
                    questions.add(question)
                }
            }
        }

        questionList = questions
        notifyDataSetChanged()
    }
}
