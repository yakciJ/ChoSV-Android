package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * Model cho Category với cấu trúc cây (có children)
 */
@Serializable
data class CategoryTree(
    val categoryId: Int,
    val name: String,
    val imageUrl: String,
    val childs: List<CategoryTree> = emptyList()
)

