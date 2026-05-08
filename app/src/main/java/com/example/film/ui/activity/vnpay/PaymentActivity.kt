package com.example.film.ui.activity.vnpay

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.film.databinding.ActivityPaymentBinding
import com.example.moneymanagement.presentation.view.base.BaseActivity

class PaymentActivity : BaseActivity<ActivityPaymentBinding>(ActivityPaymentBinding::inflate) {

    override fun initializeComponent() {
        super.initializeComponent()

        val paymentUrl = intent.getStringExtra("payment_url") ?: ""
        if (paymentUrl.isEmpty()) {
            finish()
            return
        }

        setupWebView()
        binding.webView.loadUrl(paymentUrl)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE


                url?.let {
                    if (it.contains("vnp_ResponseCode")) {
                        handlePaymentResult(it)
                    }
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.contains("vnp_ResponseCode")) {
                    handlePaymentResult(url)
                    return true
                }
                return false
            }
        }
    }

    private fun handlePaymentResult(url: String) {

        if (url.contains("/api/payment/return") && url.contains("vnp_ResponseCode=00")) {
            setResult(RESULT_OK)
            finish()
        } else if (url.contains("/api/payment/return")) {

            setResult(RESULT_CANCELED)
            finish()
        }
    }
}