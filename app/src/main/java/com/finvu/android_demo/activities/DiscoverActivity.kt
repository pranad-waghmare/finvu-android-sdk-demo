package com.finvu.android_demo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.finvu.android_demo.adapters.DiscoverAdapter
import com.finvu.android_demo.adapters.SelectableItem
import com.example.app.utils.GeneralUtils
import com.finvu.android.FinvuManager
import com.finvu.android.publicInterface.DiscoveredAccount
import com.finvu.android.publicInterface.FipDetails
import com.finvu.android.publicInterface.FinvuException
import com.finvu.android.publicInterface.TypeIdentifierInfo
import com.finvu.android_demo.databinding.ActivityDiscoverBinding


class DiscoverActivity : AppCompatActivity() {

    private val finvuManager = FinvuManager.shared
    private var fipId: String? = null
    private var fipDetails: FipDetails? = null
    private lateinit var adapter: DiscoverAdapter
    private var _binding: ActivityDiscoverBinding? = null
    private val v get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDiscoverBinding.inflate(layoutInflater)
        setContentView(v.root)

        v.tilMobileTxt.setText(LoginActivity.mobileNumber)


        v.btnContinue.setOnClickListener {
            list.clear()
            accountDiscoverApi(fipId!!)
        }


        v.btnLink.setOnClickListener {
            if (adapter.discoveredAccountsAdapterList.any { it.isSelected }) {
                // Proceed with the account linking API
                accountLinkingApi()
            } else {
                // Show a Toast message when no items are selected
                Toast.makeText(
                    this@DiscoverActivity,
                    "Please select an account",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (intent.extras != null) {
            fipId = intent.getSerializableExtra("fipId") as String
        }
    }

    override fun onResume() {
        super.onResume()
        fetchEntityDetails()
    }

    private fun fetchEntityDetails() {
        finvuManager.getEntityInfo(fipId!!, "FIP") { result ->
            runOnUiThread {
                if (result.isSuccess) {
                    val entityDetails = result.getOrNull()
                    if (entityDetails != null) {
                        v.tvFipInfo.text = entityDetails.entityName
                        entityDetails.entityIconUri?.let {
                            val circularProgressDrawable = CircularProgressDrawable(this)
                            circularProgressDrawable.strokeWidth = 10f
                            circularProgressDrawable.centerRadius = 50f
                            circularProgressDrawable.start()

                            Glide.with(this).load(it).placeholder(circularProgressDrawable)
                                .into(v.ivFipLogo)
                        }
                    }
                } else {
                    val exception = result.exceptionOrNull() as? FinvuException
                    Log.e("FinvuError", "❌ DiscoverActivity - Get entity info failed - Code: ${exception?.code}, Message: ${exception?.message}")
                }
            }
        }
    }

    var list: MutableList<SelectableItem<DiscoveredAccount>> = mutableListOf()

    private fun accountDiscoverApi(fipId: String) {
        val identifiers = mutableListOf(
            TypeIdentifierInfo(
                "STRONG",
                "MOBILE",
                v.tilMobileTxt.text.toString()
            )
        )

        val panText = v.tilPanTxt.text.toString()
        if (panText.isNotEmpty()) {
            identifiers.add(
                TypeIdentifierInfo(
                    "WEAK",
                    "PAN",
                    panText
                )
            )
        }

        // Fetch FIP details
        finvuManager.fetchFipDetails(fipId) { result ->
            if (result.isFailure) {
                val exception = result.exceptionOrNull() as? FinvuException
                Log.e("FinvuError", "❌ DiscoverActivity - Fetch FIP details failed - Code: ${exception?.code}, Message: ${exception?.message}")
                return@fetchFipDetails
            }

            val response = result.getOrThrow()
            fipDetails = response

            // Fetch the fiTypes from fipDetails
            val fiTypes =
                fipDetails!!.typeIdentifiers.map { fipFiTypeIdentifier -> fipFiTypeIdentifier.fiType }

            // Fetch discovered accounts
            runOnUiThread {
                finvuManager.discoverAccounts(fipDetails!!.fipId, fiTypes, identifiers) { discoveryResult ->
                    runOnUiThread {
                        if (discoveryResult.isSuccess) {
                            val discoveredAccounts = discoveryResult.getOrNull()?.discoveredAccounts
                            if (discoveredAccounts != null) {
                                // Fetch linked accounts
                                finvuManager.fetchLinkedAccounts { linkedAccountsResult ->
                                    if (linkedAccountsResult.isSuccess) {
                                        val linkedAccounts = linkedAccountsResult.getOrNull()

                                        // Generate account identifiers for the linked accounts
                                        val linkedAccountIdentifiers =
                                            linkedAccounts!!.linkedAccounts.map { account ->
                                                generateAccountIdentifier(
                                                    fiType = account.fiType,
                                                    maskedAccountNumber = account.maskedAccountNumber,
                                                    accountType = account.accountType
                                                )
                                            }.toSet()

                                        // Add discovered accounts and mark linked accounts as non-selectable
                                        list.clear() // Clear the previous list (optional)
                                        list.addAll(discoveredAccounts.map { account ->
                                            // Mark linked accounts as non-selectable
                                            val accountIdentifier = generateAccountIdentifier(
                                                fiType = account.fiType,
                                                maskedAccountNumber = account.maskedAccountNumber,
                                                accountType = account.accountType
                                            )
                                            val isLinked =
                                                linkedAccountIdentifiers.contains(accountIdentifier)
                                            SelectableItem(account, isDisabled = isLinked)
                                        })

                                        // Set up the adapter with non-selectable linked accounts
                                        adapter = DiscoverAdapter(
                                            list,
                                            object : DiscoverAdapter.OnItemClickListener {
                                                override fun onItemClick(
                                                    item: SelectableItem<DiscoveredAccount>,
                                                    position: Int
                                                ) {
                                                    // Only toggle selection if the item is selectable
                                                    if (!item.isDisabled) {
                                                        adapter.apply {
                                                            discoveredAccountsAdapterList[position].isSelected =
                                                                !discoveredAccountsAdapterList[position].isSelected
                                                            notifyItemChanged(position)
                                                        }
                                                    }
                                                }
                                            })

                                        // Set up the RecyclerView with the adapter
                                        runOnUiThread {

                                            v.rvDiscoveredAccounts.layoutManager =
                                                LinearLayoutManager(this@DiscoverActivity)
                                            v.rvDiscoveredAccounts.adapter = adapter
                                        }
                                    } else {
                                        val exception = linkedAccountsResult.exceptionOrNull() as? FinvuException
                                        Log.e("FinvuError", "❌ DiscoverActivity - Fetch linked accounts failed - Code: ${exception?.code}, Message: ${exception?.message}")
                                        GeneralUtils(this@DiscoverActivity).showDialog("Failed to fetch linked accounts")
                                    }
                                }
                            }
                        } else {
                            val exception = discoveryResult.exceptionOrNull() as? FinvuException
                            Log.e("FinvuError", "❌ DiscoverActivity - Discover accounts failed - Code: ${exception?.code}, Message: ${exception?.message}")
                            GeneralUtils(this@DiscoverActivity).showDialog(
                                discoveryResult.exceptionOrNull()?.message
                                    ?: "Failed to discover accounts"
                            )
                        }
                    }
                }
            }
        }
    }

    fun generateAccountIdentifier(
        fiType: String,
        maskedAccountNumber: String,
        accountType: String
    ): String {
        return "${fiType}_${maskedAccountNumber.takeLast(4)}_${accountType}"
    }

    private fun accountLinkingApi() {
        val selectedAccounts = adapter.discoveredAccountsAdapterList
            .filter { it.isSelected }
            .map { it.data }

        finvuManager.linkAccounts(
            selectedAccounts,
            fipDetails!!,
        ) { result ->
            runOnUiThread {
                if (result.isSuccess) {
                    val referenceNumber = result.getOrNull()?.referenceNumber
                    startActivity(
                        Intent(
                            this@DiscoverActivity,
                            AccountLinkingConfirmActivity::class.java
                        ).putExtra("referenceNumber", referenceNumber)
                    )

                } else {
                    val exception = result.exceptionOrNull() as? FinvuException
                    Log.e("FinvuError", "❌ DiscoverActivity - Link accounts failed - Code: ${exception?.code}, Message: ${exception?.message}")
                    GeneralUtils(this@DiscoverActivity).showDialog(
                        result.exceptionOrNull()?.message ?: "Failed"
                    )
                }
            }
        }
    }
}
