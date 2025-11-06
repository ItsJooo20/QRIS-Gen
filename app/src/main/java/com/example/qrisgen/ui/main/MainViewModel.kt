package com.example.qrisgen.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.qrisgen.data.model.ParsedQRIS
import com.example.qrisgen.data.model.QRISTransaction
import com.example.qrisgen.data.repository.QRISRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: QRISRepository) : ViewModel() {

    val allTransactions: LiveData<List<QRISTransaction>> =
        repository.allTransactions.asLiveData()

    private val _parsedQRIS = MutableLiveData<ParsedQRIS?>()
    val parsedQRIS: LiveData<ParsedQRIS?> = _parsedQRIS

    private val _originalPayload = MutableLiveData<String>()
    val originalPayload: LiveData<String> = _originalPayload

    private val _generatedPayload = MutableLiveData<String?>()
    val generatedPayload: LiveData<String?> = _generatedPayload

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _amount = MutableLiveData<Long>(0L)
    val amount: LiveData<Long> = _amount

    private val _feeType = MutableLiveData<String>("none")
    val feeType: LiveData<String> = _feeType

    private val _feeValue = MutableLiveData<Double>(0.0)
    val feeValue: LiveData<Double> = _feeValue

    fun parseInputQRIS(payload: String) {
        if (payload.isBlank()) {
            _errorMessage.value = "QRIS payload cannot be empty"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                println("Parsing payload: ${payload.take(50)}...")

                repository.validateQRIS(payload)
                val parsed = repository.parseQRIS(payload)

                println("Parse result - Valid: ${parsed.isValid}")
                println("Merchant: ${parsed.merchant.name}")

                if (parsed.isValid) {
                    _parsedQRIS.value = parsed
                    _originalPayload.value = payload
//                    _successMessage.value = "QRIS parsed successfully"
                } else {
                    _errorMessage.value = "Parse failed: ${parsed.errorMessage ?: "Unknown error"}"
                    println("Error: ${parsed.errorMessage}")
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                println("Exception: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateQRIS(
        customMerchantName: String? = null,
        customMerchantCity: String? = null,
        customMerchantPostCode: String? = null,
    ) {
        val payload = _originalPayload.value
        val amount = _amount.value ?: 0L
        val feeType = _feeType.value ?: "none"
        val feeValue = _feeValue.value ?: 0.0

        if (payload.isNullOrBlank()) {
            _errorMessage.value = "Please input or scan QRIS first"
            return
        }

        if (amount <= 0) {
            _errorMessage.value = "Amount must be greater than 0"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val generatedPayload = repository.generateDynamicQRIS(
                    originalPayload = payload,
                    amount = amount,
                    feeType = feeType,
                    feeValue = feeValue,
                    customMerchantName = customMerchantName,
                    customMerchantCity = customMerchantCity,
                    customMerchantPostCode = customMerchantPostCode,
                )

                _generatedPayload.value = generatedPayload
//                _successMessage.value = "QRIS generated successfully"

            } catch (e: Exception) {
                _errorMessage.value = "Generation failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveTransaction() {
        val payload = _originalPayload.value
        val generatedPayload = _generatedPayload.value
        val amount = _amount.value ?: 0L
        val feeType = _feeType.value ?: "none"
        val feeValue = _feeValue.value ?: 0.0
        val parsed = _parsedQRIS.value

        if (payload.isNullOrBlank() || generatedPayload.isNullOrBlank()) {
            _errorMessage.value = "Generate QRIS first before saving"
            return
        }

        if (parsed == null) {
            _errorMessage.value = "No merchant information available"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val transaction = QRISTransaction(
                    originalPayload = payload,
                    generatedPayload = generatedPayload,
                    amount = amount,
                    feeType = feeType,
                    feeValue = feeValue,
                    merchant = parsed.merchant,
                    timestamp = System.currentTimeMillis()
                )

                repository.saveTransaction(transaction)

//                _successMessage.value = "Transaction saved to history"

            } catch (e: Exception) {
                _errorMessage.value = "Failed to save: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setAmount(amount: Long) {
        _amount.value = amount
    }

    fun setFeeType(feeType: String) {
        _feeType.value = feeType
    }

    fun setFeeValue(feeValue: Double) {
        _feeValue.value = feeValue
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

class MainViewModelFactory(
    private val repository: QRISRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}