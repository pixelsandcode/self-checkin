package me.tipi.self_check_in.data.api;

import com.squareup.okhttp.RequestBody;

import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.CountryResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;

public interface AuthenticationService {

  @POST(ApiConstants.LOGIN)
  Call<ApiResponse> login(@Body LoginRequest userRequest);

  @GET(ApiConstants.HOME_TOWN)
  Call<CountryResponse> getSuggestedCountries(@Query("q") String query);

  @Multipart
  @POST(ApiConstants.SIGN_UP)
  Call<ApiResponse> addGuest(
      @Part("email") RequestBody email,
      @Part("name") RequestBody name,
      @Part("city") RequestBody city,
      @Part("country") RequestBody country,
      @Part("passport[number]") RequestBody passportNumber,
      @Part("dob") RequestBody dob,
      @Part("booking[reference_number]") RequestBody referenceNumber,
      @Part("booking[from]") RequestBody from,
      @Part("booking[to]") RequestBody to,
      @Part("avatar\"; filename=\"image.jpg\"") RequestBody avatar);
}
