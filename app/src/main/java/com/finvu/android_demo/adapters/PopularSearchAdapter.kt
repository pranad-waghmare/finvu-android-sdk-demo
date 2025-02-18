package com.finvu.android_demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finvu.android.publicInterface.FIPInfo
import com.finvu.android_demo.R


class PopularSearchAdapter(
    val accountAppArrayList: List<FIPInfo>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<PopularSearchAdapter.PopularSearchVH>() {
    interface OnItemClickListener {
        fun onItemClick(item: FIPInfo, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularSearchVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_popular_search, parent, false)
        return PopularSearchVH(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: PopularSearchVH, position: Int) {
        holder.onBindView(accountAppArrayList[position].productName.toString())
    }

    override fun getItemCount(): Int {
        return accountAppArrayList.size
    }

    inner class PopularSearchVH(
        view: View,
        private val onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(view) {

        private var tvName: TextView? = view.findViewById(R.id.tvName)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(accountAppArrayList[position], position)
                }
            }
        }

        fun onBindView(item: String) {
            tvName?.text = item
        }
    }
}
