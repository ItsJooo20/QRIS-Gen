package com.example.qrisgen.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrisgen.databinding.ActivitySettingsBinding
import com.example.qrisgen.utils.Constants
import com.example.qrisgen.utils.PreferencesManager
import com.example.qrisgen.utils.QRISParser
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var parsedPayload: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupFeeTypeSpinner()
        loadExistingSettings()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupFeeTypeSpinner() {
        val feeTypes = arrayOf("No Fee", "Fixed Amount", "Percentage")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, feeTypes)
        binding.spinnerFeeType.setAdapter(adapter)
        binding.spinnerFeeType.setText(feeTypes[0], false)

        binding.spinnerFeeType.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> binding.tilFeeValue.visibility = View.GONE
                1 -> {
                    binding.tilFeeValue.visibility = View.VISIBLE
                    binding.tilFeeValue.hint = "Fee Amount (IDR)"
                }
                2 -> {
                    binding.tilFeeValue.visibility = View.VISIBLE
                    binding.tilFeeValue.hint = "Fee Percentage (%)"
                }
            }
        }
    }

    private fun loadExistingSettings() {
        val savedPayload = PreferencesManager.getQRISPayload(this)

        if (savedPayload != null) {
            println("Found existing QRIS in settings")

            binding.etQRISInput.setText(savedPayload)
            parsedPayload = savedPayload

            loadMerchantInfo()
            loadFeeSettings()
            showEditCards()
        } else {
            println("ℹNo existing QRIS found")
        }
    }

    private fun loadMerchantInfo() {
        val merchantName = PreferencesManager.getMerchantName(this)
        val merchantCity = PreferencesManager.getMerchantCity(this)
        val merchantCategory = PreferencesManager.getMerchantCategory(this)
        val postCode = PreferencesManager.getPostalCode(this)

        binding.etMerchantName.setText(merchantName)
        binding.etMerchantCity.setText(merchantCity)
        binding.etMerchantCategory.setText(merchantCategory ?: "")
        binding.etPostalCode.setText(postCode ?: "")
    }

    private fun loadFeeSettings() {
        val feeType = PreferencesManager.getFeeType(this)
        val feeValue = PreferencesManager.getFeeValue(this)

        val feeTypes = arrayOf("No Fee", "Fixed Amount", "Percentage")
        when (feeType) {
            Constants.FeeType.NONE -> {
                binding.spinnerFeeType.setText(feeTypes[0], false)
                binding.tilFeeValue.visibility = View.GONE
            }
            Constants.FeeType.FIXED -> {
                binding.spinnerFeeType.setText(feeTypes[1], false)
                binding.tilFeeValue.visibility = View.VISIBLE
                binding.tilFeeValue.hint = "Fee Amount (IDR)"
                binding.etFeeValue.setText(feeValue.toLong().toString())
            }
            Constants.FeeType.PERCENTAGE -> {
                binding.spinnerFeeType.setText(feeTypes[2], false)
                binding.tilFeeValue.visibility = View.VISIBLE
                binding.tilFeeValue.hint = "Fee Percentage (%)"
                binding.etFeeValue.setText(feeValue.toString())
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnScanQR.setOnClickListener {
            Toast.makeText(this, "QR Scanner coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnPickFromGallery.setOnClickListener {
            Toast.makeText(this, "Gallery picker coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnParseQRIS.setOnClickListener {
            parseQRISInput()
        }

        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun parseQRISInput() {
        val input = binding.etQRISInput.text.toString().trim()

        if (input.isBlank()) {
            showError("Please paste or scan QRIS first")
            return
        }

        if (input.length < 50) {
            showError("Invalid QRIS format (too short)")
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        try {
            println("📝 Parsing QRIS...")

            val parsed = QRISParser.parseQRIS(input)

            if (parsed.isValid || parsed.merchant.name.isNotEmpty()) {
                parsedPayload = input

                binding.etMerchantName.setText(parsed.merchant.name)
                binding.etMerchantCity.setText(parsed.merchant.city)
                binding.etMerchantCategory.setText(parsed.merchant.category ?: "")
                binding.etPostalCode.setText(parsed.merchant.postCode ?: "")

                println("QRIS parsed successfully")
                println("Merchant: ${parsed.merchant.name}")
                println("City: ${parsed.merchant.city}")
                println("PostCode: ${parsed.merchant.postCode}")

                showEditCards()

                Toast.makeText(this, "QRIS parsed successfully!", Toast.LENGTH_SHORT).show()

            } else {
                showError("Failed to parse QRIS: ${parsed.errorMessage ?: "Unknown error"}")
            }

        } catch (e: Exception) {
            println("Parse error: ${e.message}")
            e.printStackTrace()
            showError("Error parsing QRIS: ${e.message}")
        } finally {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showEditCards() {
        binding.cardMerchantInfo.visibility = View.VISIBLE
        binding.cardFeeSettings.visibility = View.VISIBLE
        binding.btnSave.visibility = View.VISIBLE
    }

    private fun saveSettings() {
        val payload = parsedPayload
        if (payload.isNullOrBlank()) {
            showError("Please parse QRIS first")
            return
        }

        val merchantName = binding.etMerchantName.text.toString().trim()
        val merchantCity = binding.etMerchantCity.text.toString().trim()
        val merchantCategory = binding.etMerchantCategory.text.toString().trim()
        val postCode = binding.etPostalCode.text.toString().trim()

        if (merchantName.isEmpty()) {
            binding.etMerchantName.error = "Merchant name is required"
            binding.etMerchantName.requestFocus()
            return
        }

        if (merchantCity.isEmpty()) {
            binding.etMerchantCity.error = "City is required"
            binding.etMerchantCity.requestFocus()
            return
        }

        val feeTypeText = binding.spinnerFeeType.text.toString()
        val feeType = when (feeTypeText) {
            "No Fee" -> Constants.FeeType.NONE
            "Fixed Amount" -> Constants.FeeType.FIXED
            "Percentage" -> Constants.FeeType.PERCENTAGE
            else -> Constants.FeeType.NONE
        }

        val feeValue = if (feeType != Constants.FeeType.NONE) {
            val value = binding.etFeeValue.text.toString().trim()
            if (value.isEmpty()) {
                binding.etFeeValue.error = "Fee value is required"
                binding.etFeeValue.requestFocus()
                return
            }
            val feeDouble = value.toDoubleOrNull()
            if (feeDouble == null || feeDouble <= 0) {
                binding.etFeeValue.error = "Invalid fee value"
                binding.etFeeValue.requestFocus()
                return
            }
            feeDouble
        } else {
            0.0
        }

        PreferencesManager.saveQRISPayload(this, payload)
        PreferencesManager.saveMerchantInfo(
            context = this,
            name = merchantName,
            city = merchantCity,
            category = merchantCategory.ifEmpty { null },
            postalCode = postCode.ifEmpty { null },
        )
        PreferencesManager.saveFeeSettings(this, feeType, feeValue)

        println("All settings saved!")

        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()

        finish()
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}