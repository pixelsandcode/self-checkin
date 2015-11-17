/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

import java.util.List;

public final class CountryResponse {

  public final List<Country> data;

  /**
   * Instantiates a new Country response.
   *
   * @param data the data
   */
  protected CountryResponse(List<Country> data) {
    this.data = data;
  }
}
