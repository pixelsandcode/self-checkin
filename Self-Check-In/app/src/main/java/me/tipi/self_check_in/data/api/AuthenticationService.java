/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

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

  /**
   * Login call.
   *
   * @param userRequest the user request
   * @return the call
   */
  @POST(ApiConstants.LOGIN)
  Call<ApiResponse> login(@Body LoginRequest userRequest);

  /**
   * Gets suggested countries.
   *
   * @param query the query
   * @return the suggested countries
   */
  @GET(ApiConstants.HOME_TOWN)
  Call<CountryResponse> getSuggestedCountries(@Query("q") String query);

  /**
   * Add guest call.
   *
   * @param email           the email
   * @param name            the name
   * @param city            the city
   * @param country         the country
   * @param passportNumber  the passport number
   * @param dob             the dob
   * @param referenceNumber the reference number
   * @param from            the from
   * @param to              the to
   * @param avatar          the avatar
   * @return the call
   */
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
