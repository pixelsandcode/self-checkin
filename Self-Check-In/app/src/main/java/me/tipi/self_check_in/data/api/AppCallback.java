package me.tipi.self_check_in.data.api;

import java.lang.annotation.Annotation;
import java.net.SocketTimeoutException;
import me.tipi.self_check_in.data.api.models.BaseResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by k.monem on 1/18/2017.
 */

public abstract class AppCallback<T> implements Callback<T> {

  private BaseResponse baseResponse;

  @Override public void onResponse(Call<T> call, Response<T> response) {

    if (response == null) {
      onNullResponse(call);
      return;
    }

    if (!response.isSuccessful()) {

      baseResponse = parseError(response.errorBody());

      if (baseResponse.getStatusCode() == 400) {
        onBadRequest(call, baseResponse);
        return;
      }

      if (baseResponse.getStatusCode() == 401) {
        onAuthError(call, baseResponse);
        return;
      }

      if (baseResponse.getStatusCode() == 404) {
        onApiNotFound(call, baseResponse);
        return;
      }

      if (baseResponse.getStatusCode() == 504) {
        onServerError(call, baseResponse);
        return;
      }

      onRequestFail(call, baseResponse);
      return;
    }

    baseResponse = (BaseResponse) response.body();
    onRequestSuccess(call, response);
  }

  @Override public void onFailure(Call<T> call, Throwable t) {
    if (t instanceof SocketTimeoutException) {
      onRequestTimeOut(call, t);
    } else {
      onRequestFail(call, t);
    }
  }

  private BaseResponse parseError(ResponseBody errorBody) {
    Converter<ResponseBody, BaseResponse> converter = new ServiceGenerator().getRetrofit()
        .responseBodyConverter(BaseResponse.class, new Annotation[0]);

    BaseResponse error;

    try {
      error = converter.convert(errorBody);
    } catch (Exception e) {
      return new BaseResponse();
    }

    return error;
  }

  public abstract void onRequestSuccess(Call<T> call, Response<T> response);

  public abstract void onRequestFail(Call<T> call, BaseResponse response);

  public abstract void onRequestFail(Call<T> call, Throwable t);

  public abstract void onBadRequest(Call<T> call, BaseResponse response);

  public abstract void onApiNotFound(Call<T> call, BaseResponse response);

  public abstract void onAuthError(Call<T> call, BaseResponse response);

  public abstract void onServerError(Call<T> call, BaseResponse response);

  public abstract void onRequestTimeOut(Call<T> call, Throwable t);

  public abstract void onNullResponse(Call<T> call);
}
