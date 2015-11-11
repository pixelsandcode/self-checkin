package me.tipi.self_check_in.data.api.models;

import java.util.List;

public final class Country {

  public final String name;
  public final List<String> cities;

  public Country(String name, List<String> cities) {
    this.name = name;
    this.cities = cities;
  }
}
