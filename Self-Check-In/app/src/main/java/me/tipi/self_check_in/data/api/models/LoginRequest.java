/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

public class LoginRequest {

  public final String email;
  public final String password;

  /**
   * Instantiates a new Login request.
   *
   * @param email    the email
   * @param password the password
   */
  public LoginRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
