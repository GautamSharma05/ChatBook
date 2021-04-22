package com.example.chatbook.sendNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authoriztion:key=AAAAHTk_psM:APA91bEzl0mpg9UxTKGUOqiLKn96677dvdEaEptye_Tg0QXrimfMFUBUU9XCdnFvFuY7cRU2O2FkSHXqmVgFbkHr-83G6lZRTv6yAVAUPUE-VlEsEbg0QJUwd5jpH4P3rZrYjJKdNwux"
    })

    @POST("fom/send")
    Call<Response> sendNotification(@Body Sender body);
}
