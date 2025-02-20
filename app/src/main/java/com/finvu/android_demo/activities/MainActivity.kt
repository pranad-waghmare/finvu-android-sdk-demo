package com.finvu.android_demo.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.finvu.android.FinvuManager
import com.finvu.android_demo.DemoApplication
import com.finvu.android_demo.adapters.LinkedAccountsAdapter
import com.finvu.android_demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val v get() = _binding!!
    private val finvuManager = FinvuManager.shared

    override fun onResume() {
        super.onResume()
        getLinkedAccounts()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(v.root)

        val myApp = application as DemoApplication
        // in our app, we want all responses to be on main thread
        finvuManager.setCompletionCoroutineScope(myApp.applicationScope)

        v.goToProcessConsent.setOnClickListener {
            startActivity(Intent(this@MainActivity, ProcessConsentActivity::class.java))
        }

        v.goToAddAccount.setOnClickListener {
            startActivity(Intent(this@MainActivity, PopularSearchActivity::class.java))
        }


    }

    private fun getLinkedAccounts() {
        FinvuManager.shared.fetchLinkedAccounts { result ->
            runOnUiThread {
                if (result.isSuccess) {
                    val list = result.getOrNull()?.linkedAccounts!!
                    v.rvLinkedAccounts.layoutManager = LinearLayoutManager(
                        this@MainActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    v.rvLinkedAccounts.adapter = LinkedAccountsAdapter(list)
                }
            }
        }
    }

}
