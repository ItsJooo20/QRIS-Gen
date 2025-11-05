package com.example.qrisgen.ui.scanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qrisgen.R
import com.example.qrisgen.databinding.ActivityCustomScannerBinding
import com.journeyapps.barcodescanner.CaptureManager

class CustomScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomScannerBinding
    private lateinit var capture: CaptureManager
    private var isFlashOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup view binding
        binding = ActivityCustomScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ZXing scanner
        capture = CaptureManager(this, binding.barcodeScanner)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        // Setup buttons
        setupButtons()
    }

    private fun setupButtons() {
        // Close button - finish activity
        binding.btnClose.setOnClickListener {
            finish()
        }

        // Flash button - toggle flashlight
        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }
    }

    private fun toggleFlash() {
        if (isFlashOn) {
            // Turn OFF flash
            binding.barcodeScanner.setTorchOff()
            binding.btnFlash.setImageResource(R.drawable.ic_flash_on)
            isFlashOn = false
        } else {
            // Turn ON flash
            binding.barcodeScanner.setTorchOn()
            binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
            isFlashOn = true
        }
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }
}