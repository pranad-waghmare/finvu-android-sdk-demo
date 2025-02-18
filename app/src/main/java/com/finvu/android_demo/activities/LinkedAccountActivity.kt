package com.finvu.android_demo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.finvu.android_demo.adapters.LinkedAccountsAdapter
import com.finvu.android.FinvuManager
import com.finvu.android_demo.databinding.ActivityLinkedAccountBinding

class LinkedAccountActivity : AppCompatActivity() {
    private var _binding: ActivityLinkedAccountBinding? = null
    private val v get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLinkedAccountBinding.inflate(layoutInflater)
        setContentView(v.root)
        getLinkedAccounts()
    }

    private fun getLinkedAccounts() {
        FinvuManager.shared.fetchLinkedAccounts { result ->
            runOnUiThread {
                if (result.isSuccess) {
                    val list = result.getOrNull()?.linkedAccounts!!
                    v.rvLinkedAccounts.layoutManager = LinearLayoutManager(
                        this@LinkedAccountActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    v.rvLinkedAccounts.adapter = LinkedAccountsAdapter(list)

                }
            }
        }
    }
}
