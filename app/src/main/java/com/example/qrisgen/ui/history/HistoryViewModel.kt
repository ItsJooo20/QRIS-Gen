package com.example.qrisgen.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.qrisgen.data.model.QRISTransaction
import com.example.qrisgen.data.repository.QRISRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: QRISRepository) : ViewModel() {

    val allTransactions: LiveData<List<QRISTransaction>> =
        repository.allTransactions.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun deleteTransaction(transaction: QRISTransaction) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteTransaction(transaction)
                _successMessage.value = "Transaction deleted"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteAllTransactions()
                _successMessage.value = "All history cleared"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

class HistoryViewModelFactory(
    private val repository: QRISRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}