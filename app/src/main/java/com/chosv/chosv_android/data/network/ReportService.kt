package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.ReportRequest
import com.chosv.chosv_android.data.model.ReportResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportApiService {

    /**
     * Gửi báo cáo vi phạm
     * @param request Thông tin báo cáo bao gồm: reportedEntityId, reportedEntityType, reportReason
     */
    @POST("api/Report")
    suspend fun sendReport(
        @Body request: ReportRequest
    ): ReportResponse
}

