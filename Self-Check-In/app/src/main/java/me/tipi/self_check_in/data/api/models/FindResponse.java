/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

public class FindResponse extends BaseResponse{
  public final User data;

  /**
   * Instantiates a new Find response.
   *
   * @param data the data
   */
  public FindResponse(User data) {
    this.data = data;
  }
}
