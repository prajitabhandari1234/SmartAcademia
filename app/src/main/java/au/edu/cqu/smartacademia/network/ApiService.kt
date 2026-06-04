package au.edu.cqu.smartacademia.network

import au.edu.cqu.smartacademia.model.TaskResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * Retrofit API service interface for SmartAcademia.
 *
 * This interface defines HTTP endpoints used to
 * retrieve academic task data from a remote server.
 *
 * Supports Assignment 3 requirements:
 * - HTTP Data Fetching
 * - JSON Decoding
 * - Remote Data Integration
 */
interface ApiService {

    /**
     * Retrieves task data from the remote JSON source.
     *
     * The JSON response is automatically mapped to a
     * list of TaskResponse objects using Retrofit and Gson.
     *
     * Endpoint:
     * tasks.json
     *
     * @return HTTP response containing a list of tasks.
     */
    @GET("tasks.json")
    suspend fun getTasks(): Response<List<TaskResponse>>
}