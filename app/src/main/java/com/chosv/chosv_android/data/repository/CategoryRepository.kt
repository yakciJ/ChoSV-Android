package com.chosv.chosv_android.data.repository

import com.chosv.chosv_android.data.model.CategoryProductsResponse
import com.chosv.chosv_android.data.model.CategoryTree
import com.chosv.chosv_android.data.network.CategoryApiService

/**
 * Interface cho repository quản lý các hoạt động liên quan đến Category.
 */
interface CategoryRepository {
    /**
     * Lấy danh sách category dạng cây
     * @return Danh sách CategoryTree bao gồm category cha và con
     */
    suspend fun getCategoryTree(): List<CategoryTree>

    /**
     * Lấy danh sách tất cả category đã được flatten (phẳng hóa)
     * Hữu ích khi cần hiển thị danh sách phẳng để chọn
     * @return Danh sách CategoryTree không có children
     */
    suspend fun getAllCategoriesFlat(): List<CategoryTree>

    /**
     * Lấy danh sách sản phẩm theo danh mục
     * @param categoryId: ID của danh mục
     * @param page: số trang
     * @param pageSize: số lượng sản phẩm trên mỗi trang
     */
    suspend fun getCategoryProducts(categoryId: Int, page: Int, pageSize: Int): CategoryProductsResponse
}

/**
 * Implementation mặc định của CategoryRepository.
 */
class CategoryRepositoryImpl(
    private val categoryApiService: CategoryApiService
) : CategoryRepository {

    override suspend fun getCategoryTree(): List<CategoryTree> {
        return categoryApiService.getCategoryTree()
    }

    override suspend fun getAllCategoriesFlat(): List<CategoryTree> {
        val tree = categoryApiService.getCategoryTree()
        return flattenCategoryTree(tree)
    }

    override suspend fun getCategoryProducts(categoryId: Int, page: Int, pageSize: Int): CategoryProductsResponse {
        return categoryApiService.getCategoryProducts(categoryId, page, pageSize)
    }

    /**
     * Flatten category tree thành danh sách phẳng
     */
    private fun flattenCategoryTree(categories: List<CategoryTree>): List<CategoryTree> {
        val result = mutableListOf<CategoryTree>()

        fun traverse(category: CategoryTree) {
            // Thêm category hiện tại (không có children)
            result.add(category.copy(childs = emptyList()))
            // Duyệt qua tất cả children
            category.childs.forEach { child ->
                traverse(child)
            }
        }

        categories.forEach { category ->
            traverse(category)
        }

        return result
    }
}
