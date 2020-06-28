package com.bagas.socialmediaapps.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAZA3oYNs:APA91bG6CI27oxw_9Uyw9IT-QeIDRdEJAr7lUYvKxkNlyhgHhgNFqVG_EA0SQiM1G3-rkSGsYfVr1o_4GlMqyMg0IwEgpqyuAtMj2ux5m4AMaLnUOgVynM0j7bxBk9oS64YZIIZ-CQMC"
    })

    @POST("fcm/send")
    public Call<Response> sendNotification(@Body Sender body);
}
