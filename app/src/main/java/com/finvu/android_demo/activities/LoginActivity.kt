package com.finvu.android_demo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.utils.GeneralUtils
import com.finvu.android.FinvuManager
import com.finvu.android.publicInterface.FinvuErrorCode
import com.finvu.android.publicInterface.FinvuException
import com.finvu.android.types.FinvuEnvironment
import com.finvu.android.utils.FinvuConfig
import com.finvu.android.utils.FinvuSNAAuthConfig
import com.finvu.android_demo.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope

class LoginActivity : AppCompatActivity() {

    private val finvuManager = FinvuManager.shared
    private var loginError = false
    private var _binding: ActivityLoginBinding? = null
    var otpReference: String? = null

    private val baseUrl = "wss://webvwdev.finvu.in/consentapiv2"
    private val finvuClientConfig = FinvuClientConfig(
        finvuEndpoint = baseUrl, certificatePins = listOf(
        ), finvuSNAAuthConfig = FinvuSNAAuthClientConfig(
            this, lifecycleScope, FinvuEnvironment.UAT
        )
    )

    data class FinvuSNAAuthClientConfig(
        override val activity: Activity,
        override var scope: CoroutineScope,
        override val env: FinvuEnvironment
    ) : FinvuSNAAuthConfig

    data class FinvuClientConfig(
        override val finvuEndpoint: String,
        override val certificatePins: List<String>?,
        override val finvuSNAAuthConfig: FinvuSNAAuthConfig?
    ) : FinvuConfig

    private val v get() = _binding!!

    companion object {
        var mobileNumber = ""
        var consentHandleIds = mutableListOf(
            ""
        )
        var username = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(v.root)
        v.etConsentHandleId.setText(consentHandleIds[0])
        finvuManager.initializeWith(finvuClientConfig)

        // connect Finvu manager
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

        // login button
        v.btnLogin.setOnClickListener {
            triggerLoginFlow()
        }

        // manual OTP verify button
        v.btnVerify.setOnClickListener {
            if (otpReference != null) {
                if (v.etOtp.text.isNotEmpty()) {
                    performOtpVerification(v.etOtp.text.toString(), otpReference!!)
                } else {
                    GeneralUtils(this@LoginActivity).showDialog("Please enter otp")
                }
            } else {
                GeneralUtils(this@LoginActivity).showDialog("No OTP reference available. Please login again.")
            }
        }
    }

    /**
     * Trigger login — primary call that handles SNA and, if snaToken == null,
     * does a single secondary login call to obtain OTP reference for manual flow.
     */
    private fun triggerLoginFlow() {
        if (v.etUsername.text.isNotEmpty() && v.etMobileNumber.text.isNotEmpty() && v.etConsentHandleId.text.isNotEmpty()) {

            // First login attempt (may return snaToken)
            finvuManager.loginWithUsernameOrMobileNumber(username = v.etUsername.text.toString(),
                mobileNumber = v.etMobileNumber.text.toString(),
                consentHandleId = v.etConsentHandleId.text.toString(),
                completion = { result ->
                    runOnUiThread {
                        if (result.isSuccess) {
                            val loginResult = result.getOrNull()
                            loginError = false
                            otpReference = loginResult?.reference

                            val snaToken = loginResult?.snaToken
                            if (!snaToken.isNullOrEmpty() && otpReference != null) {
                                // SNA succeeded -> auto-verify with snaToken
                                Log.d("LoginActivity", "✅ SNA Token: $snaToken")
                                Log.d("LoginActivity", "Login OTP Reference: $otpReference")

                                GeneralUtils(this@LoginActivity).showDialog(
                                    "SNA Authentication Successful!\n\nUsing SNA Token to verify login."
                                )

                                v.etOtp.visibility = View.GONE
                                v.btnVerify.visibility = View.GONE

                                // Auto-verify with snaToken
                                performOtpVerification(snaToken, otpReference!!, isSnaFlow = true)
                            } else {
                                // SNA token is null => log and perform a single additional login to get OTP reference
                                Log.w(
                                    "LoginActivity",
                                    "⚠️ SNA failed or no token received."
                                )

                                GeneralUtils(this@LoginActivity).showDialog("SNA not available. enter otp...")
                                v.etOtp.visibility = View.VISIBLE
                                v.btnVerify.visibility = View.VISIBLE
                            }
                        } else {
                            loginError = true
                            GeneralUtils(this@LoginActivity).showDialog(
                                result.exceptionOrNull()?.message.toString()
                            )
                        }
                    }
                })
        } else {
            GeneralUtils(this@LoginActivity).showDialog("Please enter username and passcode")
        }
    }

    /**
     * Common function to verify OTP or SNA token
     * @param isSnaFlow if true, after SNA-based verify failure we could decide to re-initiate login; kept for compatibility
     */
    private fun performOtpVerification(
        otp: String, otpReference: String, isSnaFlow: Boolean = false
    ) {
        finvuManager.verifyLoginOtp(otp = otp, otpReference = otpReference, completion = { result ->
            runOnUiThread {
                if (result.isSuccess) {
                    mobileNumber = v.etMobileNumber.text.toString()
                    consentHandleIds[0] = v.etConsentHandleId.text.toString()
                    username = v.etUsername.text.toString()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    Log.e(
                        "LoginActivity",
                        "❌ OTP/SNA verify failed: ${result.exceptionOrNull()?.message}"
                    )
                    GeneralUtils(this@LoginActivity).showDialog(
                        "Verification failed: ${result.exceptionOrNull()?.message}"
                    )

                    // If SNA-based verification failed, let user proceed with manual OTP flow:
                    if (isSnaFlow) {
                        Log.w(
                            "LoginActivity",
                            "SNA verify failed. Please use manual OTP verification."
                        )
                        // Show OTP UI so user can manually enter OTP if available
                        v.etOtp.visibility = View.VISIBLE
                        v.btnVerify.visibility = View.VISIBLE
                        // Do not automatically loop — user will trigger login again or use the shown OTP UI
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
