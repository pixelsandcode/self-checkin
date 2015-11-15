package me.tipi.self_check_in.data.api.models;


public final class ApiResponse {
  public final boolean success;
  public final String error;
  public final String message;

  public ApiResponse(boolean success, String error, String message) {
    this.success = success;
    this.error = error;
    this.message = message;
  }
}
