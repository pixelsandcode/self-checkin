/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

public class ApiConstants {

  // Api Ips
//  public static final String API_URL = "http://api.tipi.me/v1";
//  public static final String API_IMAGE_URL = "http://api.tipi.me/cdn";

  // Api Stg
  public static final String API_URL = "http://stg.api.tipi.me/v1";
  public static final String API_IMAGE_URL = "http://stg.api.tipi.me/cdn";

  // Routes
  public static final String LOGIN = "/dashboard/login";
  public static final String HOME_TOWN = "/countries/suggest";
  public static final String SIGN_UP = "/dashboard/users/signup";
  public static final String FIND = "/dashboard/users/find";
  public static final String CLAIM = "/dashboard/users/{user_key}/claim";

  // Images
  public static final String AVATAR_URL = "/user/%s/savatar";

  // Prefs
  public static final String USER_NAME = "username";
  public static final String PASSWORD = "password";
  public static final String AVATAR = "avatar";
  public static final String PASSPORT = "passport";
}
