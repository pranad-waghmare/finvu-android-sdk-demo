package com.finvu.android_demo.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.utils.GeneralUtils
import com.finvu.android.FinvuManager
import com.finvu.android.publicInterface.FinvuErrorCode
import com.finvu.android.publicInterface.FinvuException
import com.finvu.android.utils.FinvuConfig
import com.finvu.android_demo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val finvuManager = FinvuManager.shared
    private var loginError = false
    private var _binding: ActivityLoginBinding? = null
    var otpReference: String? = null

    private val baseUrl = "wss://webvwdev.finvu.in/consentapi"
    private val finvuClientConfig =
        FinvuClientConfig(finvuEndpoint = baseUrl, certificatePins = listOf())

    data class FinvuClientConfig(
        override val finvuEndpoint: String,
        override val certificatePins: List<String>?
    ) : FinvuConfig

    private val v get() = _binding!!

    companion object {
        var mobileNumber = ""

        // Pre-Filled Consent handle IDs for demo app, in PROD make sure new handle IDs are created for each account selected for split consent
        var consentHandleIds = mutableListOf(
            "c518e2a3-d738-4241-a744-49e23832a643",
            "62584175-37e4-462f-a4c9-3b95247b80c3",
            "ef4c9d2d-90a5-410c-a212-86c10846cf13"
        )
        var username = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(v.root)
        v.etConsentHandleId.setText(consentHandleIds[0])
        finvuManager.initializeWith(finvuClientConfig)
        finvuManager.connect { result ->
            runOnUiThread {
                if (result.isSuccess) {
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                } else {
                    when ((result.exceptionOrNull() as FinvuException).code) {
                        FinvuErrorCode.SSL_PINNING_FAILURE_ERROR.code -> {
                            Toast.makeText(this, "Your connection is insecure", Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> {
                            Toast.makeText(
                                this,
                                "Something went wrong ${(result.exceptionOrNull() as FinvuException).code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        v.btnLogin.setOnClickListener {
            if (v.etUsername.text.isNotEmpty() && v.etMobileNumber.text.isNotEmpty() && v.etConsentHandleId.text.isNotEmpty()) {
                finvuManager.loginWithUsernameOrMobileNumber(
                    username = v.etUsername.text.toString(),
                    mobileNumber = v.etMobileNumber.text.toString(),
                    consentHandleId = v.etConsentHandleId.text.toString(),
                    completion = { result ->
                        runOnUiThread {
                            if (result.isSuccess) {
                                loginError = false
                                v.etOtp.visibility = View.VISIBLE
                                v.btnVerify.visibility = View.VISIBLE
                                otpReference = result.getOrNull()?.reference
                            } else {
                                loginError = true
                                GeneralUtils(this@LoginActivity).showDialog(result.exceptionOrNull()?.message.toString())
                            }
                        }
                    })
            } else {
                GeneralUtils(this@LoginActivity).showDialog("Please enter username and passcode")
            }

        }

        v.btnVerify.setOnClickListener {
            if (otpReference != null) {
                if (v.etOtp.text.isNotEmpty()) {
                    finvuManager.verifyLoginOtp(
                        otp = v.etOtp.text.toString(),
                        otpReference = otpReference!!,
                        completion = { result ->
                            if (result.isSuccess) {
                                mobileNumber = v.etMobileNumber.text.toString()
                                consentHandleIds[0] = v.etConsentHandleId.text.toString()
                                username = v.etUsername.text.toString()
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        MainActivity::class.java
                                    )
                                )
                            }
                        })
                } else {
                    GeneralUtils(this@LoginActivity).showDialog("Please enter otp")
                }

            }

        }

    }

}
