package com.chosv.chosv_android

import com.chosv.chosv_android.data.EnvVariable
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

fun convertBaseUrl(
    url: String,
    oldHost: String = EnvVariable.OLD_HOST_BACKEND,
    newHost: String = EnvVariable.BASE_URL
) : String {
    return if (url.contains(oldHost)) {
        url.replace(oldHost, newHost)
    } else {
        url
    }
}