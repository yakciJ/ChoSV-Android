package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * Request để gửi báo cáo
 */
@Serializable
data class ReportRequest(
    val reportedEntityId: String,
    val reportedEntityType: String,
    val reportReason: String
)

/**
 * Response khi gửi báo cáo thành công
 */
@Serializable
data class ReportResponse(
    val message: String
)

/**
 * Enum định nghĩa các loại entity có thể báo cáo
 */
enum class ReportEntityType(val value: String) {
    User("User"),
    Product("Product"),
    Comment("Comment")
}

