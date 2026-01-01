package com.chosv.chosv_android.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.chosv.chosv_android.data.model.UploadImageResponse
import com.chosv.chosv_android.data.model.UploadMultipleImagesResponse
import com.chosv.chosv_android.data.network.ImageApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

/**
 * Interface cho repository quản lý các hoạt động liên quan đến Image.
 */
interface ImageRepository {
    /**
     * Upload 1 ảnh lên server
     * @param uri: Uri của ảnh cần upload
     * @return: Response chứa URL của ảnh đã upload
     */
    suspend fun uploadImage(uri: Uri): UploadImageResponse

    /**
     * Upload nhiều ảnh lên server (tối đa 6 ảnh)
     * @param uris: Danh sách Uri của các ảnh cần upload
     * @return: Response chứa danh sách URL của các ảnh đã upload
     */
    suspend fun uploadMultipleImages(uris: List<Uri>): UploadMultipleImagesResponse

    /**
     * Xóa ảnh theo URL
     * @param imageUrl: URL của ảnh cần xóa
     */
    suspend fun deleteImage(imageUrl: String)
}

/**
 * Implementation mặc định của ImageRepository.
 */
class ImageRepositoryImpl(
    private val imageApiService: ImageApiService,
    private val context: Context
) : ImageRepository {

    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun uploadImage(uri: Uri): UploadImageResponse {
        val filePart = uriToMultipartBodyPart(uri, "File")
        return imageApiService.uploadImage(filePart)
    }

    override suspend fun uploadMultipleImages(uris: List<Uri>): UploadMultipleImagesResponse {
        if (uris.size > 6) {
            throw IllegalArgumentException("Số lượng hình ảnh không được vượt quá 6")
        }

        val fileParts = uris.map { uri ->
            uriToMultipartBodyPart(uri, "files")
        }
        return imageApiService.uploadMultipleImages(fileParts)
    }

    override suspend fun deleteImage(imageUrl: String) {
        imageApiService.deleteImage(imageUrl)
    }

    /**
     * Chuyển đổi Uri thành MultipartBody.Part để upload
     */
    private fun uriToMultipartBodyPart(uri: Uri, partName: String): MultipartBody.Part {
        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Không thể mở file từ Uri: $uri")

        val fileName = getFileNameFromUri(uri) ?: "image_${System.currentTimeMillis()}.jpg"
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        val bytes = inputStream.use { it.readBytes() }
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }

    /**
     * Lấy tên file từ Uri
     */
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null

        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
        }

        if (fileName == null) {
            fileName = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) path.substring(cut + 1) else path
            }
        }

        return fileName
    }
}

