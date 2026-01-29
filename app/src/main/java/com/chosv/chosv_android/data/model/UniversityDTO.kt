package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * DTO cho thông tin trường đại học
 */
@Serializable
data class University(
    val universityId: Int,
    val universityName: String,
    val universityEmail: String,
    val universityLogo: String
)

/**
 * Response cho danh sách trường đại học có phân trang
 */
@Serializable
data class UniversityResponse(
    val items: List<University>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)
