package com.finvu.android_demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.finvu.android.FinvuManager
import com.finvu.android.publicInterface.ConsentDetail
import com.finvu.android.publicInterface.LinkedAccountDetails
import com.finvu.android_demo.adapters.LinkedAccountsSelectableAdapter
import com.finvu.android_demo.databinding.ActivityProcessConsentBinding
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ProcessConsentActivity : AppCompatActivity() {

    private var _binding: ActivityProcessConsentBinding? = null

    private val v get() = _binding!!

    private var linkedAccountsSelectableAdapter: LinkedAccountsSelectableAdapter? = null
    private var consentDetailList: HashMap<String, ConsentDetail> = HashMap()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProcessConsentBinding.inflate(layoutInflater)
        setContentView(v.root)
        v.btnAccept.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@ProcessConsentActivity)
            val gson = GsonBuilder().setPrettyPrinting().create()
            builder.setTitle("Do you want to split consents")
            builder.setPositiveButton("YES") { dialog, _ -> splitConsentFlow() }
            builder.setNegativeButton("NO") { dialog, _ -> multiConsentFlow() }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        v.btnReject.setOnClickListener {
            FinvuManager.shared.denyConsentRequest(consentDetailList[LoginActivity.consentHandleIds[0]]!!) { result ->
                if (result.isSuccess) {
                    runOnUiThread {
                        Toast.makeText(this, "Consent rejected", Toast.LENGTH_LONG).show()
                        startActivity(
                            Intent(
                                this@ProcessConsentActivity,
                                LoginActivity::class.java
                            )
                        )
                        finishAffinity()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Error rejecting consent", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun splitConsentFlow() {
        if (linkedAccountsSelectableAdapter!!.getSelectedItems().size != LoginActivity.consentHandleIds.size) {
            // Need to generate new ConsentHandeId for each account selected -1
            // Currently for demo app we pre-define them hence the block
            Toast.makeText(
                this@ProcessConsentActivity,
                "Number of consentHandleIds and selected accounts does not match",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {


            for ((index, account) in linkedAccountsSelectableAdapter!!.getSelectedItems()
                .withIndex()) {
                if (index == 0) {
                    FinvuManager.shared.approveConsentRequest(
                        consentDetailList[LoginActivity.consentHandleIds[0]]!!,
                        listOf<LinkedAccountDetails>(linkedAccountsSelectableAdapter!!.getSelectedItems()[0])
                    ) { result ->
                        if (result.isSuccess) {
                            runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "Parent Consent $index accepted",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            if (index == linkedAccountsSelectableAdapter!!.getSelectedItems().size - 1) {
                                startActivity(
                                    Intent(
                                        this@ProcessConsentActivity,
                                        LoginActivity::class.java
                                    )
                                )
                                finishAffinity()
                            }

                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Error accepting consent", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }

                } else {
                    try {
                        FinvuManager.shared.approveConsentRequest(
                            consentDetailList[LoginActivity.consentHandleIds[index]]!!,
                            listOf<LinkedAccountDetails>(linkedAccountsSelectableAdapter!!.getSelectedItems()[index])
                        ) { result ->
                            if (result.isSuccess) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Child Consent $index accepted",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                    if (index == linkedAccountsSelectableAdapter!!.getSelectedItems().size - 1) {
                                        startActivity(
                                            Intent(
                                                this@ProcessConsentActivity,
                                                LoginActivity::class.java
                                            )
                                        )
                                        finishAffinity()
                                    }

                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Error accepting consent",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                    } catch (e: Exception) {
                        // Not an active consent handleId
                        Toast.makeText(
                            this@ProcessConsentActivity,
                            "Consent Handle Id detail not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
    }

    fun multiConsentFlow() {
        FinvuManager.shared.approveConsentRequest(
            consentDetailList[LoginActivity.consentHandleIds[0]]!!,
            linkedAccountsSelectableAdapter!!.getSelectedItems()
        ) { result ->
            if (result.isSuccess) {
                runOnUiThread {
                    Toast.makeText(this, "Consent accepted", Toast.LENGTH_LONG).show()
                    startActivity(
                        Intent(
                            this@ProcessConsentActivity,
                            LoginActivity::class.java
                        )
                    )
                    finishAffinity()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Error accepting consent", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchLinkedAccounts()
        consentDetailList = HashMap()
        for (id in LoginActivity.consentHandleIds) {
            getConsentRequestDetailIndividual(id)
        }
    }

    private fun fetchLinkedAccounts() {
        FinvuManager.shared.fetchLinkedAccounts { result ->
            if (result.isSuccess) {
                val linkedAccounts = result.getOrNull()!!
                runOnUiThread {
                    v.rvLinkedAccounts.layoutManager = LinearLayoutManager(
                        this@ProcessConsentActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    linkedAccountsSelectableAdapter =
                        LinkedAccountsSelectableAdapter(linkedAccounts.linkedAccounts)
                    v.rvLinkedAccounts.adapter = linkedAccountsSelectableAdapter
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Error fetching linked accounts", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getConsentRequestDetailIndividual(handleId: String) {
        var returnObj: ConsentDetail
        FinvuManager.shared.getConsentRequestDetails(handleId) { result ->
            if (result.isFailure) {
                // Show generic error message via toast on ui thread
                runOnUiThread {
                    Toast.makeText(
                        this@ProcessConsentActivity,
                        "Error fetching consent details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


            try {
                val response = result.getOrNull()!!

                returnObj = ConsentDetail(
                    consentHandle = response.consentDetail.consentHandle,
                    financialInformationUser = response.consentDetail.financialInformationUser,
                    consentId = null,
                    statusLastUpdateTimestamp = null,
                    consentPurpose = response.consentDetail.consentPurpose,
                    consentDisplayDescriptions = response.consentDetail.consentDisplayDescriptions,
                    dataDateTimeRange = response.consentDetail.dataDateTimeRange,
                    consentDateTimeRange = response.consentDetail.consentDateTimeRange,
                    consentDataLife = response.consentDetail.consentDataLife,
                    consentDataFrequency = response.consentDetail.consentDataFrequency,
                    fiTypes = response.consentDetail.fiTypes
                )

                consentDetailList[handleId] = returnObj

                if (handleId == LoginActivity.consentHandleIds[0]) {
                    runOnUiThread {

                        v.txtConsentPurpose.text =
                            "Consent Purpose: \n${response.consentDetail.consentPurpose.code} -  ${response.consentDetail.consentPurpose.text}"
                        v.txtDataFetchFrequency.text =
                            "Data Fetch Frequency: \n${response.consentDetail.consentDataFrequency.value} ${response.consentDetail.consentDataFrequency.unit}"
                        v.txtDataUse.text =
                            "Data Use: \n${response.consentDetail.consentDataLife.value} ${response.consentDetail.consentDataLife.unit}"
                        v.txtDataFetchFrom.text =
                            "Data Fetch From: \n${getCurrentDateTimeInUTC(response.consentDetail.dataDateTimeRange.from)}"
                        v.txtDataFetchUntil.text =
                            "Data Fetch Until: \n${getCurrentDateTimeInUTC(response.consentDetail.dataDateTimeRange.to)}"
                        v.txtConsentRequestedOn.text =
                            "Consent Requested on: \n${getCurrentDateTimeInUTC(response.consentDetail.consentDateTimeRange.from)}"
                        v.txtConsentExpiresOn.text =
                            "Consent Expires on: \n${getCurrentDateTimeInUTC(response.consentDetail.consentDateTimeRange.to)}"
                        v.txtAccountInformation.text = "Account Information: \n${
                            response.consentDetail.consentDisplayDescriptions.joinToString(", ")
                        }"
                        v.txtAccountTypesRequested.text =
                            "Account Types: \n${response.consentDetail.fiTypes?.joinToString(", ")}"
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun getCurrentDateTimeInUTC(date: Date): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(date)
    }

}
