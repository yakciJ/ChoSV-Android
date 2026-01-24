package com.chosv.chosv_android.data.repository

import com.chosv.chosv_android.data.model.ReportEntityType
import com.chosv.chosv_android.data.model.ReportRequest
import com.chosv.chosv_android.data.model.ReportResponse
import com.chosv.chosv_android.data.network.ReportApiService

/**
 * Interface cho repository quản lý các hoạt động liên quan đến Report.
 */
interface ReportRepository {
    /**
     * Gửi báo cáo vi phạm
     * @param entityId ID của đối tượng bị báo cáo
     * @param entityType Loại đối tượng (User, Product, Comment)
     * @param reason Lý do báo cáo
     */
    suspend fun sendReport(
        entityId: String,
        entityType: ReportEntityType,
        reason: String
    ): ReportResponse
}

/**
 * Implementation của ReportRepository
 */
class ReportRepositoryImpl(
    private val reportApiService: ReportApiService
) : ReportRepository {

    override suspend fun sendReport(
        entityId: String,
        entityType: ReportEntityType,
        reason: String
    ): ReportResponse {
        val request = ReportRequest(
            reportedEntityId = entityId,
            reportedEntityType = entityType.value,
            reportReason = reason
        )
        return reportApiService.sendReport(request)
    }
}

