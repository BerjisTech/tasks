package tech.berjis.tasks;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA0LIdkYM:APA91bH3kvrY_xp4kWZDd0h9SlnJnQhCcFTscYe_0QCSzL34K4Gu11GNeaxLH-pIQrKgG7mRSqyi6d_4-rICGCtRMAfEaEEpgYEVvCC-VJr-feSwF792WP_IOA6qSilt4f44lRlBP1ZE"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}