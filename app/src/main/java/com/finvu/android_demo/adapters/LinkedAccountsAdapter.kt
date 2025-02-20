package com.finvu.android_demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finvu.android.publicInterface.LinkedAccountDetails
import com.finvu.android_demo.R

class LinkedAccountsAdapter(
    private val linkedAccounts: List<LinkedAccountDetails>,
) : RecyclerView.Adapter<LinkedAccountsAdapter.LinkedAccountsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkedAccountsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_linked_account, parent, false)
        return LinkedAccountsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinkedAccountsViewHolder, position: Int) {
        holder.onBindView(linkedAccounts[position])
    }

    override fun getItemCount(): Int {
        return linkedAccounts.size
    }

    inner class LinkedAccountsViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private var tvName: TextView? = view.findViewById(R.id.tvName)

        fun onBindView(item: LinkedAccountDetails) {
            tvName?.text = "${item.fipName} - ${item.accountReferenceNumber}"
        }
    }
}
