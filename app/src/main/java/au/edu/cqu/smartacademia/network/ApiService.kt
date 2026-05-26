package au.edu.cqu.smartacademia.network

import au.edu.cqu.smartacademia.model.TaskResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("tasks.json")
    suspend fun getTasks(): Response<List<TaskResponse>>
}