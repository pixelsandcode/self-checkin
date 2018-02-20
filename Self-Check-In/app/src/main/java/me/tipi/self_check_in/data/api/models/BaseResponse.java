package me.tipi.self_check_in.data.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Khashayar on 21/09/2017.
 */

public class BaseResponse {

  @SerializedName("statusCode") @Expose private Integer statusCode;
  @SerializedName("error") @Expose private String error;
  @SerializedName("message") @Expose private String message;

  public Integer getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


}
