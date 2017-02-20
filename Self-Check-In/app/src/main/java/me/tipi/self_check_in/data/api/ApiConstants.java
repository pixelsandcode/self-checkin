/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import me.tipi.self_check_in.BuildConfig;

public class ApiConstants {

  // Api Ips
  public static final String API_URL = "http://api.tipi.me/v1";
  public static final String API_IMAGE_URL = "http://api.tipi.me/cdn";

  // Api Stg
  public static final String API_URL_STG = "http://stg.api.tipi.me/v1";
  public static final String API_IMAGE_URL_STG = "http://stg.api.tipi.me/cdn";

  // Download Page
  public static final String BASE_DOWNLOAD_PAGE = "https://play.google.com/store/apps/details?id=me.tipi.self_check_in";

  public static final int START_OVER_TIME = 600000;

  // Routes
  public static final String LOGIN = "/dashboard/login";
  public static final String HOME_TOWN = "/countries/suggest";
  public static final String SIGN_UP = "/dashboard/users/signup";
  public static final String FIND = "/dashboard/users/find";
  public static final String CLAIM = "/dashboard/users/{user_key}/claim";
  public static final String TERMS = "/hostels/{hostel_key}/terms";
  public static final String NOTE = "/dashboard/guests/{guest_key}/notes/append";

  // Images
  public static final String AVATAR_URL = "/user/%s/savatar";

  // Prefs
  public static final String USER_NAME = "username";
  public static final String PASSWORD = "password";
  public static final String AVATAR = "avatar";
  public static final String PASSPORT = "passport";
  public static final String HOSTEL_NAME = "hostelName";
  public static final String PRINTER = "printer";
  public static final String HOSTEL_KEY = "hostelKey";
  public static final String KIOSK_NAME = "kioskName";

  public static String getApiBaseUrl() {
    if (BuildConfig.STG) {
      return API_URL_STG;
    } else {
      return API_URL;
    }
  }

  public static String getImageUrl() {
    if (BuildConfig.STG) {
      return API_IMAGE_URL_STG;
    } else {
      return API_IMAGE_URL;
    }
  }

}
