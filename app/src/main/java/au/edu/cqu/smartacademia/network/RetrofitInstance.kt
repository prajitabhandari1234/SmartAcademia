package au.edu.cqu.smartacademia.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object responsible for creating
 * and providing the Retrofit instance used
 * throughout the SmartAcademia application.
 *
 * Supports Assignment 3 requirements:
 * - HTTP Data Fetching
 * - Remote JSON Retrieval
 * - Backend Integration
 *
 * Uses:
 * - Retrofit
 * - Gson Converter
 * - Singleton Pattern
 */
object RetrofitInstance {

    /**
     * Base URL of the remote JSON data source.
     *
     * GitHub Raw Content is used to host
     * academic task data for HTTP retrieval.
     */
    private const val BASE_URL =
        "https://raw.githubusercontent.com/prajitabhandari1234/SmartAcademiaData/main/"

    /**
     * Lazily creates and provides a single
     * Retrofit API service instance.
     *
     * The Gson converter automatically maps
     * JSON responses into Kotlin objects.
     */
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiService::class.java)
    }
}