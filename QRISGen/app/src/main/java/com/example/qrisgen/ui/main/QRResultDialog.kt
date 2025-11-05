package com.example.qrisgen.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.qrisgen.R
import com.example.qrisgen.databinding.DialogQrResultBinding
import com.example.qrisgen.utils.Constants
import com.example.qrisgen.utils.PreferencesManager
import com.example.qrisgen.utils.QRCodeHelper
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToLong

class QRResultDialog : DialogFragment() {

    private var _binding: DialogQrResultBinding? = null
    private val binding get() = _binding!!

    private var qrBitmap: Bitmap? = null
    private var qrPayload: String? = null

    private var onSaveClicked: ((String) -> Unit)? = null
    private var onShareClicked: ((Bitmap) -> Unit)? = null

    companion object {
        private const val ARG_AMOUNT = "amount"
        private const val ARG_FEE_INFO = "fee_info"

        fun newInstance(
            payload: String,
            amount: Long,
            merchantName: String,
            merchantCity: String,
            feeInfo: String? = null,
            feeValue: Double,
            feeType: String?,
        ): QRResultDialog {
            val dialog = QRResultDialog()
            val args = Bundle().apply {
                putString(PreferencesManager.Keys.QRIS_PAYLOAD, payload)
                putLong(ARG_AMOUNT, amount)
                putString(PreferencesManager.Keys.MERCHANT_NAME, merchantName)
                putString(PreferencesManager.Keys.MERCHANT_CITY, merchantCity)
                putString(ARG_FEE_INFO, feeInfo)
                putDouble(PreferencesManager.Keys.FEE_VALUE, feeValue)
                putString(PreferencesManager.Keys.FEE_TYPE, feeType)
            }
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogQrResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()
        setupClickListeners()
    }

    private fun loadData() {
        val payload = arguments?.getString(PreferencesManager.Keys.QRIS_PAYLOAD) ?: return
        val amount = arguments?.getLong(ARG_AMOUNT) ?: 0L
        val merchantName = arguments?.getString(PreferencesManager.Keys.MERCHANT_NAME) ?: ""
        val merchantCity = arguments?.getString(PreferencesManager.Keys.MERCHANT_CITY) ?: ""
        val feeInfo = arguments?.getString(ARG_FEE_INFO)
        val feeTotal = arguments?.getDouble(PreferencesManager.Keys.FEE_VALUE) ?: 0.0
        val amountDouble: Double = amount.toDouble()
        val feeType = arguments?.getString(PreferencesManager.Keys.FEE_TYPE) ?: ""
        val totalAmount = if (feeType == Constants.FeeType.PERCENTAGE) {
            amountDouble + (amountDouble * (feeTotal / 100))
        } else {
            amountDouble + feeTotal
        }

//        val totalAmount = feeTotal.plus(amountDouble)

        println("fee: " + feeTotal)
        println("amountTotal = " + amountDouble + " + " + feeTotal)
        println("total  = " + totalAmount)

        qrPayload = payload
        binding.tvMerchantName.text = merchantName
        binding.tvMerchantCity.text = merchantCity
        binding.tvAmount.text = formatCurrency(totalAmount)

        if (!feeInfo.isNullOrEmpty()) {
            binding.tvFeeInfo.visibility = View.VISIBLE
            binding.tvFeeInfo.text = feeInfo
        } else {
            binding.tvFeeInfo.visibility = View.GONE
        }

        try {
            qrBitmap = QRCodeHelper.generateQRBitmap(payload, 512)
            binding.ivQRCode.setImageBitmap(qrBitmap)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnCopy.setOnClickListener {
            qrPayload?.let { payload ->
                copyToClipboard(payload)
                Toast.makeText(context, "copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShare.setOnClickListener {
            qrBitmap?.let { bitmap ->
                onShareClicked?.invoke(bitmap)
                Toast.makeText(context, "Share coming soon!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSave.setOnClickListener {
            qrPayload?.let { payload ->
                onSaveClicked?.invoke(payload)
                Toast.makeText(context, "Download coming soon!", Toast.LENGTH_SHORT).show()
//                dismiss()
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QRIS Payload", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }

    fun setOnSaveClickedListener(listener: (String) -> Unit) {
        onSaveClicked = listener
    }

    fun setOnShareClickedListener(listener: (Bitmap) -> Unit) {
        onShareClicked = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}