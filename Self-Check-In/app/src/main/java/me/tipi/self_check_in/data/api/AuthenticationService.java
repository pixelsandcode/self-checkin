/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.ClaimRequest;
import me.tipi.self_check_in.data.api.models.CountryResponse;
import me.tipi.self_check_in.data.api.models.FindResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import me.tipi.self_check_in.data.api.models.LoginResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
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
  void login(@Body LoginRequest userRequest, Callback<LoginResponse> cb);

  /**
   * Gets suggested countries.
   *
   * @param query the query
   * @param cb    the cb
   */
  @GET(ApiConstants.HOME_TOWN)
  void getSuggestedCountries(@Query("q") String query, Callback<CountryResponse> cb);

  /**
   * Find user.
   *
   * @param email the email
   * @param cb    the cb
   */
  @GET(ApiConstants.FIND)
  void findUser(@Query("email") String email, Callback<FindResponse> cb);

  /**
   * Add guest.
   *
   * @param avatar          the avatar
   * @param scan            the scan
   * @param email           the email
   * @param name            the name
   * @param city            the city
   * @param country         the country
   * @param passportNumber  the passport number
   * @param dob             the dob
   * @param referenceNumber the reference number
   * @param from            the from
   * @param to              the to
   * @param cb              the cb
   */
  @Multipart
  @POST(ApiConstants.SIGN_UP)
  void addGuest(
      @Part("avatar") TypedFile avatar,
      @Part("passport[scan]") TypedFile scan,
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

  /**
   * Claim.
   *
   * @param userKey      the user key
   * @param claimRequest the claim request
   * @param cb           the cb
   */
  @POST(ApiConstants.CLAIM)
  void claim(
      @Path("user_key") String userKey,
      @Body ClaimRequest claimRequest,
      Callback<ApiResponse> cb);
}
