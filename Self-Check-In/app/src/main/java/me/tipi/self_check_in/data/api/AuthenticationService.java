/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.BaseResponse;
import me.tipi.self_check_in.data.api.models.ClaimRequest;
import me.tipi.self_check_in.data.api.models.ClaimResponse;
import me.tipi.self_check_in.data.api.models.CountryResponse;
import me.tipi.self_check_in.data.api.models.FindResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import me.tipi.self_check_in.data.api.models.LoginResponse;
import me.tipi.self_check_in.data.api.models.NoteRequest;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AuthenticationService {

  /**
   * Login call.
   *
   * @param userRequest the user request
   * @param cb the cb
   */
  @POST(ApiConstants.LOGIN) Call<LoginResponse> login(@Body LoginRequest userRequest);

  /**
   * Gets suggested countries.
   *
   * @param query the query
   * @param cb the cb
   */
  @GET(ApiConstants.HOME_TOWN) Call<CountryResponse> getSuggestedCountries(
      @Query("q") String query);

  @GET(ApiConstants.HOME_TOWN) Call<CountryResponse> getCountries(@Query("q") String query);

  /**
   * Find user.
   *
   * @param email the email
   * @param cb the cb
   */
  @GET(ApiConstants.FIND) Call<FindResponse> findUser(@Query("email") String email);

  /**
   * Add guest.
   *
   * @param avatar the avatar
   * @param scan the scan
   * @param email the email
   * @param name the name
   * @param city the city
   * @param country the country
   * @param passportNumber the passport number
   * @param dob the dob
   * @param referenceNumber the reference number
   * @param from the from
   * @param to the to
   * @param cb the cb
   */
  @Multipart @POST(ApiConstants.SIGN_UP) Call<ClaimResponse> addGuest(
      @Part MultipartBody.Part avatar, @Part MultipartBody.Part scan,
      @Part("email") RequestBody email, @Part("name") RequestBody name,
      @Part("city") RequestBody city, @Part("country") RequestBody country,
      @Part("passport[number]") RequestBody passportNumber, @Part("dob") RequestBody dob,
      @Part("booking[reference_number]") RequestBody referenceNumber,
      @Part("booking[from]") RequestBody from, @Part("booking[to]") RequestBody to,
      @Part("gender") RequestBody gender);

  @Multipart @POST(ApiConstants.LOG) Call<BaseResponse> sendLog(@Part("name") RequestBody name,
      @Part MultipartBody.Part log);

  /**
   * Claim.
   *
   * @param userKey the user key
   * @param claimRequest the claim request
   * @param cb the cb
   */
  @POST(ApiConstants.CLAIM) Call<ClaimResponse> claim(@Path("user_key") String userKey,
      @Body ClaimRequest claimRequest);

  @GET(ApiConstants.TERMS) Call<LoginResponse> getTerms(@Path("hostel_key") String hostelKey);

  @POST(ApiConstants.NOTE) Call<ApiResponse> sendNote(@Path("guest_key") String guestKey,
      @Body NoteRequest noteRequest);
}
