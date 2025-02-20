package com.finvu.android_demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.app.utils.getDisplayText
import com.finvu.android.publicInterface.DiscoveredAccount
import com.finvu.android_demo.R

data class SelectableItem<T>(
    val data: T,
    var isSelected: Boolean = false,
    var isDisabled: Boolean = false
)

class DiscoverAdapter(
    val discoveredAccountsAdapterList: List<SelectableItem<DiscoveredAccount>>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<DiscoverAdapter.DiscoverVH>() {
    interface OnItemClickListener {
        fun onItemClick(
            item: SelectableItem<DiscoveredAccount>,
            position: Int
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_discover, parent, false)
        return DiscoverVH(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: DiscoverVH, position: Int) {
        holder.onBindView(discoveredAccountsAdapterList[position], position)
    }

    override fun getItemCount(): Int {
        return discoveredAccountsAdapterList.size
    }

    inner class DiscoverVH(
        private val view: View,
        private val onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(view) {

        private var llAccountItem: LinearLayout? = view.findViewById(R.id.llAccountItem)
        private var txtTitle: TextView? = view.findViewById(R.id.txtTitle)
        private var txtMsg: TextView? = view.findViewById(R.id.txtMsg)
        private var imgSelected: ImageView? = view.findViewById(R.id.imgSelected)
        private var alreadyLinked: TextView? = view.findViewById(R.id.alreadyLinked)
        private var greenColor = ContextCompat.getColor(view.context, R.color.green_new)


        init {

            llAccountItem!!.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(
                        discoveredAccountsAdapterList[position],
                        position
                    )
                }

            }
        }

        fun onBindView(
            item: SelectableItem<DiscoveredAccount>,
            position: Int
        ) {
            txtTitle!!.text = "${getDisplayText(item.data.accountType, item.data.fiType)} Account"
            if (item.data.maskedAccountNumber.isNotEmpty()) {
                txtMsg!!.text = item.data.maskedAccountNumber
            }

            if (item.isDisabled) {
                alreadyLinked!!.visibility = View.VISIBLE
                imgSelected!!.visibility = View.GONE
            } else {
                alreadyLinked!!.visibility = View.GONE
                imgSelected!!.visibility = View.VISIBLE
            }


            if (item.isSelected) {
                imgSelected!!.setColorFilter(greenColor)
            } else {
                imgSelected!!.clearColorFilter()
            }

        }
    }
}
