package com.chosv.chosv_android.data

import android.content.Context
import com.chosv.chosv_android.data.interceptor.AuthInterceptor
import com.chosv.chosv_android.data.interceptor.TokenAuthenticator
import com.chosv.chosv_android.data.network.AuthApiService
import com.chosv.chosv_android.data.network.CategoryApiService
import com.chosv.chosv_android.data.network.ChatApiService
import com.chosv.chosv_android.data.network.ChatHubService
import com.chosv.chosv_android.data.network.FavoriteApiService
import com.chosv.chosv_android.data.network.ImageApiService
import com.chosv.chosv_android.data.network.NotificationApiService
import com.chosv.chosv_android.data.network.NotificationHubService
import com.chosv.chosv_android.data.network.ProductApiService
import com.chosv.chosv_android.data.network.ReportApiService
import com.chosv.chosv_android.data.network.UserApiService
import com.chosv.chosv_android.data.repository.AuthRepository
import com.chosv.chosv_android.data.repository.AuthRepositoryImpl
import com.chosv.chosv_android.data.repository.CategoryRepository
import com.chosv.chosv_android.data.repository.CategoryRepositoryImpl
import com.chosv.chosv_android.data.repository.ChatRepository
import com.chosv.chosv_android.data.repository.ChatRepositoryImpl
import com.chosv.chosv_android.data.repository.FavoriteRepository
import com.chosv.chosv_android.data.repository.FavoriteRepositoryImpl
import com.chosv.chosv_android.data.repository.ImageRepository
import com.chosv.chosv_android.data.repository.ImageRepositoryImpl
import com.chosv.chosv_android.data.repository.NotificationRepository
import com.chosv.chosv_android.data.repository.NotificationRepositoryImpl
import com.chosv.chosv_android.data.repository.ProductRepository
import com.chosv.chosv_android.data.repository.ProductRepositoryImpl
import com.chosv.chosv_android.data.repository.ReportRepository
import com.chosv.chosv_android.data.repository.ReportRepositoryImpl
import com.chosv.chosv_android.data.repository.UniversityRepository
import com.chosv.chosv_android.data.repository.UniversityRepositoryImpl
import com.chosv.chosv_android.data.repository.UserProfileRepository
import com.chosv.chosv_android.data.repository.UserProfileRepositoryImpl
import com.chosv.chosv_android.data.repository.UserRepository
import com.chosv.chosv_android.data.repository.UserRepositoryImpl
import com.chosv.chosv_android.preferences.TokenPreferences
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.JavaNetCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

object EnvVariable {
    const val OLD_HOST_BACKEND = "https://localhost:7049/"
    const val BASE_URL = "http://10.0.2.2:5092/"
}

interface AppContainer {
    val tokenPreferences: TokenPreferences
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val universityRepository: UniversityRepository
    val userProfileRepository: UserProfileRepository
    val productRepository: ProductRepository
    val favoriteRepository: FavoriteRepository
    val imageRepository: ImageRepository
    val categoryRepository: CategoryRepository
    val chatRepository: ChatRepository
    val chatHubService: ChatHubService
    val reportRepository: ReportRepository
    val notificationRepository: NotificationRepository
    val notificationHubService: NotificationHubService
}

class DefaultAppContainer(
    private val context: Context
) : AppContainer {
    override val tokenPreferences by lazy {
        TokenPreferences(context)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true // Cho phép parse string trực tiếp
    }

    // CookieManager để lưu và gửi cookies (bao gồm refreshToken)
    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    private val cookieJar = JavaNetCookieJar(cookieManager)

    private val authInterceptor by lazy {
        AuthInterceptor(tokenPreferences)
    }

    // OkHttpClient cho refresh token - có CookieJar, KHÔNG có AuthInterceptor
    private val tokenRefreshOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val tokenRefreshRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EnvVariable.BASE_URL)
            .client(tokenRefreshOkHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val tokenRefreshApiService: AuthApiService by lazy {
        tokenRefreshRetrofit.create(AuthApiService::class.java)
    }

    // TokenAuthenticator để tự động refresh token khi 401
    private val tokenAuthenticator by lazy {
        TokenAuthenticator(tokenPreferences) { tokenRefreshApiService }
    }

    // OkHttpClient chính - có CookieJar, AuthInterceptor và TokenAuthenticator
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EnvVariable.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService, tokenPreferences)
    }

    override val userRepository: UserRepository by lazy {
        UserRepositoryImpl(authApiService, tokenPreferences)
    }

    override val universityRepository: UniversityRepository by lazy {
        UniversityRepositoryImpl(authApiService)
    }

    private val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    override val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepositoryImpl(userApiService)
    }

    private val productApiService: ProductApiService by lazy {
        retrofit.create(ProductApiService::class.java)
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(productApiService)
    }

    private val favoriteApiService: FavoriteApiService by lazy {
        retrofit.create(FavoriteApiService::class.java)
    }

    override val favoriteRepository: FavoriteRepository by lazy {
        FavoriteRepositoryImpl(favoriteApiService)
    }

    private val imageApiService: ImageApiService by lazy {
        retrofit.create(ImageApiService::class.java)
    }

    override val imageRepository: ImageRepository by lazy {
        ImageRepositoryImpl(imageApiService, context)
    }

    private val categoryApiService: CategoryApiService by lazy {
        retrofit.create(CategoryApiService::class.java)
    }

    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(categoryApiService)
    }

    private val chatApiService: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }

    override val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(chatApiService)
    }

    override val chatHubService: ChatHubService by lazy {
        ChatHubService(tokenPreferences)
    }

    private val reportApiService: ReportApiService by lazy {
        retrofit.create(ReportApiService::class.java)
    }

    override val reportRepository: ReportRepository by lazy {
        ReportRepositoryImpl(reportApiService)
    }

    private val notificationApiService: NotificationApiService by lazy {
        retrofit.create(NotificationApiService::class.java)
    }

    override val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(notificationApiService)
    }

    override val notificationHubService: NotificationHubService by lazy {
        NotificationHubService(tokenPreferences)
    }

}