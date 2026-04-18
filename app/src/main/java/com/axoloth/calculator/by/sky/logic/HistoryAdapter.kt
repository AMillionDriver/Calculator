package com.axoloth.calculator.by.sky.logic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.database.HistoryEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter untuk menampilkan riwayat perhitungan dengan fitur interaktif.
 */
class HistoryAdapter(
    private var items: MutableList<HistoryEntity>,
    private val onItemClick: (HistoryEntity) -> Unit,
    private val onItemLongClick: (HistoryEntity) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val expression: TextView = v.findViewById(R.id.tv_history_expression)
        val result: TextView = v.findViewById(R.id.tv_history_result)
        val time: TextView? = v.findViewById(R.id.tv_history_time) // Optional if we add it to XML
    }

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.expression.text = item.expression
        holder.result.text = "= ${item.result}"
        
        // Format waktu jika TextView-nya ada
        holder.time?.text = timeFormat.format(Date(item.timestamp))

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(item)
            true
        }
    }

    override fun getItemCount() = items.size

    fun removeItem(item: HistoryEntity) {
        val position = items.indexOf(item)
        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateData(newItems: List<HistoryEntity>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
