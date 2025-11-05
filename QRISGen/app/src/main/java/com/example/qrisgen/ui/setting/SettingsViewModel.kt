package com.example.qrisgen.ui.settings

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrisgen.data.model.ParsedQRIS
import com.example.qrisgen.data.repository.SettingsRepository
import com.example.qrisgen.utils.QRCodeDecoder
import com.example.qrisgen.utils.QRISParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<SettingsUiState>()
    val uiState: LiveData<SettingsUiState> = _uiState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentPayload: String? = null

    init {
        loadExistingSettings()
    }

    fun loadExistingSettings() {
        viewModelScope.launch {
            try {
                val settings = repository.loadCompleteSettings()

                if (settings != null) {
                    currentPayload = settings.qrisPayload
                    _uiState.value = SettingsUiState.Loaded(
                        qrisPayload = settings.qrisPayload,
                        merchantName = settings.merchantInfo.name,
                        merchantCity = settings.merchantInfo.city,
                        merchantCategory = settings.merchantInfo.category,
                        postalCode = settings.merchantInfo.postCode,
                        feeType = settings.feeType,
                        feeValue = settings.feeValue
                    )
                } else {
                    _uiState.value = SettingsUiState.Empty
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load settings: ${e.message}"
            }
        }
    }

    fun parseQRIS(payload: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                if (payload.isBlank()) {
                    _errorMessage.value = "Please paste or scan QRIS first"
                    return@launch
                }

                val isValid = withContext(Dispatchers.Default) {
                    QRISParser.isValidQRIS(payload)
                }

                if (!isValid) {
                    _errorMessage.value = "Invalid QRIS format"
                    return@launch
                }

                val parsed = withContext(Dispatchers.Default) {
                    QRISParser.parseQRIS(payload)
                }

                if (parsed.isValid || parsed.merchant.name.isNotEmpty()) {
                    currentPayload = payload
                    _uiState.value = SettingsUiState.Parsed(parsed)
                } else {
                    _errorMessage.value = "Failed to parse QRIS: ${parsed.errorMessage}"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Parse error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun decodeQRFromImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val qrContent = withContext(Dispatchers.IO) {
                    QRCodeDecoder.decodeBitmap(bitmap)
                }

                if (qrContent != null) {
                    parseQRIS(qrContent)
                } else {
                    _errorMessage.value = "Invalid QRIS image"
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to decode QR: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun saveSettings(
        merchantName: String,
        merchantCity: String,
        merchantCategory: String?,
        postalCode: String?,
        feeType: String,
        feeValue: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                if (currentPayload.isNullOrBlank()) {
                    _errorMessage.value = "Please parse QRIS first"
                    return@launch
                }

                if (merchantName.isBlank()) {
                    _errorMessage.value = "Merchant name is required"
                    return@launch
                }

                if (merchantCity.isBlank()) {
                    _errorMessage.value = "City is required"
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    repository.saveCompleteSettings(
                        payload = currentPayload!!,
                        merchantName = merchantName,
                        merchantCity = merchantCity,
                        merchantCategory = merchantCategory,
                        postalCode = postalCode,
                        feeType = feeType,
                        feeValue = feeValue
                    )
                }

                _uiState.value = SettingsUiState.Saved

            } catch (e: Exception) {
                _errorMessage.value = "Failed to save: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

sealed class SettingsUiState {
    object Empty : SettingsUiState()

    data class Loaded(
        val qrisPayload: String,
        val merchantName: String,
        val merchantCity: String,
        val merchantCategory: String?,
        val postalCode: String?,
        val feeType: String,
        val feeValue: Double
    ) : SettingsUiState()

    data class Parsed(
        val parsedQRIS: ParsedQRIS
    ) : SettingsUiState()

    object Saved : SettingsUiState()
}