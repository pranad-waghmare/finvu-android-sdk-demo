package com.finvu.android_demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finvu.android.publicInterface.LinkedAccountDetails
import com.finvu.android_demo.R

// write an adapter for the linked accounts recycler view, allows selection of linked accounts

class LinkedAccountsSelectableAdapter(val linkedAccounts: List<LinkedAccountDetails>): RecyclerView.Adapter<LinkedAccountsSelectableAdapter.LinkedAccountsViewHolder>() {
    private val selectedItems = mutableListOf<LinkedAccountDetails>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkedAccountsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_linked_account_selectable, parent, false)
        return LinkedAccountsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinkedAccountsViewHolder, position: Int) {
        holder.onBindView(linkedAccounts[position])
    }

    override fun getItemCount(): Int {
        return linkedAccounts.size
    }

    inner class LinkedAccountsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var tvAccountMaskedNumber: TextView? = view.findViewById(R.id.tvAccountMaskedNumber)
        private var tvFipName: TextView? = view.findViewById(R.id.tvFipName)
        private var cbAccountSelected: CheckBox? = view.findViewById(R.id.cbAccountSelected)

        fun onBindView(item: LinkedAccountDetails) {
            tvAccountMaskedNumber?.text = item.accountReferenceNumber
            tvFipName?.text = item.fipName
            cbAccountSelected?.isChecked = selectedItems.contains(item)
            cbAccountSelected?.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (selectedItems.contains(item)) {
                        selectedItems.remove(item)
                    } else {
                        selectedItems.add(item)
                    }
                    notifyItemChanged(position)
                }
            }
        }
    }

    fun getSelectedItems(): List<LinkedAccountDetails> {
        return selectedItems
    }
}
