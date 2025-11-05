package com.example.qrisgen.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.qrisgen.data.model.QRISTransaction
import com.example.qrisgen.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onItemClick: (QRISTransaction) -> Unit,
    private val onDeleteClick: (QRISTransaction) -> Unit
) : ListAdapter<QRISTransaction, TransactionAdapter.TransactionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: QRISTransaction) {
            val amount = formatCurrency(transaction.finalAmount)
            binding.tvAmount.text = amount

            binding.tvMerchantName.text = transaction.merchant.name

            binding.tvTimestamp.text = formatTimestamp(transaction.timestamp)

            if (transaction.feeValue > 0) {
                binding.tvFeeInfo.visibility = View.VISIBLE
                val feeText = when (transaction.feeType) {
                    "fixed" -> "Fee: ${formatCurrency(transaction.feeValue.toLong())}"
                    "percentage" -> "Fee: ${transaction.feeValue}%"
                    else -> ""
                }
                binding.tvFeeInfo.text = feeText
            } else {
                binding.tvFeeInfo.visibility = View.GONE
            }

            val payload = transaction.generatedPayload ?: transaction.originalPayload
            binding.tvPayloadPreview.text = payload.take(50) + "..."

            binding.cardTransaction.setOnClickListener {
                onItemClick(transaction)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(transaction)
            }
        }

        private fun formatCurrency(amount: Long): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return formatter.format(amount)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes min ago"
                hours < 24 -> "$hours hours ago"
                days < 7 -> {
                    if (days == 1L) "Yesterday" else "$days days ago"
                }
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<QRISTransaction>() {
        override fun areItemsTheSame(
            oldItem: QRISTransaction,
            newItem: QRISTransaction
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: QRISTransaction,
            newItem: QRISTransaction
        ): Boolean {
            return oldItem == newItem
        }
    }
}