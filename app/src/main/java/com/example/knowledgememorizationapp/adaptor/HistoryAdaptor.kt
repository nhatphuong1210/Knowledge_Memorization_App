package com.example.knowledgememorizationapp.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.databinding.HistoryitemBinding
import com.example.knowledgememorizationapp.model.HistoryModelClass

class HistoryAdaptor(private val list: List<HistoryModelClass>) :
    RecyclerView.Adapter<HistoryAdaptor.SpinHistoryViewHolder>() {

    inner class SpinHistoryViewHolder(val binding: HistoryitemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinHistoryViewHolder {
        val binding = HistoryitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpinHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpinHistoryViewHolder, position: Int) {
        val item = list[position]
        holder.binding.Coin.text = item.reward
        holder.binding.Time.text = item.time

    }

    override fun getItemCount(): Int = list.size
}
