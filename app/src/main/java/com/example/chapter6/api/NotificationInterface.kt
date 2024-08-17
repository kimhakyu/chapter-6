package com.example.chapter6.api

import com.example.chapter6.AccessToken
import com.example.chapter6.model.Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationInterface {

    @POST("/v1/projects/part2-chapter6-71723/messages:send")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )

    fun notification (
        @Body message: Notification,
        @Header("Authorization") accessToken: String = "Bearer ${AccessToken.getAccessToken()}"
    ): Call<Notification>

}