package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.UploadImageResponse
import com.chosv.chosv_android.data.model.UploadMultipleImagesResponse
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImageApiService {

    /**
     * Upload 1 ảnh lên server
     * @param file: File ảnh cần upload
     * @return: Response chứa URL của ảnh đã upload
     */
    @Multipart
    @POST("api/Image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadImageResponse

    /**
     * Upload nhiều ảnh lên server (tối đa 6 ảnh)
     * @param files: Danh sách các file ảnh cần upload
     * @return: Response chứa danh sách URL của các ảnh đã upload
     */
    @Multipart
    @POST("api/Image/upload-multiple")
    suspend fun uploadMultipleImages(
        @Part files: List<MultipartBody.Part>
    ): UploadMultipleImagesResponse

    /**
     * Xóa ảnh theo URL
     * @param imageUrl: URL của ảnh cần xóa
     */
    @DELETE("api/Image")
    suspend fun deleteImage(
        @Query("imageUrl") imageUrl: String
    )
}

