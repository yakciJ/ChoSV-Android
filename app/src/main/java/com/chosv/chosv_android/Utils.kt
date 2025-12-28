package com.chosv.chosv_android

import com.chosv.chosv_android.data.EnvVariable
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

fun convertBaseUrl(
    url: String?,
    oldHost: String = EnvVariable.OLD_HOST_BACKEND,
    newHost: String = EnvVariable.BASE_URL
) : String {
    if (url.isNullOrEmpty()) return ""
    return if (url.contains(oldHost)) {
        url.replace(oldHost, newHost)
    } else {
        url
    }
}

fun formatDateString(inputDate: String?): String {
    if (inputDate.isNullOrEmpty()) return "N/A"
    // Mẫu này giả định ngày đầu vào có dạng "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        val date = inputFormat.parse(inputDate)
        if (date != null) outputFormat.format(date) else "N/A"
    } catch (e: Exception) {
        // Nếu không parse được, thử một định dạng khác hoặc trả về ngày gốc
        try {
            val simplerInputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = simplerInputFormat.parse(inputDate)
            if (date != null) outputFormat.format(date) else "N/A"
        } catch (e2: Exception) {
            inputDate // Trả về ngày gốc nếu không thể định dạng
        }
    }
}

