package com.example.qrisgen.ui.settings

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.qrisgen.data.repository.SettingsRepository
import com.example.qrisgen.databinding.ActivitySettingsBinding
import com.example.qrisgen.ui.scanner.CustomScannerActivity
import com.example.qrisgen.utils.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository(this))
    }

    private lateinit var payloadFromScan: ActivityResultLauncher<ScanOptions>
    private lateinit var pickImageFromGallery: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLaunchers()
        setupToolbar()
        setupFeeTypeSpinner()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupLaunchers() {
        payloadFromScan = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                binding.etQRISInput.text.clear()
                viewModel.parseQRIS(result.contents)
            }
        }

        pickImageFromGallery = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                loadImageAndDecode(it) }
        }
    }

    private fun loadImageAndDecode(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            binding.etQRISInput.text.clear()

            if (bitmap != null) {
                viewModel.decodeQRFromImage(bitmap)
            } else {
                showError("Failed to load image")
            }

        } catch (e: Exception) {
            showError("Error loading image: ${e.message}")
        }
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

    private fun setupClickListeners() {
        binding.btnScanQR.setOnClickListener {
            scanPayload()
        }

        binding.btnPickFromGallery.setOnClickListener {
            pickImageFromGallery.launch("image/*")
        }

        binding.btnParseQRIS.setOnClickListener {
            val input = binding.etQRISInput.text.toString().trim()
            viewModel.parseQRIS(input)
        }

        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                showError(it)
                viewModel.clearError()
            }
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is SettingsUiState.Empty -> {
                }

                is SettingsUiState.Loaded -> {
                    binding.etQRISInput.text.clear()
                    binding.etMerchantName.setText(state.merchantName)
                    binding.etMerchantCity.setText(state.merchantCity)
                    binding.etMerchantCategory.setText(state.merchantCategory ?: "")
                    binding.etPostalCode.setText(state.postalCode ?: "")

                    loadFeeTypeToUI(state.feeType, state.feeValue)
                    showEditCards()
                }

                is SettingsUiState.Parsed -> {
                    val parsed = state.parsedQRIS
                    binding.etMerchantName.setText(parsed.merchant.name)
                    binding.etMerchantCity.setText(parsed.merchant.city)
                    binding.etMerchantCategory.setText(parsed.merchant.category ?: "")
                    binding.etPostalCode.setText(parsed.merchant.postCode ?: "")

                    showEditCards()
                    Toast.makeText(this, "QRIS parsed successfully!", Toast.LENGTH_SHORT).show()
                }

                is SettingsUiState.Saved -> {
                    Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun scanPayload() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(CustomScannerActivity::class.java)
        }
        payloadFromScan.launch(options)
    }

    private fun saveSettings() {
        val merchantName = binding.etMerchantName.text.toString().trim()
        val merchantCity = binding.etMerchantCity.text.toString().trim()
        val merchantCategory = binding.etMerchantCategory.text.toString().trim()
        val postalCode = binding.etPostalCode.text.toString().trim()

        val feeTypeText = binding.spinnerFeeType.text.toString()
        val feeType = when (feeTypeText) {
            "No Fee" -> Constants.FeeType.NONE
            "Fixed Amount" -> Constants.FeeType.FIXED
            "Percentage" -> Constants.FeeType.PERCENTAGE
            else -> Constants.FeeType.NONE
        }

        val feeValue = if (feeType != Constants.FeeType.NONE) {
            binding.etFeeValue.text.toString().toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

        viewModel.saveSettings(
            merchantName = merchantName,
            merchantCity = merchantCity,
            merchantCategory = merchantCategory.ifEmpty { null },
            postalCode = postalCode.ifEmpty { null },
            feeType = feeType,
            feeValue = feeValue
        )
    }

    private fun loadFeeTypeToUI(feeType: String, feeValue: Double) {
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

    private fun showEditCards() {
        binding.cardMerchantInfo.visibility = View.VISIBLE
        binding.cardFeeSettings.visibility = View.VISIBLE
        binding.btnSave.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}