/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.util;


import me.tipi.self_check_in.data.api.ApiConstants;

public final class Strings {
  /**
   * Instantiates a new Strings.
   */
  private Strings() {
    // No instances.
  }

  /**
   * Is valid email boolean.
   *
   * @param target the target
   * @return the boolean
   */
  public static boolean isValidEmail(CharSequence target) {
    return target != null &&
        android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
  }

  /**
   * Gets pre string split.
   *
   * @param value    the value
   * @param splitter the splitter
   * @return the pre string split
   */
  public static String getPreStringSplit(String value, String splitter) {
    String[] parts = value.split(splitter);
    if (parts.length > 2) {
      return value.substring(0, value.lastIndexOf("-")).trim();
    }
    return parts[0];
  }

  /**
   * Gets post string split.
   *
   * @param value    the value
   * @param splitter the splitter
   * @return the post string split
   */
  public static String getPostStringSplit(String value, String splitter) {
    String[] parts = value.split(splitter);
    return parts[parts.length - 1];
  }

  /**
   * Make avatar url string.
   *
   * @param userId the user id
   * @return the string
   */
  public static String makeAvatarUrl(String userId) {
    return ApiConstants.API_IMAGE_URL + String.format(ApiConstants.AVATAR_URL, userId) + ".jpg";
  }
}
