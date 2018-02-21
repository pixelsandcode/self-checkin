package me.tipi.self_check_in.data.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.BaseResponse;
import me.tipi.self_check_in.data.api.models.ClaimRequest;
import me.tipi.self_check_in.data.api.models.ClaimResponse;
import me.tipi.self_check_in.data.api.models.Country;
import me.tipi.self_check_in.data.api.models.CountryResponse;
import me.tipi.self_check_in.data.api.models.FindResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import me.tipi.self_check_in.data.api.models.LoginResponse;
import me.tipi.self_check_in.data.api.models.NoteRequest;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Khashayar on 21/09/2017.
 */

public class NetworkRequestManager {

  @Inject AuthenticationService authenticationService;

  private static final String IMG_JPEG = "image/jpeg";

  public NetworkRequestManager() {
    SelfCheckInApp.get().getSelfCheckInComponent().inject(this);
  }

  public void callLoginApi(LoginRequest loginRequest, AppCallback callback) {
    Call<LoginResponse> loginResponseCall = authenticationService.login(loginRequest);
    loginResponseCall.enqueue(callback);
  }

  public void callGetSuggestedCountriesApi(String query, Callback callback) {
    Call<CountryResponse> countryResponseCall = authenticationService.getSuggestedCountries(query);
    countryResponseCall.enqueue(callback);
  }

  public List<Country> callGetCountriesApi(String query) {
    Call<CountryResponse> call = authenticationService.getCountries(query);

    try {
      CountryResponse autocompleteResponse = call.execute().body();
      List<Country> autocompleteResponsePredictions = autocompleteResponse.data;
      //Here are your Autocomplete Objects
      return autocompleteResponsePredictions;
    } catch (IOException e) {
    }

    return new ArrayList<>();
  }

  public void callFindUserApi(String query, AppCallback callback) {
    Call<FindResponse> loginResponseCall = authenticationService.findUser(query);
    loginResponseCall.enqueue(callback);
  }

  public void callAddGuestApi(File avatar, File scan, String email, String name, String city,
      String country, String passportNumber, String dob, String referenceNumber, String from,
      String to, int gender, AddGuestCallback callback) {

    RequestBody avatarBody = RequestBody.create(MediaType.parse("image/jpeg"), avatar);
    MultipartBody.Part avatarFileAndBody =
        MultipartBody.Part.createFormData("avatar", avatar.getName(), avatarBody);

    RequestBody scanBody = RequestBody.create(MediaType.parse("image/jpeg"), scan);
    MultipartBody.Part scanBodyFileAndBody =
        MultipartBody.Part.createFormData("passport[scan]", scan.getName(), scanBody);

    RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
    RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
    RequestBody cityBody = RequestBody.create(MediaType.parse("text/plain"), city);
    RequestBody countryBody = RequestBody.create(MediaType.parse("text/plain"), country);
    RequestBody passportNumberBody =
        RequestBody.create(MediaType.parse("text/plain"), passportNumber);
    RequestBody dobBody = RequestBody.create(MediaType.parse("text/plain"), dob);
    RequestBody referenceNumberBody = null;

    if (referenceNumber!= null) {
      referenceNumberBody = RequestBody.create(MediaType.parse("text/plain"), referenceNumber);
    }

    RequestBody fromBody = RequestBody.create(MediaType.parse("text/plain"), from);
    RequestBody toBody = RequestBody.create(MediaType.parse("text/plain"), to);
    RequestBody genderBody =
        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(gender));

    Call<ClaimResponse> loginResponseCall =
        authenticationService.addGuest(avatarFileAndBody, scanBodyFileAndBody, emailBody, nameBody,
            cityBody, countryBody, passportNumberBody, dobBody, referenceNumberBody, fromBody,
            toBody, genderBody);
    loginResponseCall.enqueue(callback);
  }

  public void callSendLogApi(String name, File log, Callback callback) {

    RequestBody logBody = RequestBody.create(MediaType.parse("text/plain/"), log);
    MultipartBody.Part logFileAndBody =
        MultipartBody.Part.createFormData("file", log.getName(), logBody);

    RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);

    Call<BaseResponse> loginResponseCall = authenticationService.sendLog(nameBody, logFileAndBody);
    loginResponseCall.enqueue(callback);
  }

  public void callClaimApi(String userKey, ClaimRequest claimRequest, Callback callback) {
    Call<ClaimResponse> loginResponseCall = authenticationService.claim(userKey, claimRequest);
    loginResponseCall.enqueue(callback);
  }

  public void callGetTermsApi(String hostelKey, Callback callback) {
    Call<LoginResponse> loginResponseCall = authenticationService.getTerms(hostelKey);
    loginResponseCall.enqueue(callback);
  }

  public void callSendNoteApi(String guestKey, NoteRequest noteRequest, Callback callback) {
    Call<ApiResponse> loginResponseCall = authenticationService.sendNote(guestKey, noteRequest);
    loginResponseCall.enqueue(callback);
  }
}
