package com.finvu.android_demo.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.finvu.android_demo.adapters.PopularSearchAdapter
import com.example.app.utils.GeneralUtils
import com.finvu.android.FinvuManager
import com.finvu.android.publicInterface.FIPInfo
import com.finvu.android_demo.databinding.ActivityPopularSearchBinding

class PopularSearchActivity : AppCompatActivity() {
    private var _binding: ActivityPopularSearchBinding? = null
    private val v get() = _binding!!
    private val finvuManager = FinvuManager.shared


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPopularSearchBinding.inflate(layoutInflater)
        setContentView(v.root)
        getPopularSearch()
    }

    private fun getPopularSearch() {
        finvuManager.fipsAllFIPOptions { result ->
            runOnUiThread {
                if (result.isSuccess) {
                    val list = (result.getOrNull()?.searchOptions)
                    v.rvPopularSearch.layoutManager = LinearLayoutManager(
                        this@PopularSearchActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    if (list != null) {
                        v.rvPopularSearch.adapter =
                            PopularSearchAdapter(list, object :
                                PopularSearchAdapter.OnItemClickListener {
                                override fun onItemClick(
                                    item: FIPInfo,
                                    position: Int
                                ) {
                                    getFipDetails(item)
                                }
                            })
                    }
                } else {
                    GeneralUtils(this@PopularSearchActivity).showDialog("Failed")
                }
            }
        }
    }

    private fun getFipDetails(fipInfo: FIPInfo) {
        startActivity(
            Intent(this@PopularSearchActivity, DiscoverActivity::class.java)
                .putExtra("fipId", fipInfo.fipId)
        )
    }
}
