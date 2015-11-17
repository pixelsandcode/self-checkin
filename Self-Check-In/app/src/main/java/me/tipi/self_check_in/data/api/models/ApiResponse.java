/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;


public final class ApiResponse {
  public final boolean success;
  public final String error;
  public final String message;

  /**
   * Instantiates a new Api response.
   *
   * @param success the success
   * @param error   the error
   * @param message the message
   */
  public ApiResponse(boolean success, String error, String message) {
    this.success = success;
    this.error = error;
    this.message = message;
  }
}
