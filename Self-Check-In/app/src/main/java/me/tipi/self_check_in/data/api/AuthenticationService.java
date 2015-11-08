package me.tipi.self_check_in.data.api;

import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

public interface AuthenticationService {

  @POST(ApiConstants.LOGIN)
  Call<ApiResponse> login(@Body LoginRequest userRequest);

  /*@Multipart
  @POST(ApiConstants.SIGN_UP)
  void addUser(@Part("email") TypedString email,
               @Part("name") TypedString name,
               @Part("country") TypedString country,
               @Part("city") TypedString city,
               @Part("avatar") TypedFile avatar,
               @Part("passport[number]") TypedString number,
               @Part("passport[scan]") TypedFile scan,
               @Part("version") String version,
               @Part("device") String device,
               Callback<SignUpResponse> cb);*/
}
