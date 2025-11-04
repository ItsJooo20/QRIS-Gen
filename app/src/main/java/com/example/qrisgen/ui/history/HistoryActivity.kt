package com.example.qrisgen.ui.history

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.qrisgen.data.local.database.AppDatabase
import com.example.qrisgen.data.model.QRISTransaction
import com.example.qrisgen.data.repository.QRISRepository
import com.example.qrisgen.databinding.ActivityHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private val viewModel: HistoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = QRISRepository(database.qrisDao())
        HistoryViewModelFactory(repository)
    }

    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onItemClick = { transaction ->
                showTransactionDetail(transaction)
            },
            onDeleteClick = { transaction ->
                showDeleteConfirmation(transaction)
            }
        )
        binding.rvTransactions.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allTransactions.observe(this) { transactions ->
            adapter.submitList(transactions)

            if (transactions.isEmpty()) {
                binding.rvTransactions.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvTransactions.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
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
        binding.btnClearAll.setOnClickListener {
            showClearAllConfirmation()
        }
    }

    private fun showTransactionDetail(transaction: QRISTransaction) {
        Toast.makeText(
            this,
            "Transaction Detail\n${transaction.merchant.name}\nAmount: ${transaction.finalAmount}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showDeleteConfirmation(transaction: QRISTransaction) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear All History")
            .setMessage("Are you sure you want to delete all transactions? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                viewModel.clearAllHistory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}