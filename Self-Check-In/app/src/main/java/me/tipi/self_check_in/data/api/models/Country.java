/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

import java.util.List;

public final class Country {

  public final String name;
  public final List<String> cities;

  /**
   * Instantiates a new Country.
   *
   * @param name   the name
   * @param cities the cities
   */
  public Country(String name, List<String> cities) {
    this.name = name;
    this.cities = cities;
  }
}
