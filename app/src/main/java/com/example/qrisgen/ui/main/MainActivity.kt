package com.example.qrisgen.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.qrisgen.data.local.database.AppDatabase
import com.example.qrisgen.data.repository.QRISRepository
import com.example.qrisgen.databinding.ActivityMainBinding
import com.example.qrisgen.ui.history.HistoryActivity
import com.example.qrisgen.ui.settings.SettingsActivity
import com.example.qrisgen.utils.PreferencesManager
import com.example.qrisgen.utils.QRCodeHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = QRISRepository(database.qrisDao())
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkQRISConfiguration()
        setupObservers()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        checkQRISConfiguration()
    }

    private fun checkQRISConfiguration() {
        val isConfigured = PreferencesManager.isQRISConfigured(this)

        if (isConfigured) {
            showCalculatorScreen()
        } else {
            showSetupRequiredScreen()
        }
    }

    private fun showSetupRequiredScreen() {
        binding.layoutSetupRequired.visibility = View.VISIBLE
        binding.layoutCalculator.visibility = View.GONE

        println("QRIS not configured - showing setup screen")
    }

    private fun showCalculatorScreen() {
        binding.layoutSetupRequired.visibility = View.GONE
        binding.layoutCalculator.visibility = View.VISIBLE

        val merchantName = PreferencesManager.getMerchantName(this)
        val merchantCity = PreferencesManager.getMerchantCity(this)
        val feeType = PreferencesManager.getFeeType(this)
        val feeValue = PreferencesManager.getFeeValue(this)
        val postCode = PreferencesManager.getPostalCode(this)

        println("QRIS configured - showing calculator")
        println("Merchant: $merchantName")
        println("City: $merchantCity")
        println("Fee: $feeType = $feeValue")

        binding.tvMerchantName.text = merchantName
        binding.tvMerchantCity.text = merchantCity

        if (feeType != "none" && feeValue > 0) {
            binding.tvFeeInfo.visibility = View.VISIBLE
            binding.tvFeeInfo.text = when (feeType) {
                "fixed" -> "Service Fee: ${formatCurrency(feeValue.toLong())}"
                "percentage" -> "Service Fee: $feeValue%"
                else -> ""
            }
        } else {
            binding.tvFeeInfo.visibility = View.GONE
        }

        setupAmountWatcher()
    }

    private fun setupAmountWatcher() {
        binding.etAmount.doAfterTextChanged { text ->
            val amount = text.toString().toLongOrNull() ?: 0L

            if (amount > 0) {
                val feeType = PreferencesManager.getFeeType(this)
                val feeValue = PreferencesManager.getFeeValue(this)

                val finalAmount = when (feeType) {
                    "fixed" -> amount + feeValue.toLong()
                    "percentage" -> amount + (amount * feeValue / 100).toLong()
                    else -> amount
                }

                binding.layoutFinalAmount.visibility = View.VISIBLE
                binding.tvFinalAmount.text = formatCurrency(finalAmount)

                viewModel.setAmount(amount)
                viewModel.setFeeType(feeType)
                viewModel.setFeeValue(feeValue)
            } else {
                binding.layoutFinalAmount.visibility = View.GONE
            }
        }
    }

    private fun setupObservers() {
        viewModel.generatedPayload.observe(this) { payload ->
            if (payload != null) {
//                binding.cardGeneratedQR.visibility = View.VISIBLE
                showQRResultDialog(payload)

                binding.layoutCalculator.post {
                    binding.layoutCalculator.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGenerate.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                showErrorDialog(error)
                viewModel.clearErrorMessage()
            }
        }

        viewModel.successMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnGoToSetup.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.btnAmount10k.setOnClickListener {
            binding.etAmount.setText("10000")
        }

        binding.btnAmount50k.setOnClickListener {
            binding.etAmount.setText("50000")
        }

        binding.btnAmount100k.setOnClickListener {
            binding.etAmount.setText("100000")
        }

        binding.btnGenerate.setOnClickListener {
            generateQRIS()
        }
    }

    private fun generateQRIS() {
        val amount = binding.etAmount.text.toString().toLongOrNull()

        if (amount == null || amount <= 0) {
            showErrorDialog("Please enter a valid amount")
            return
        }

        val originalPayload = PreferencesManager.getQRISPayload(this)
        if (originalPayload.isNullOrBlank()) {
            showErrorDialog("QRIS payload not found. Please setup again.")
            return
        }

        val customMerchantName = PreferencesManager.getMerchantName(this)
        val customMerchantCity = PreferencesManager.getMerchantCity(this)
        val customMerchantPostCode = PreferencesManager.getMerchantPostalCode(this)

        viewModel.parseInputQRIS(originalPayload)

        binding.layoutCalculator.postDelayed({
            viewModel.generateQRIS(
                customMerchantName = customMerchantName,
                customMerchantCity = customMerchantCity,
                customMerchantPostCode = customMerchantPostCode
            )
        }, 300)
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun formatCurrency(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }

    private fun showQRResultDialog(payload: String) {
        val amount = viewModel.amount.value ?: 0L
        val merchantName = PreferencesManager.getMerchantName(this)
        val merchantCity = PreferencesManager.getMerchantCity(this)

        val feeType = PreferencesManager.getFeeType(this)
        val feeValue = PreferencesManager.getFeeValue(this)
        val feeInfo = if (feeType != "none" && feeValue > 0) {
            when (feeType) {
                "fixed" -> "Including fee: ${formatCurrency(feeValue.toLong())}"
                "percentage" -> "Including fee: $feeValue%"
                else -> null
            }
        } else {
            null
        }

        val dialog = QRResultDialog.newInstance(
            payload = payload,
            amount = amount,
            merchantName = merchantName,
            merchantCity = merchantCity,
            feeInfo = feeInfo,
            feeValue = feeValue,
            feeType = feeType,
        )

//        dialog.setOnSaveClickedListener { savedPayload ->
//            viewModel.saveTransaction()
//        }

        dialog.show(supportFragmentManager, "QRResultDialog")
        viewModel.saveTransaction()
    }
}