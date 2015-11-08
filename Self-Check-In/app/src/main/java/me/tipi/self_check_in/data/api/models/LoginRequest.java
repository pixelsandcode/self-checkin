package me.tipi.self_check_in.data.api.models;

/**
 * Copyright (c) 2015-2016 www.Tipi.me.
 * Created by Ashkan Hesaraki.
 * Ashkan@tipi.me
 */
public class LoginRequest {

  public final String email;
  public final String password;

  public LoginRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
