/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.CountryResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

public interface AuthenticationService {

  /**
   * Login call.
   *
   * @param userRequest the user request
   * @param cb          the cb
   */
  @POST(ApiConstants.LOGIN)
  void login(@Body LoginRequest userRequest, Callback<Response> cb);

  /**
   * Gets suggested countries.
   *
   * @param query the query
   * @param cb    the cb
   */
  @GET(ApiConstants.HOME_TOWN)
  void getSuggestedCountries(@Query("q") String query, Callback<CountryResponse> cb);

  @Multipart
  @POST(ApiConstants.SIGN_UP)
  void addGuest(
      @Part("avatar") TypedFile avatar,
      @Part("email") String email,
      @Part("name") String name,
      @Part("city") String city,
      @Part("country") String country,
      @Part("passport[number]") String passportNumber,
      @Part("dob") String dob,
      @Part("booking[reference_number]") String referenceNumber,
      @Part("booking[from]") String from,
      @Part("booking[to]") String to,
      Callback<ApiResponse> cb);
}
