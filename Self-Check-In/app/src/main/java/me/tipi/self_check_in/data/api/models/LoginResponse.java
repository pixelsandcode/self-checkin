/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

public class LoginResponse extends BaseResponse {

  public final Hostel data;

  public LoginResponse(Hostel data) {
    this.data = data;
  }
}
